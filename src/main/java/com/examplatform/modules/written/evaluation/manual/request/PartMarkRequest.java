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

    @NotBlank(message = "part is required")
    private String part; // A / B / C / D

    @NotNull(message = "obtainedMark is required")
    private BigDecimal obtainedMark;

    private String feedback;
}