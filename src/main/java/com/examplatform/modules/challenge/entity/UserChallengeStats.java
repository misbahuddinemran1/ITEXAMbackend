package com.examplatform.modules.challenge.entity;

import com.examplatform.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "user_challenge_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeStats implements Persistable<String> {

    @Id
    @Column(name = "user_id", length = 36)
    private String userId;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private int totalPoints = 0;

    @Column(name = "total_wins", nullable = false)
    @Builder.Default
    private int totalWins = 0;

    @Column(name = "total_losses", nullable = false)
    @Builder.Default
    private int totalLosses = 0;

    @Column(name = "total_draws", nullable = false)
    @Builder.Default
    private int totalDraws = 0;

    @Column(name = "total_played", nullable = false)
    @Builder.Default
    private int totalPlayed = 0;

    @Column(name = "current_win_streak", nullable = false)
    @Builder.Default
    private int currentWinStreak = 0;

    @Column(name = "best_win_streak", nullable = false)
    @Builder.Default
    private int bestWinStreak = 0;

    @Column(name = "updated_at", nullable = false)
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void touch() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
