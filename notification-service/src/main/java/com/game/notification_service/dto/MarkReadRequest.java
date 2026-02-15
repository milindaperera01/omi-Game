package com.game.notification_service.dto;

import jakarta.validation.constraints.NotBlank;

public class MarkReadRequest {
    @NotBlank
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
