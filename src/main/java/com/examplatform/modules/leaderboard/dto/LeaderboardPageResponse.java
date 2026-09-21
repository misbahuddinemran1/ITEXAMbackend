package com.examplatform.modules.leaderboard.dto;

import lombok.*;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardPageResponse {
    private String status; // OK, DISABLED, NEEDS_PROFILE_COMPLETION
    private String educationLevel; // level-wise বন্ধ থাকলে "ALL"
    private String yearMonth;
    private Integer requiredExamsThisMonth;
    private List<LeaderboardEntryDto> entries;
    private long totalElements;
    private Integer myRank;
    private String message;

    public static LeaderboardPageResponse disabled() {
        return LeaderboardPageResponse.builder()
                .status("DISABLED")
                .entries(Collections.emptyList())
                .message("Leaderboard is currently unavailable.")
                .build();
    }

    public static LeaderboardPageResponse needsProfileCompletion() {
        return LeaderboardPageResponse.builder()
                .status("NEEDS_PROFILE_COMPLETION")
                .entries(Collections.emptyList())
                .message("আপনার Target Exam এখনো সেট করা হয়নি। Leaderboard দেখতে প্রোফাইল আপডেট করুন।")
                .build();
    }
}
