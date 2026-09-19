package com.examplatform.modules.written.question.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Admin view — full part detail (model answer + AI answer সহ) */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPartResponse {
    private String id;
    private Integer partOrder;
    private String questionText;
    private String modelAnswer;
    private String aiAnswer;
    private String markingScheme;
    private BigDecimal maxMark;
}
