package com.examplatform.modules.guide.dto;

import lombok.Data;

@Data
public class GuideContentAdminRequest {
    private String topicId;
    private String title;
    private String bodyHtml;
    private String pdfUrl;
    private Integer sortOrder;
}
