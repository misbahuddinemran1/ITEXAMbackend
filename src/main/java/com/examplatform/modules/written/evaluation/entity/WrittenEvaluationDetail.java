package com.examplatform.modules.written.evaluation.entity;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "written_evaluation_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenEvaluationDetail {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private WrittenEvaluation evaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private WrittenQuestion question;

    @Enumerated(EnumType.STRING)
    @Column(name = "part", nullable = false, length = 1)
    private QuestionPart part;

    @Column(name = "obtained_mark", nullable = false)
    @Builder.Default
    private BigDecimal obtainedMark = BigDecimal.ZERO;

    @Column(name = "max_mark", nullable = false)
    @Builder.Default
    private BigDecimal maxMark = BigDecimal.ZERO;

    @Column(name = "predicted_mark_manual", precision = 5, scale = 2)
    private BigDecimal predictedMarkManual;

    @Column(name = "predicted_mark_ai", precision = 5, scale = 2)
    private BigDecimal predictedMarkAi;

    @Column(name = "match_score_manual", precision = 5, scale = 4)
    private BigDecimal matchScoreManual;

    @Column(name = "match_score_ai", precision = 5, scale = 4)
    private BigDecimal matchScoreAi;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }
}