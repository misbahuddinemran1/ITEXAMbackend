package com.examplatform.modules.challenge.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private Mode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent_id")
    private User opponent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "question_count", nullable = false)
    @Builder.Default
    private int questionCount = 10;

    @Column(name = "expires_at")
    private java.time.LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    public enum Mode { FRIEND, RANDOM }

    public enum Status { PENDING, ACTIVE, COMPLETED, EXPIRED, DECLINED }
}
