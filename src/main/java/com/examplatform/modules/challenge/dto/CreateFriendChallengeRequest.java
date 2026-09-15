package com.examplatform.modules.challenge.dto;

import lombok.Data;

@Data
public class CreateFriendChallengeRequest {
    private String opponentId;
    private String chapterId;
    private String topicId;      // optional, null হলে chapter-এর সব topic মিশিয়ে
    private Integer questionCount; // optional, default 10
}
