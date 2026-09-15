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
public class QuestionStudentResponse {

    private String id;
    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private String partAQuestion;
    private BigDecimal partAMaxMark;

    private String partBQuestion;
    private BigDecimal partBMaxMark;

    private String partCQuestion;
    private BigDecimal partCMaxMark;

    private String partDQuestion;
    private BigDecimal partDMaxMark;

    private BigDecimal totalMaxMark;
}