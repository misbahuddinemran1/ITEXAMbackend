package com.examplatform.modules.liveexam.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RevisionQuizResponse {
    private int totalQuestions;
    private List<RevisionQuestionDto> questions;
}
