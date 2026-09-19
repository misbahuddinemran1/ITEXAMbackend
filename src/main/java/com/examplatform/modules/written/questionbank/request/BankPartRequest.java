package com.examplatform.modules.written.questionbank.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BankPartRequest {
    private String questionText;
    private String modelAnswer;
    private String aiAnswer;
    private String markingScheme;
    private BigDecimal maxMark;
}
