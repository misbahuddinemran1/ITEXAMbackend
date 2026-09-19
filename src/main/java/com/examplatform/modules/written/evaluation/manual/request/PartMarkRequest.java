package com.examplatform.modules.written.evaluation.manual.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PartMarkRequest {

    @NotBlank(message = "questionId is required")
    private String questionId;

    @NotNull(message = "partOrder is required")
    private Integer partOrder; // 1, 2, 3... (BCS Written স্টাইল flexible sub-question index)

    @NotNull(message = "obtainedMark is required")
    private BigDecimal obtainedMark;

    private String feedback;
}
