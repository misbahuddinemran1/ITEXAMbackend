package com.examplatform.modules.challenge.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengeResultResponse {
    private String challengeId;
    private String winnerId;   // null = draw
    private String creatorId;
    private String creatorName;
    private int creatorScore;
    private int creatorPointsEarned;
    private String opponentId;
    private String opponentName;
    private int opponentScore;
    private int opponentPointsEarned;
    private boolean isDraw;
}
