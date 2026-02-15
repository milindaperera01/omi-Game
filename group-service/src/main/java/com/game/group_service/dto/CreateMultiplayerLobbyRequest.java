package com.game.group_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateMultiplayerLobbyRequest {
    @NotBlank
    private String hostPlayerId;

    @NotBlank
    private String hostDisplayName;

    @NotNull
    @Size(min = 3, max = 3)
    private List<@NotBlank String> invitedPlayerIds;

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }

    public String getHostDisplayName() {
        return hostDisplayName;
    }

    public void setHostDisplayName(String hostDisplayName) {
        this.hostDisplayName = hostDisplayName;
    }

    public List<String> getInvitedPlayerIds() {
        return invitedPlayerIds;
    }

    public void setInvitedPlayerIds(List<String> invitedPlayerIds) {
        this.invitedPlayerIds = invitedPlayerIds;
    }
}
