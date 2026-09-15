package com.examplatform.modules.written.evaluation.manual.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ManualEvaluationRequest {

    @NotEmpty(message = "At least one part mark is required")
    @Valid
    private List<PartMarkRequest> partMarks;
}