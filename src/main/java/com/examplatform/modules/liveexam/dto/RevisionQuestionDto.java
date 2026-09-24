package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RevisionQuestionDto {
    private String questionId;
    private String questionText;
    private String questionTextBn;
    private List<OptionDto> options;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionDto {
        private String optionId;
        private String optionKey;
        private String optionText;
        private String optionTextBn;
    }
}
