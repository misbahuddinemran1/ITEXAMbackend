package com.examplatform.modules.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuidePracticeCqResponse {
    private String id;
    private String topicId;
    private String stimulus;
    private String stimulusBn;

    @JsonProperty("isBoardQuestion")
    private boolean isBoardQuestion;

    private String board;
    private Integer examYear;

    private List<GuidePracticeCqPartResponse> parts;

    private Integer totalMaxMark;
    private int sortOrder;
}
