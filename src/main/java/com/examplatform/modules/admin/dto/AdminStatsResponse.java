package com.examplatform.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsResponse {
    private long totalUsers;
    private long activeSubscriptions;
    private long totalQuestions;
    private long totalExamSessions;
    private long newUsersThisMonth;
    private long todayExamAttempts;
    private long newUsersToday;
    private long liveExamsNow;
    private long liveExamStudents;
    private long pendingWrittenSubmissions;
    private long pendingReviewQuestions;
    private long draftQuestions;
    private List<RecentUser> recentUsers;
    private List<RecentExam> recentExams;
    private List<ChartData> last7DaysExams;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentUser {
        private String id;
        private String fullName;
        private String email;
        private String createdAt;
        private String subscriptionStatus;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentExam {
        private String id;
        private String userName;
        private String sessionType;
        private Integer score;
        private Integer totalQuestions;
        private String createdAt;
        private String status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChartData {
        private String date;
        private long count;
    }
}