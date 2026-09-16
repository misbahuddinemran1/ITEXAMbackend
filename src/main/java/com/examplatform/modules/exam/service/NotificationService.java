package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.NotificationResponse;
import com.examplatform.modules.exam.entity.UserNotification;
import com.examplatform.modules.exam.repository.NotificationGroupProjection;
import com.examplatform.modules.exam.repository.UserNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserNotificationRepository userNotificationRepository;

    public NotificationResponse getNotifications(String userId) {
        List<UserNotification> notifications = userNotificationRepository
                .findByUserIdOrderBySentAtDesc(userId);

        int unreadCount = userNotificationRepository.countByUserIdAndIsReadFalse(userId);

        List<NotificationResponse.Notification> notificationList = notifications.stream()
                .map(notif -> NotificationResponse.Notification.builder()
                        .notificationId(notif.getId())
                        .title(notif.getTitle())
                        .body(notif.getBody())
                        .type(notif.getType().name())
                        .isRead(notif.isRead())
                        .sentAt(
                                notif.getSentAt()
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toInstant()
                                        .toEpochMilli()
                        )
                        .build())
                .collect(Collectors.toList());
        return NotificationResponse.builder()
                .notifications(notificationList)
                .unreadCount(unreadCount)
                .build();
    }

    public void markAsRead(String notificationId) {
        userNotificationRepository.findById(notificationId)
                .ifPresent(notif -> {
                    notif.setRead(true);
                    userNotificationRepository.save(notif);
                });
    }

    // পুরনো মেথড — backward compatible
    public void sendNotification(String userId, UserNotification.NotificationType type,
                                 String title, String body) {
        sendNotification(userId, type, title, body, null, null);
    }

    public void sendNotification(String userId, UserNotification.NotificationType type,
                                 String title, String body, LocalDateTime expiryDate) {
        sendNotification(userId, type, title, body, expiryDate, null);
    }

    public void sendNotification(String userId, UserNotification.NotificationType type,
                                 String title, String body, LocalDateTime expiryDate, String batchId) {
        UserNotification notification = UserNotification.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .deliveryChannel(UserNotification.DeliveryChannel.IN_APP)
                .expiryDate(expiryDate)
                .batchId(batchId)
                .build();

        userNotificationRepository.save(notification);
    }

    public List<NotificationGroupProjection> getGroupedNotifications() {
        return userNotificationRepository.findGroupedNotifications();
    }

    public void deleteBatch(String batchKey) {
        userNotificationRepository.deleteByBatchKey(batchKey);
    }

    public List<UserNotification> getAllNotifications() {
        return userNotificationRepository.findAllOrderBySentAtDesc();
    }
}
