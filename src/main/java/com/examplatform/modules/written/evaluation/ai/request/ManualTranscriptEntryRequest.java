package com.examplatform.modules.written.evaluation.ai.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualTranscriptEntryRequest {
    private String questionId;
    private String part; // A/B/C/D
    @NotBlank(message = "transcribedText is required")
    private String transcribedText;
}
