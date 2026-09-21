package com.examplatform.modules.leaderboard.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_monthly_leaderboard_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMonthlyLeaderboardStats {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "education_level", length = 30)
    private String educationLevel;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth; // "2026-07"

    @Column(name = "exams_taken_this_month", nullable = false)
    @Builder.Default
    private int examsTakenThisMonth = 0;

    @Column(name = "total_points_this_month", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPointsThisMonth = BigDecimal.ZERO;

    @Column(name = "avg_score_percent_this_month", nullable = false, precision = 6, scale = 2)
    @Builder.Default
    private BigDecimal avgScorePercentThisMonth = BigDecimal.ZERO;

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
