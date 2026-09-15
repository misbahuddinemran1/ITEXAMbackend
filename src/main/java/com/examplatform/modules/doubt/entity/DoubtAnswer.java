package com.examplatform.modules.doubt.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "doubt_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoubtAnswer {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "doubt_question_id", length = 36, nullable = false, unique = true)
    private String doubtQuestionId;

    // DoubtAnswer.java তে — এইটা পরিবর্তন করতে হবে
@Column(name = "admin_id", length = 36) // nullable = false বাদ দিলাম
private String adminId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "answer_pdf_url", length = 500)
    private String answerPdfUrl;

    @Column(name = "answered_via_ai", nullable = false)
    private Boolean answeredViaAi;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) this.id = java.util.UUID.randomUUID().toString();
        if (this.answeredViaAi == null) this.answeredViaAi = false;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
