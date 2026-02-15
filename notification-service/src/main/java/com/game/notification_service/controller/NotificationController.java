package com.game.notification_service.controller;

import com.game.notification_service.dto.CreateInviteNotificationRequest;
import com.game.notification_service.dto.MarkReadRequest;
import com.game.notification_service.dto.NotificationView;
import com.game.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/invites")
    public NotificationView createInvite(@Valid @RequestBody CreateInviteNotificationRequest request) {
        return notificationService.createInviteNotification(request);
    }

    @GetMapping("/users/{userId}")
    public List<NotificationView> getUserNotifications(@PathVariable String userId) {
        return notificationService.getUserNotifications(userId);
    }

    @PostMapping("/{notificationId}/read")
    public NotificationView markRead(@PathVariable UUID notificationId,
                                     @Valid @RequestBody MarkReadRequest request) {
        return notificationService.markAsRead(notificationId, request.getUserId());
    }
}
