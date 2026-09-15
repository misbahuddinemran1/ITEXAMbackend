package com.examplatform.modules.doubt.dto;

import lombok.Data;

@Data
public class CreateDoubtRequest {
    private String subjectId;
    private String chapterId; // required
    private String topicId;
    private String questionText;
    // image/pdf আলাদা upload endpoint দিয়ে আসবে
}
