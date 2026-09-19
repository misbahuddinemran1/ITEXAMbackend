package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * একটা sub-question (part) — admin ইচ্ছামতো ২টা, ৩টা, যেকোনো সংখ্যক এরকম part পাঠাতে পারবে।
 */
@Getter
@Setter
public class QuestionPartRequest {
    private String questionText;
    private String modelAnswer;
    private String markingScheme;
    private BigDecimal maxMark;
}
