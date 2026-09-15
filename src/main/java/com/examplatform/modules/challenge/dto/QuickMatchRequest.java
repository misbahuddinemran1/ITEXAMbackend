package com.examplatform.modules.challenge.dto;

import lombok.Data;

@Data
public class QuickMatchRequest {
    private String chapterId;
    private String topicId;
    private Integer questionCount;
}
