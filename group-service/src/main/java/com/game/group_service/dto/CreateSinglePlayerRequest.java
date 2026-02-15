package com.game.group_service.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSinglePlayerRequest {
    @NotBlank
    private String playerId;

    @NotBlank
    private String displayName;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
