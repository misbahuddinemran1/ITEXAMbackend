package com.examplatform.modules.written.exam.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateExamRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 200)
    private String titleBn;

    private String description;

    @NotBlank(message = "Education level is required")
    private String educationLevel;

    private String subjectId;
    private String chapterId;
    private String topicId;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotBlank(message = "Evaluation mode is required")
    private String evaluationMode;

    private String aiProvider;

    // শুধু evaluationMode = HYBRID হলে প্রাসঙ্গিক — কোন কোন sub-question-index AI দিয়ে মূল্যায়ন হবে
    private List<Integer> aiPartOrders;

    private Boolean practiceEnabled = true;
    private Boolean showResultInPractice = true;
}
