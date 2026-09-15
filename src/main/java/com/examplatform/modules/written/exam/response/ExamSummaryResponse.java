package com.examplatform.modules.written.exam.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSummaryResponse {

    private String id;
    private String title;
    private String subjectName;
    private String educationLevel;
    private Integer totalMarks;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Boolean alreadyAttemptedThisCycle;
    private Boolean practiceEnabled;
    private Boolean showResultInPractice;
}
