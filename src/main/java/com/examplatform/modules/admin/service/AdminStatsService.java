package com.examplatform.modules.admin.service;

import com.examplatform.modules.admin.dto.AdminStatsResponse;
import com.examplatform.modules.exam.entity.Exam;
import com.examplatform.modules.exam.repository.ExamRepository;
import com.examplatform.modules.liveexam.entity.LiveExamSession;
import com.examplatform.modules.liveexam.repository.LiveExamSessionRepository;
import com.examplatform.modules.question.entity.Question;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.question.repository.QuestionRepository;
import com.examplatform.modules.subscription.entity.UserSubscription;
import com.examplatform.modules.subscription.repository.UserSubscriptionRepository;
import com.examplatform.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final QuestionRepository questionRepository;
    private final ExamSessionRepository examSessionRepository;
    private final ExamRepository examRepository;
    private final LiveExamSessionRepository liveExamSessionRepository;
    private final WrittenSubmissionRepository writtenSubmissionRepository;

    public AdminStatsResponse getStats() {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfToday = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        // Recent Users
        List<AdminStatsResponse.RecentUser> recentUsers = userRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(u -> AdminStatsResponse.RecentUser.builder()
                        .id(u.getId())
                        .fullName(u.getFullName())
                        .email(u.getEmail() != null ? u.getEmail() : u.getPhone())
                        .createdAt(u.getCreatedAt() != null ?
                                u.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "")
                        .build())
                .toList();

        // Recent Exams
        List<AdminStatsResponse.RecentExam> recentExams = examSessionRepository
                .findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(e -> {
                    String userName = userRepository.findById(e.getUserId())
                            .map(u -> u.getFullName())
                            .orElse("Unknown");
                    return AdminStatsResponse.RecentExam.builder()
                            .id(e.getId())
                            .userName(userName)
                            .sessionType(e.getSessionType().name())
                            .score((int) e.getScore())
                            .totalQuestions(e.getTotalQuestions())
                            .status(e.getStatus().name())
                            .createdAt(e.getCreatedAt() != null ?
                                    e.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "")
                            .build();
                })
                .toList();

        // Last 7 days chart data
        List<AdminStatsResponse.ChartData> chartData = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i)
                    .withHour(0).withMinute(0).withSecond(0);
            LocalDateTime nextDay = day.plusDays(1);
            long count = examSessionRepository.countByCreatedAtBetween(day, nextDay);
            chartData.add(AdminStatsResponse.ChartData.builder()
                    .date(day.format(formatter))
                    .count(count)
                    .build());
        }

        // চলমান Live Exam: আজ (বাংলাদেশ সময়) PUBLISHED আর এখন সময়-জানালার ভেতরে থাকা exam
        ZoneId bdZone = ZoneId.of("Asia/Dhaka");
        LocalDate todayBd = LocalDate.now(bdZone);
        LocalTime nowBd = LocalTime.now(bdZone);
        long liveExamsNow = examRepository
                .findByPublishStatusAndExamDate(Exam.PublishStatus.PUBLISHED, todayBd)
                .stream()
                .filter(e -> !nowBd.isBefore(e.getStartTime()) && !nowBd.isAfter(e.getEndTime()))
                .count();

        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeSubscriptions(userSubscriptionRepository
                        .countByStatus(UserSubscription.Status.ACTIVE))
                .totalQuestions(questionRepository.count())
                .totalExamSessions(examSessionRepository.count())
                .newUsersThisMonth(userRepository.countByCreatedAtAfter(startOfMonth))
                .todayExamAttempts(examSessionRepository.countByCreatedAtAfter(startOfToday))
                .newUsersToday(userRepository.countByCreatedAtAfter(startOfToday))
                .liveExamsNow(liveExamsNow)
                .liveExamStudents(liveExamSessionRepository
                        .countByStatus(LiveExamSession.Status.IN_PROGRESS))
                .pendingWrittenSubmissions(writtenSubmissionRepository
                        .countByIsPracticeModeFalseAndStatusIn(
                                List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.UNDER_REVIEW)))
                .pendingReviewQuestions(questionRepository
                        .countByStatus(Question.QuestionStatus.UNDER_REVIEW))
                .draftQuestions(questionRepository
                        .countByStatus(Question.QuestionStatus.DRAFT))
                .recentUsers(recentUsers)
                .recentExams(recentExams)
                .last7DaysExams(chartData)
                .build();
    }
}