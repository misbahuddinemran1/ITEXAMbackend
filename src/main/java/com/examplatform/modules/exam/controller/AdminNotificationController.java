package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.request.AdminSendNotificationRequest;
import com.examplatform.modules.exam.entity.UserNotification;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.exam.repository.NotificationGroupProjection;
import com.examplatform.modules.exam.service.NotificationService;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ExamSessionRepository examSessionRepository;
    private final WrittenSubmissionRepository writtenSubmissionRepository;

    @PostMapping("/send")
    public Map<String, Object> sendNotification(@RequestBody AdminSendNotificationRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title আবশ্যক");
        }
        if (request.getBody() == null || request.getBody().isBlank()) {
            throw new IllegalArgumentException("Message আবশ্যক");
        }

        LocalDateTime expiryDate = null;
        if (request.getDeleteAt() != null && !request.getDeleteAt().isBlank()) {
            try {
                expiryDate = LocalDateTime.parse(request.getDeleteAt());
            } catch (Exception e) {
                throw new IllegalArgumentException("deleteAt ফরম্যাট ভুল, ISO datetime দিন (yyyy-MM-ddTHH:mm:ss)");
            }
        }

        List<String> userIds = resolveTargetUserIds(request);
        String batchId = UUID.randomUUID().toString();

        for (String userId : userIds) {
            notificationService.sendNotification(
                    userId,
                    UserNotification.NotificationType.SYSTEM,
                    request.getTitle(),
                    request.getBody(),
                    expiryDate,
                    batchId
            );
        }

        Map<String, Object> result = new HashMap<>();
        result.put("recipientCount", userIds.size());
        return result;
    }

    @GetMapping("/grouped")
    public List<Map<String, Object>> getGroupedNotifications() {
        List<NotificationGroupProjection> grouped = notificationService.getGroupedNotifications();
        return grouped.stream().map(g -> {
            Map<String, Object> m = new HashMap<>();
            m.put("batchKey", g.getBatchKey());
            m.put("title", g.getTitle());
            m.put("body", g.getBody());
            m.put("type", g.getType());
            m.put("sentAt", g.getSentAt());
            m.put("expiryDate", g.getExpiryDate());
            m.put("recipientCount", g.getRecipientCount());
            return m;
        }).collect(Collectors.toList());
    }

    @GetMapping
    public List<UserNotification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @DeleteMapping("/batch/{batchKey}")
    public Map<String, Object> deleteBatch(@PathVariable String batchKey) {
        notificationService.deleteBatch(batchKey);
        Map<String, Object> result = new HashMap<>();
        result.put("deleted", true);
        return result;
    }

    private List<String> resolveTargetUserIds(AdminSendNotificationRequest request) {
        switch (request.getTargetType()) {
            case ALL:
                return userRepository.findAllUserIds();

            case EDUCATION_LEVEL:
                if (request.getEducationLevel() == null) {
                    throw new IllegalArgumentException("educationLevel আবশ্যক");
                }
                User.EducationLevel level = User.EducationLevel.valueOf(request.getEducationLevel());
                return userRepository.findUserIdsByEducationLevel(level);

            case TARGET_EXAM:
                if (request.getTargetExam() == null) {
                    throw new IllegalArgumentException("targetExam আবশ্যক");
                }
                User.TargetExam targetExam = User.TargetExam.valueOf(request.getTargetExam());
                return userRepository.findUserIdsByTargetExam(targetExam);

            case WRITTEN_EXAM:
                if (request.getExamId() == null) {
                    throw new IllegalArgumentException("examId আবশ্যক");
                }
                List<WrittenSubmission> submissions = writtenSubmissionRepository.findByExamId(request.getExamId());
                return submissions.stream()
                        .map(WrittenSubmission::getUserId)
                        .distinct()
                        .collect(Collectors.toList());

            case MCQ_EXAM:
                if (request.getExamId() == null) {
                    throw new IllegalArgumentException("examId আবশ্যক");
                }
                return examSessionRepository.findDistinctUserIdsBySpecialExamId(request.getExamId());

            default:
                throw new IllegalArgumentException("অজানা targetType");
        }
    }
}
