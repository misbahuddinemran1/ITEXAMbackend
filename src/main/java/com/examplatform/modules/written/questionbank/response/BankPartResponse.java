package com.examplatform.modules.written.questionbank.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankPartResponse {
    private String id;
    private Integer partOrder;
    private String questionText;
    private String modelAnswer;
    private String aiAnswer;
    private String markingScheme;
    private BigDecimal maxMark;
}
