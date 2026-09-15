package com.examplatform.modules.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

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

    private String partAQuestion;
    private String partAModelAnswer;
    private String partAMarkingScheme;
    private Integer partAMaxMark;

    private String partBQuestion;
    private String partBModelAnswer;
    private String partBMarkingScheme;
    private Integer partBMaxMark;

    private String partCQuestion;
    private String partCModelAnswer;
    private String partCMarkingScheme;
    private Integer partCMaxMark;

    private String partDQuestion;
    private String partDModelAnswer;
    private String partDMarkingScheme;
    private Integer partDMaxMark;

    private Integer totalMaxMark;
    private int sortOrder;
}
