package com.examplatform.modules.written.exam.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private String subjectId;   // optional at exam level
    private String chapterId;   // optional
    private String topicId;     // optional

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotBlank(message = "Evaluation mode is required")
    private String evaluationMode; // MANUAL / AI / HYBRID

    private String aiProvider;     // GEMINI / CLAUDE / OPENAI — required when evaluationMode = AI or HYBRID

    // Only relevant when evaluationMode = HYBRID; each value AI / MANUAL
    private String partAMode;
    private String partBMode;
    private String partCMode;
    private String partDMode;

    // Practice control settings (admin configurable per exam)
    private Boolean practiceEnabled = true;
    private Boolean showResultInPractice = true;
}
