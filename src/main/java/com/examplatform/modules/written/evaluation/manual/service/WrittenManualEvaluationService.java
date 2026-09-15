package com.examplatform.modules.written.evaluation.manual.service;

import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;

public interface WrittenManualEvaluationService {

    EvaluationResponse submitManualEvaluation(String submissionId, ManualEvaluationRequest request, String adminId);
}