package com.examplatform.modules.written.evaluation.service;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.response.WrittenLeaderboardEntryResponse;

import java.util.List;

public interface WrittenEvaluationService {

    EvaluationResponse getEvaluationBySubmissionId(String submissionId);

    EvaluationResponse getEvaluationById(String evaluationId);

    List<EvaluationResponse> getEvaluationsForExam(String examId);

    List<WrittenLeaderboardEntryResponse> getLeaderboard(String examId, String requestingUserId);
}
