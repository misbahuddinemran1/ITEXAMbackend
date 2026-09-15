package com.examplatform.modules.doubt.dto;

import com.examplatform.modules.doubt.enums.DoubtStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DoubtResponse {
    private String id;
    private String studentUserId;
    private String subjectId;
    private String chapterId;
    private String topicId;
    private String questionText;
    private String questionImageUrl;
    private String questionPdfUrl;
    private DoubtStatus status;

    // answer (nullable, ANSWERED না হলে সব null)
    private String answerText;
    private String answerPdfUrl;
    private Boolean answeredViaAi;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
