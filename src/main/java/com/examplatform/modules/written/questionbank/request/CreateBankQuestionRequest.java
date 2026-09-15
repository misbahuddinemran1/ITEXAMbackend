package com.examplatform.modules.written.questionbank.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateBankQuestionRequest {

    private String subjectId;   // required
    private String chapterId;   // required
    private String topicId;     // optional

    private String stimulus;
    private String stimulusBn;

    private boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    // true দিলে create করার সময়ই AI answer বানিয়ে ফেলবে
    private boolean autoGenerateAiAnswer;

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
