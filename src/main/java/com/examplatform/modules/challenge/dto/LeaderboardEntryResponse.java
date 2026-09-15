package com.examplatform.modules.challenge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryResponse {
    private int rank;
    private String userId;
    private String userName;
    private int totalPoints;
    private int totalWins;
    private int totalPlayed;
    private int currentWinStreak;
}
