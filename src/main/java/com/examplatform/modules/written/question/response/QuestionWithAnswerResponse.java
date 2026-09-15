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
public class QuestionWithAnswerResponse {

    private String id;
    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private String partAQuestion;
    private String partAAnswer; // modelAnswer থাকলে সেটা, না থাকলে aiAnswer
    private Boolean partAIsAi;  // true হলে AI answer দেখানো হচ্ছে (UI badge এর জন্য)
    private BigDecimal partAMaxMark;

    private String partBQuestion;
    private String partBAnswer;
    private Boolean partBIsAi;
    private BigDecimal partBMaxMark;

    private String partCQuestion;
    private String partCAnswer;
    private Boolean partCIsAi;
    private BigDecimal partCMaxMark;

    private String partDQuestion;
    private String partDAnswer;
    private Boolean partDIsAi;
    private BigDecimal partDMaxMark;

    private BigDecimal totalMaxMark;
}
