package com.examplatform.modules.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuidePracticeMcqOptionResponse {
    private String id;
    private String optionKey;
    private String optionText;
    private String optionTextBn;

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    private String explanation;
    private int orderIndex;
}
