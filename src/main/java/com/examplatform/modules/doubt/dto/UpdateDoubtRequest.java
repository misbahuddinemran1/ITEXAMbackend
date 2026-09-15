package com.examplatform.modules.doubt.dto;

import lombok.Data;

@Data
public class UpdateDoubtRequest {
    private String subjectId;
    private String chapterId;
    private String topicId;
    private String questionText;
}
