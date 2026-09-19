package com.examplatform.modules.written.question.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Exam শেষ হওয়ার পর রিভিউ-এর জন্য — উত্তরসহ (model answer থাকলে সেটা, না থাকলে AI answer) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPartAnswerResponse {
    private Integer partOrder;
    private String questionText;
    private String answer;
    private Boolean isAi;
    private BigDecimal maxMark;
}
