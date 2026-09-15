package com.examplatform.modules.written.submission.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {

    private String id;
    private String examId;
    private String userId;
    private Integer cycleNumber;
    private Integer attemptNumber;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private BigDecimal totalObtainedMark;
    private boolean isPracticeMode;
    private LocalDateTime createdAt;
}
