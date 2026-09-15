package com.examplatform.modules.written.evaluation.ai.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ManualTranscriptBulkRequest {
    @NotEmpty
    @Valid
    private List<ManualTranscriptEntryRequest> entries;
}
