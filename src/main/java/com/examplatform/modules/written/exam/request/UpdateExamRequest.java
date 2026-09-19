package com.examplatform.modules.written.exam.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UpdateExamRequest {

    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String titleBn;

    private String description;

    private String educationLevel;

    private String subjectId;
    private String chapterId;
    private String topicId;

    @Min(1)
    private Integer durationMinutes;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String evaluationMode;

    private String aiProvider;

    private List<Integer> aiPartOrders;

    private Boolean practiceEnabled;
    private Boolean showResultInPractice;
}
