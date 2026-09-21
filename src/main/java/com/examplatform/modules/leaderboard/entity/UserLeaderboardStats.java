package com.examplatform.modules.leaderboard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * প্রতি ছাত্রের সর্বকালের leaderboard হিসাব।
 * educationLevel এ এই প্রজেক্টে ছাত্রের Target Exam (Exam Category code) জমা থাকে।
 */
@Entity
@Table(name = "user_leaderboard_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLeaderboardStats {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36, unique = true)
    private String userId;

    @Column(name = "education_level", length = 30)
    private String educationLevel;

    @Column(name = "total_exams_taken", nullable = false)
    @Builder.Default
    private int totalExamsTaken = 0;

    @Column(name = "total_points", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPoints = BigDecimal.ZERO;

    @Column(name = "avg_score_percent", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal avgScorePercent = BigDecimal.ZERO;

    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdatedAt = LocalDateTime.now();
    }
}
