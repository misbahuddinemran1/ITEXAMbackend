package com.examplatform.modules.written.submission.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

        @NotBlank(message = "part is required")
        private String part; // A / B / C / D

        @NotBlank(message = "answerText is required")
        private String answerText;
    }
}
