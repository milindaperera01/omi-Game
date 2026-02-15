package com.game.notification_service.service;

import com.game.notification_service.dto.NotificationView;
import com.game.notification_service.model.NotificationRecord;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationView toView(NotificationRecord n) {
        NotificationView v = new NotificationView();
        v.setNotificationId(n.getNotificationId());
        v.setUserId(n.getUserId());
        v.setType(n.getType());
        v.setTitle(n.getTitle());
        v.setMessage(n.getMessage());
        v.setLobbyId(n.getLobbyId());
        v.setRead(n.isRead());
        v.setCreatedAt(n.getCreatedAt());
        v.setReadAt(n.getReadAt());
        return v;
    }
}
