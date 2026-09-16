package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.UserNotification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, String> {

    List<UserNotification> findByUserIdOrderBySentAtDesc(String userId);

    List<UserNotification> findByUserIdAndIsReadFalseOrderBySentAtDesc(String userId);

    int countByUserIdAndIsReadFalse(String userId);

    void deleteByUserIdAndIsReadTrue(String userId);

    @Query("SELECT n FROM UserNotification n ORDER BY n.sentAt DESC")
    List<UserNotification> findAllOrderBySentAtDesc();

    @Query(value = "SELECT COALESCE(batch_id, id) as batchKey, MIN(id) as sampleId, " +
            "MAX(title) as title, MAX(body) as body, MAX(type) as type, " +
            "MAX(sent_at) as sentAt, MAX(expiry_date) as expiryDate, COUNT(*) as recipientCount " +
            "FROM user_notifications " +
            "GROUP BY COALESCE(batch_id, id) " +
            "ORDER BY MAX(sent_at) DESC", nativeQuery = true)
    List<NotificationGroupProjection> findGroupedNotifications();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_notifications WHERE COALESCE(batch_id, id) = ?1", nativeQuery = true)
    void deleteByBatchKey(String batchKey);

}
