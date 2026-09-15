package com.examplatform.modules.challenge.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeResult extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false, unique = true)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner; // null = draw

    @Column(name = "creator_score", nullable = false)
    @Builder.Default
    private int creatorScore = 0;

    @Column(name = "opponent_score", nullable = false)
    @Builder.Default
    private int opponentScore = 0;

    @Column(name = "creator_points_earned", nullable = false)
    @Builder.Default
    private int creatorPointsEarned = 0;

    @Column(name = "opponent_points_earned", nullable = false)
    @Builder.Default
    private int opponentPointsEarned = 0;

    @Column(name = "completed_at", nullable = false)
    private java.time.LocalDateTime completedAt;

    @PrePersist
    private void setCompletedAtIfNull() {
        if (completedAt == null) {
            completedAt = java.time.LocalDateTime.now();
        }
    }
}
