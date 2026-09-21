package com.examplatform.modules.leaderboard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardSettings {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "overall_min_exams_required", nullable = false)
    @Builder.Default
    private int overallMinExamsRequired = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "monthly_threshold_type", nullable = false, length = 20)
    @Builder.Default
    private ThresholdType monthlyThresholdType = ThresholdType.RELATIVE;

    @Column(name = "monthly_min_exams_required", nullable = false)
    @Builder.Default
    private int monthlyMinExamsRequired = 5;

    @Column(name = "monthly_allowed_missed_exams", nullable = false)
    @Builder.Default
    private int monthlyAllowedMissedExams = 2;

    @Column(name = "level_wise_separate", nullable = false)
    @Builder.Default
    private boolean levelWiseSeparate = true;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(name = "updated_by_admin_id", length = 36)
    private String updatedByAdminId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ThresholdType {
        FIXED, RELATIVE
    }
}
