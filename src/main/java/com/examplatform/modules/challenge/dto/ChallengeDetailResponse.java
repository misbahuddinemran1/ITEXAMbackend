package com.examplatform.modules.challenge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChallengeDetailResponse {
    private String id;
    private String mode;
    private String status;
    private String creatorId;
    private String creatorName;
    private String opponentId;
    private String opponentName;
    private String chapterId;
    private String topicId;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    @JsonProperty("isMeCreator")
    private boolean isMeCreator;

    @JsonProperty("hasIStarted")
    private boolean hasIStarted;

    @JsonProperty("hasOpponentFinished")
    private boolean hasOpponentFinished;

    // নতুন: আমি (requesting user) কোন কোন mcqId-র উত্তর ইতিমধ্যে দিয়ে ফেলেছি — resume এর জন্য দরকার
    private List<String> answeredMcqIds;

    // নতুন: আমি আমার নির্ধারিত সব প্রশ্নের উত্তর দিয়ে ফেলেছি কিনা
    @JsonProperty("myTurnDone")
    private boolean myTurnDone;

    private List<ChallengeQuestionResponse> questions;
}
