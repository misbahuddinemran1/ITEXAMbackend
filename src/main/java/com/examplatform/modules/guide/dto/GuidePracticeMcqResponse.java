package com.examplatform.modules.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuidePracticeMcqResponse {
    private String id;
    private String topicId;
    private String questionText;
    private String questionTextBn;

    @JsonProperty("isBoardQuestion")
    private boolean isBoardQuestion;

    private String board;
    private Integer yearAppeared;
    private int sortOrder;
    private List<GuidePracticeMcqOptionResponse> options;
}
