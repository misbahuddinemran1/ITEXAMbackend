package com.examplatform.modules.admin.dto;

import com.examplatform.modules.user.dto.UserExamSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserActivityResponse {
    private UserExamSummaryResponse exams;
    private int loginCount;
    private String educationLevel;
    private String targetExam;
    private List<SubscriptionItem> subscriptions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubscriptionItem {
        private String planName;
        private String status;
        private String method;
        private String startsAt;
        private String expiresAt;
        private String notes;
    }
}
