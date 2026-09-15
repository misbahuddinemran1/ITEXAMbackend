package com.examplatform.modules.challenge.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import com.examplatform.modules.guide.entity.GuidePracticeMcqOption;
import com.examplatform.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_attempt", uniqueConstraints = {
        @UniqueConstraint(name = "uq_challenge_attempt", columnNames = {"challenge_id", "user_id", "mcq_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcq_id", nullable = false)
    private GuidePracticeMcq mcq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private GuidePracticeMcqOption selectedOption;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private boolean isCorrect = false;

    @Column(name = "time_taken_ms")
    private Integer timeTakenMs;

    @Column(name = "answered_at", nullable = false)
    private java.time.LocalDateTime answeredAt;

    @PrePersist
    private void setAnsweredAtIfNull() {
        if (answeredAt == null) {
            answeredAt = java.time.LocalDateTime.now();
        }
    }
}
