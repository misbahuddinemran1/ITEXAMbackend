package com.examplatform.modules.written.evaluation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {

    private String id;
    private String submissionId;
    private String examId;
    private String studentUserId;
    private String evaluationMode;
    private String status;
    private BigDecimal totalMark;
    private boolean resultPublished;
    private String evaluatedByAdminId;
    private LocalDateTime evaluatedAt;
    private List<EvaluationDetailResponse> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
