package com.examplatform.modules.written.question.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAdminResponse {

    private String id;
    private String examId;

    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;

    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private boolean isBoardQuestion;
    private String board;
    private Integer examYear;

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

    private BigDecimal totalMaxMark;
}
