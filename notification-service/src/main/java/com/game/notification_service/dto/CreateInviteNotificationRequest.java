package com.game.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateInviteNotificationRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String lobbyId;

    @NotBlank
    private String hostDisplayName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(String lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getHostDisplayName() {
        return hostDisplayName;
    }

    public void setHostDisplayName(String hostDisplayName) {
        this.hostDisplayName = hostDisplayName;
    }
}
