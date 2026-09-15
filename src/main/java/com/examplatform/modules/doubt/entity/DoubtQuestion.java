package com.examplatform.modules.doubt.entity;

import com.examplatform.modules.doubt.enums.DoubtStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doubt_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoubtQuestion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "student_user_id", length = 36, nullable = false)
    private String studentUserId;

    @Column(name = "subject_id", length = 36)
    private String subjectId;

    @Column(name = "chapter_id", length = 36, nullable = false)
    private String chapterId;

    @Column(name = "topic_id", length = 36)
    private String topicId;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_image_url", length = 500)
    private String questionImageUrl;

    @Column(name = "question_pdf_url", length = 500)
    private String questionPdfUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DoubtStatus status;

    @Column(name = "reviewed_by_admin_id", length = 36)
    private String reviewedByAdminId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        if (this.status == null) this.status = DoubtStatus.PENDING;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
