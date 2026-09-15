package com.examplatform.modules.written.submission.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubmissionSummaryResponse {

    private String submissionId;
    private String examId;
    private String examTitle;

    private String studentId;
    private String studentName;
    private String studentEducationLevel;
    private String studentInstitution;

    private String status;
    private String fileType; // IMAGE / PDF / TEXT (from first file, assuming uniform per submission)
    private Integer cycleNumber;
    private Integer attemptNumber;
    private LocalDateTime submittedAt;
    private boolean hasTranscript;
}
