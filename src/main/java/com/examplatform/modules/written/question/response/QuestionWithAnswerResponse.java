package com.examplatform.modules.written.question.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionWithAnswerResponse {

    private String id;
    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private List<QuestionPartAnswerResponse> parts;
    private BigDecimal totalMaxMark;
}
