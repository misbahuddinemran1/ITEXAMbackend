package com.examplatform.modules.written.questionbank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "written_question_bank_part")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenQuestionBankPart {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_question_id", nullable = false)
    private WrittenQuestionBank bankQuestion;

    @Column(name = "part_order", nullable = false)
    private Integer partOrder;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(name = "ai_answer", columnDefinition = "TEXT")
    private String aiAnswer;

    @Column(name = "marking_scheme", columnDefinition = "TEXT")
    private String markingScheme;

    @Column(name = "max_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxMark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
