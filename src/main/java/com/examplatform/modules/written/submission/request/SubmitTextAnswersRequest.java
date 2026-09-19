package com.examplatform.modules.written.submission.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitTextAnswersRequest {

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<TextAnswerEntry> answers;

    @Getter
    @Setter
    public static class TextAnswerEntry {

        @NotBlank(message = "questionId is required")
        private String questionId;

        @NotNull(message = "partOrder is required")
        private Integer partOrder; // 1, 2, 3... (BCS Written স্টাইল flexible sub-question index)

        @NotBlank(message = "answerText is required")
        private String answerText;
    }
}
