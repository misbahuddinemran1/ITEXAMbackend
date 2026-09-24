package com.examplatform.modules.liveexam.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectAccuracyResponse {
    private String subjectId;
    private String subjectName;
    private long totalAttempts;
    private long totalCorrect;
    private long totalWrong;
    private long totalSkipped;
    private double accuracyRate;
}
