package com.examplatform.modules.written.questionbank.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateBankQuestionRequest {

    private String subjectId;
    private String chapterId;
    private String topicId;

    private String stimulus;
    private String stimulusBn;

    private Boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    private boolean regenerateAiAnswer;

    private String partAQuestion;
    private String partAModelAnswer;
    private String partAAiAnswer;
    private String partAMarkingScheme;
    private BigDecimal partAMaxMark;

    private String partBQuestion;
    private String partBModelAnswer;
    private String partBAiAnswer;
    private String partBMarkingScheme;
    private BigDecimal partBMaxMark;

    private String partCQuestion;
    private String partCModelAnswer;
    private String partCAiAnswer;
    private String partCMarkingScheme;
    private BigDecimal partCMaxMark;

    private String partDQuestion;
    private String partDModelAnswer;
    private String partDAiAnswer;
    private String partDMarkingScheme;
    private BigDecimal partDMaxMark;
}
