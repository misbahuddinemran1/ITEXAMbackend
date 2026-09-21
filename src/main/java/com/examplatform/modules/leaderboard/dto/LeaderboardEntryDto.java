package com.examplatform.modules.leaderboard.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDto {
    private int rank;
    private String userId;
    private String userName;
    private String collegeName;
    private BigDecimal totalPoints;
    private BigDecimal avgScorePercent;
    private int examsTaken;
    private boolean isCurrentUser;
}
