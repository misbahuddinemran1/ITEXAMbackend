package com.examplatform.modules.leaderboard.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyRankResponse {
    private String status; // OK, NOT_ELIGIBLE, NOT_STARTED, NEEDS_PROFILE_COMPLETION, DISABLED
    private Integer rank;
    private int examsTaken;
    private Integer examsNeededMore;
    private BigDecimal totalPoints;
    private BigDecimal avgScorePercent;
    private String message;

    public static MyRankResponse needsProfileCompletion() {
        return MyRankResponse.builder()
                .status("NEEDS_PROFILE_COMPLETION")
                .message("আপনার Target Exam এখনো সেট করা হয়নি। Leaderboard দেখতে প্রোফাইল আপডেট করুন।")
                .build();
    }
}
