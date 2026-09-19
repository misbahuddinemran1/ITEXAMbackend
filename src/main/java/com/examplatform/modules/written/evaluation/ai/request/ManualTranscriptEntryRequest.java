package com.examplatform.modules.written.evaluation.ai.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualTranscriptEntryRequest {
    private String questionId;
    @NotNull(message = "partOrder is required")
    private Integer partOrder; // 1, 2, 3...
    @NotBlank(message = "transcribedText is required")
    private String transcribedText;
}
