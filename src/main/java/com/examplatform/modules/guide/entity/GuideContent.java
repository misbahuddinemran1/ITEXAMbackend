package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "guide_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuideContent extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false, unique = true)
    private Topic topic;

    @Column(nullable = false, length = 255)
    private String title;

   @Column(name = "body_html", columnDefinition = "TEXT")
private String bodyHtml;

    @Column(name = "pdf_url", columnDefinition = "TEXT")
    private String pdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private GuideStatus status = GuideStatus.DRAFT;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public enum GuideStatus { DRAFT, PUBLISHED, ARCHIVED }
}
