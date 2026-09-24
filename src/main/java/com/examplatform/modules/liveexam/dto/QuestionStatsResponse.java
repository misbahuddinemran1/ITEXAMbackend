package com.examplatform.modules.liveexam.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionStatsResponse {
    private String questionId;
    private String questionText;
    private long totalAttempts;
    private long totalCorrect;
    private long totalWrong;
    private long totalSkipped;
    private double accuracyRate;
}
