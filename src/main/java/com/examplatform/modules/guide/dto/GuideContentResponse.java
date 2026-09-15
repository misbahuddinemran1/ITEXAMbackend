package com.examplatform.modules.guide.dto;

import com.examplatform.modules.guide.entity.GuideContent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GuideContentResponse {
    private String id;
    private String topicId;
    private String topicName;
    private String title;
    private String bodyHtml;
    private String pdfUrl;
    private String status;
    private int sortOrder;
    private LocalDateTime publishedAt;

    public static GuideContentResponse from(GuideContent c) {
        return GuideContentResponse.builder()
                .id(c.getId())
                .topicId(c.getTopic().getId())
                .topicName(c.getTopic().getName())
                .title(c.getTitle())
                .bodyHtml(c.getBodyHtml())
                .pdfUrl(c.getPdfUrl())
                .status(c.getStatus().name())
                .sortOrder(c.getSortOrder())
                .publishedAt(c.getPublishedAt())
                .build();
    }
}
