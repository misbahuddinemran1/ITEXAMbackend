
package com.examplatform.modules.admin.service;

import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.admin.dto.AdminUserActivityResponse;
import com.examplatform.modules.admin.dto.AdminUserCountsResponse;
import com.examplatform.modules.admin.dto.AdminUserResponse;
import com.examplatform.modules.admin.dto.GrantSubscriptionRequest;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.subscription.entity.SubscriptionPlan;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.repository.SubscriptionPlanRepository;
import com.examplatform.modules.subscription.repository.UserSubscriptionRepository;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import com.examplatform.modules.user.service.UserExamSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final ExamSessionRepository examSessionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserExamSummaryService userExamSummaryService;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");

    public Page<AdminUserResponse> getAllUsers(String keyword, int page, int size) {
        // নতুন user আগে; শুধু এই পাতার user গুলোকেই DB থেকে এনে response বানানো হয়
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users = (keyword != null && !keyword.isBlank())
                ? userRepository.searchUsersPaged(keyword.trim(), pageable)
                : userRepository.findAll(pageable);

        return users.map(this::toResponse);
    }

    public AdminUserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public void toggleUserStatus(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public void grantSubscription(String userId, GrantSubscriptionRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanCode(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found: " + req.getPlanId()));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(
                req.getDurationDays() > 0 ? req.getDurationDays() : plan.getDurationDays()
        );

        UserSubscription subscription = UserSubscription.builder()
                .userId(user.getId())
                .plan(plan)
                .status(UserSubscription.Status.ACTIVE)
                .paymentMethod(UserSubscription.PaymentMethod.ADMIN_GRANTED)
                .startsAt(now)
                .expiresAt(expiry)
                .amountPaid(0)
                .discountAmount(0)
                .notes(req.getNotes() != null ? req.getNotes() : "Admin কর্তৃক প্রদত্ত")
                .build();

        userSubscriptionRepository.save(subscription);
    }

    public AdminUserCountsResponse getUserCounts() {
        return AdminUserCountsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActive(true))
                .blockedUsers(userRepository.countByIsActive(false))
                .activeSubscriptions(userSubscriptionRepository
                        .countByStatus(UserSubscription.Status.ACTIVE))
                .trialSubscriptions(userSubscriptionRepository
                        .countByStatus(UserSubscription.Status.TRIAL))
                .build();
    }

    public AdminUserActivityResponse getUserActivity(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AdminUserActivityResponse.SubscriptionItem> subscriptions = userSubscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(10)
                .map(sub -> AdminUserActivityResponse.SubscriptionItem.builder()
                        .planName(sub.getPlan() != null ? sub.getPlan().getNameBn() : null)
                        .status(sub.getStatus() != null ? sub.getStatus().name() : null)
                        .method(sub.getPaymentMethod() != null ? sub.getPaymentMethod().name() : null)
                        .startsAt(sub.getStartsAt() != null ? sub.getStartsAt().format(FMT) : null)
                        .expiresAt(sub.getExpiresAt() != null ? sub.getExpiresAt().format(FMT) : null)
                        .notes(sub.getNotes())
                        .build())
                .toList();

        return AdminUserActivityResponse.builder()
                .exams(userExamSummaryService.getSummary(userId))
                .loginCount(user.getLoginCount())
                .educationLevel(user.getEducationLevel() != null ? user.getEducationLevel().name() : null)
                .targetExam(user.getTargetExam() != null ? user.getTargetExam().name() : null)
                .subscriptions(subscriptions)
                .build();
    }

    // চলমান (ACTIVE/TRIAL, মেয়াদ বাকি) সব subscription বাতিল করে CANCELLED করা
    public void revokeSubscription(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> current = userSubscriptionRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(sub -> (sub.getStatus() == UserSubscription.Status.ACTIVE
                        || sub.getStatus() == UserSubscription.Status.TRIAL)
                        && sub.getExpiresAt() != null
                        && sub.getExpiresAt().isAfter(now))
                .toList();

        if (current.isEmpty()) {
            throw new ValidationException("এই user এর কোনো চলমান subscription নেই");
        }

        String note = "Admin কর্তৃক বাতিল (" + now.format(FMT) + ")";
        for (UserSubscription sub : current) {
            sub.setStatus(UserSubscription.Status.CANCELLED);
            sub.setNotes(sub.getNotes() == null ? note : sub.getNotes() + " | " + note);
        }
        userSubscriptionRepository.saveAll(current);
    }

    private AdminUserResponse toResponse(User u) {
        String subStatus = "NONE";
        String subExpiry = null;
        String subPlanName = null;

        var activeSub = userSubscriptionRepository
                .findTopByUserIdAndStatusAndExpiresAtAfter(
                        u.getId(),
                        UserSubscription.Status.ACTIVE,
                        LocalDateTime.now());

        if (activeSub.isPresent()) {
            subStatus = "ACTIVE";
            subExpiry = activeSub.get().getExpiresAt() != null
                    ? activeSub.get().getExpiresAt().format(FMT) : null;
            subPlanName = activeSub.get().getPlan() != null
                    ? activeSub.get().getPlan().getNameBn() : null;
        } else {
            var trialSub = userSubscriptionRepository
                    .findTopByUserIdAndStatusAndExpiresAtAfter(
                            u.getId(),
                            UserSubscription.Status.TRIAL,
                            LocalDateTime.now());
            if (trialSub.isPresent()) {
                subStatus = "TRIAL";
                subExpiry = trialSub.get().getExpiresAt() != null
                        ? trialSub.get().getExpiresAt().format(FMT) : null;
                subPlanName = trialSub.get().getPlan() != null
                        ? trialSub.get().getPlan().getNameBn() : null;
            }
        }

        long totalExams = userExamSummaryService.getSummary(u.getId()).getTotalExams();

        return AdminUserResponse.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .isActive(u.isActive())
                .createdAt(u.getCreatedAt() != null
                        ? u.getCreatedAt().format(FMT) : "")
                .lastLoginAt(u.getLastLoginAt() != null
                        ? u.getLastLoginAt().format(FMT) : "Never")
                .subscriptionStatus(subStatus)
                .subscriptionExpiry(subExpiry)
                .subscriptionPlanName(subPlanName)
                .totalExams(totalExams)
                .build();
    }
}
