package com.examplatform.modules.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications", indexes = {
        @Index(name = "idx_notif_user", columnList = "user_id"),
        @Index(name = "idx_notif_read", columnList = "user_id,is_read"),
        @Index(name = "idx_notif_type", columnList = "type"),
        @Index(name = "idx_notif_batch", columnList = "batch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "title")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "metadata", columnDefinition = "JSONB")
private String metadata;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "delivery_channel")
    @Enumerated(EnumType.STRING)
    private DeliveryChannel deliveryChannel;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "batch_id", length = 36)
    private String batchId;

    public enum NotificationType {
        EXAM_REMINDER,
        WEAK_TOPIC_ALERT,
        NEW_EXAM_AVAILABLE,
        STREAK_BROKEN,
        STREAK_MILESTONE,
        RANK_CHANGED,
        EXAM_RESULT,
        SUBSCRIPTION_EXPIRING,
        SUBSCRIPTION_EXPIRED,
        PAYMENT_SUCCESS,
        BATTLE_INVITE,
        BATTLE_RESULT,
        BADGE_EARNED,
        SYSTEM
    }

    public enum DeliveryChannel {
        IN_APP,
        EMAIL,
        SMS,
        PUSH
    }

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}