package com.examplatform.modules.challenge.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChallengeQuestionResponse {
    private String mcqId;
    private int orderIndex;
    private String questionText;
    private String questionTextBn;
    private List<OptionItem> options;

    @Data
    @Builder
    public static class OptionItem {
        private String optionId;
        private String optionKey;
        private String optionText;
        private String optionTextBn;
        // isCorrect ইচ্ছাকৃতভাবে বাদ, খেলা চলাকালীন client কে দেখানো যাবে না
    }
}
