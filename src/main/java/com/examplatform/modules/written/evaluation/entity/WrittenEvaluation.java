package com.examplatform.modules.written.evaluation.entity;

import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.exam.enums.AiProvider;
import com.examplatform.modules.written.exam.enums.EvaluationMode;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "written_evaluation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenEvaluation {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private WrittenSubmission submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_mode", nullable = false, length = 10)
    private EvaluationMode evaluationMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.PENDING;

    @Column(name = "total_mark")
    private java.math.BigDecimal totalMark;

    @Column(name = "evaluated_by_admin_id", length = 36)
    private String evaluatedByAdminId;

    @Column(name = "ai_raw_response", columnDefinition = "TEXT")
    private String aiRawResponse;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

    /**
     * Whether the student is allowed to see this evaluation's marks yet.
     * Controlled by written_settings.resultPublishMode:
     *  - INSTANT -> set true automatically the moment finalizeEvaluation() runs
     *  - MANUAL  -> stays false until an admin explicitly calls the publish-result endpoint
     */
    @Column(name = "result_published", nullable = false)
    @Builder.Default
    private boolean resultPublished = false;

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
