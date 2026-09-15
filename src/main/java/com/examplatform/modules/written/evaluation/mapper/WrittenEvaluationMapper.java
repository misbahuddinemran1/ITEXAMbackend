package com.examplatform.modules.written.evaluation.mapper;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.response.EvaluationDetailResponse;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WrittenEvaluationMapper {

    public EvaluationResponse toResponse(WrittenEvaluation evaluation, List<WrittenEvaluationDetail> details) {
        return EvaluationResponse.builder()
                .id(evaluation.getId())
                .submissionId(evaluation.getSubmission().getId())
                .examId(evaluation.getSubmission().getExamId())
                .studentUserId(evaluation.getSubmission().getUserId())
                .evaluationMode(evaluation.getEvaluationMode().name())
                .status(evaluation.getStatus().name())
                .totalMark(evaluation.getTotalMark())
                .resultPublished(evaluation.isResultPublished())
                .evaluatedByAdminId(evaluation.getEvaluatedByAdminId())
                .evaluatedAt(evaluation.getEvaluatedAt())
                .details(details.stream().map(this::toDetailResponse).toList())
                .createdAt(evaluation.getCreatedAt())
                .updatedAt(evaluation.getUpdatedAt())
                .build();
    }

    public EvaluationDetailResponse toDetailResponse(WrittenEvaluationDetail detail) {
        return EvaluationDetailResponse.builder()
                .id(detail.getId())
                .questionId(detail.getQuestion().getId())
                .questionOrder(detail.getQuestion().getQuestionOrder())
                .part(detail.getPart().name())
                .obtainedMark(detail.getObtainedMark())
                .maxMark(detail.getMaxMark())
                .predictedMarkManual(detail.getPredictedMarkManual())
                .predictedMarkAi(detail.getPredictedMarkAi())
                .matchScoreManual(detail.getMatchScoreManual())
                .matchScoreAi(detail.getMatchScoreAi())
                .feedback(detail.getFeedback())
                .build();
    }
}
