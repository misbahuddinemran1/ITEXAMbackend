package com.examplatform.modules.written.question.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Student view — উত্তর/marking scheme ছাড়া, শুধু প্রশ্ন */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionPartStudentResponse {
    private Integer partOrder;
    private String questionText;
    private BigDecimal maxMark;
}
