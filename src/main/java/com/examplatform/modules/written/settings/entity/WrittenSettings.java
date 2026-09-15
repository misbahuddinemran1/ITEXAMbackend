package com.examplatform.modules.written.settings.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "written_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenSettings {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "default_evaluation_mode", nullable = false, length = 10)
    @Builder.Default
    private String defaultEvaluationMode = "MANUAL";

    @Column(name = "allowed_submission_types", nullable = false, length = 100)
    @Builder.Default
    private String allowedSubmissionTypes = "CAMERA,GALLERY,PDF";

    @Column(name = "result_publish_mode", nullable = false, length = 10)
    @Builder.Default
    private String resultPublishMode = "MANUAL";

    @Column(name = "practice_archive_mode", nullable = false, length = 10)
    @Builder.Default
    private String practiceArchiveMode = "AUTO";

    @Column(name = "updated_by_admin_id", length = 36)
    private String updatedByAdminId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = "default";
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
