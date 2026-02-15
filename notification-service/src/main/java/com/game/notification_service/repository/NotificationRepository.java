package com.game.notification_service.repository;

import com.game.notification_service.model.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationRecord, UUID> {
    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<NotificationRecord> findByNotificationIdAndUserId(UUID notificationId, String userId);
}
