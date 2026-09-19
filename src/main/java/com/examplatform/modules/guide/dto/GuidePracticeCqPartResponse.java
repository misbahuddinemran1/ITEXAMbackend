package com.examplatform.modules.guide.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuidePracticeCqPartResponse {
    private String id;
    private Integer partOrder;
    private String questionText;
    private String modelAnswer;
    private String markingScheme;
    private Integer maxMark;
}
