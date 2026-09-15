package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateQuestionRequest {

    private String subjectId;
    private String chapterId;
    private String topicId;

    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private Boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    private String partAQuestion;
    private String partAModelAnswer;
    private String partAMarkingScheme;
    private BigDecimal partAMaxMark;

    private String partBQuestion;
    private String partBModelAnswer;
    private String partBMarkingScheme;
    private BigDecimal partBMaxMark;

    private String partCQuestion;
    private String partCModelAnswer;
    private String partCMarkingScheme;
    private BigDecimal partCMaxMark;

    private String partDQuestion;
    private String partDModelAnswer;
    private String partDMarkingScheme;
    private BigDecimal partDMaxMark;
}
