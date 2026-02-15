package com.game.notification_service.service;

import com.game.notification_service.dto.CreateInviteNotificationRequest;
import com.game.notification_service.dto.NotificationView;
import com.game.notification_service.exception.NotFoundException;
import com.game.notification_service.model.NotificationRecord;
import com.game.notification_service.model.NotificationType;
import com.game.notification_service.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional
    public NotificationView createInviteNotification(CreateInviteNotificationRequest request) {
        NotificationRecord record = new NotificationRecord();
        record.setNotificationId(UUID.randomUUID());
        record.setUserId(request.getUserId());
        record.setType(NotificationType.GAME_INVITE);
        record.setTitle("Game Invite");
        record.setMessage(request.getHostDisplayName() + " invited you to an Omi game");
        record.setLobbyId(request.getLobbyId());
        record.setRead(false);
        record.setCreatedAt(Instant.now());

        return mapper.toView(notificationRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<NotificationView> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toView)
                .toList();
    }

    @Transactional
    public NotificationView markAsRead(UUID notificationId, String userId) {
        NotificationRecord record = notificationRepository.findByNotificationIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + notificationId));

        record.setRead(true);
        record.setReadAt(Instant.now());
        return mapper.toView(notificationRepository.save(record));
    }
}
