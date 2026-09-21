package com.examplatform.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserCountsResponse {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long activeSubscriptions;
    private long trialSubscriptions;
}
