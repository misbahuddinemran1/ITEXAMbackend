package com.examplatform.modules.written.submission.entity;

import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "written_submission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenSubmission {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "cycle_number", nullable = false)
    @Builder.Default
    private Integer cycleNumber = 1;

    @Column(name = "attempt_number", nullable = false)
    @Builder.Default
    private Integer attemptNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.NOT_STARTED;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "total_obtained_mark", precision = 6, scale = 2)
    private BigDecimal totalObtainedMark;

    @Column(name = "is_practice_mode", nullable = false)
    @Builder.Default
    private boolean isPracticeMode = false;

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