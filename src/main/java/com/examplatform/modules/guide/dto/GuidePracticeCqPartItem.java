package com.examplatform.modules.guide.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuidePracticeCqPartItem {
    private String questionText;
    private String modelAnswer;
    private String markingScheme;
    private Integer maxMark;
}
