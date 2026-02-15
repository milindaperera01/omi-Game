package com.game.group_service.dto;

import com.game.group_service.model.LobbyMode;
import com.game.group_service.model.LobbyStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LobbyView {
    private UUID lobbyId;
    private LobbyMode mode;
    private LobbyStatus status;
    private String hostPlayerId;
    private String gameId;
    private List<LobbyPlayerView> players = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(UUID lobbyId) {
        this.lobbyId = lobbyId;
    }

    public LobbyMode getMode() {
        return mode;
    }

    public void setMode(LobbyMode mode) {
        this.mode = mode;
    }

    public LobbyStatus getStatus() {
        return status;
    }

    public void setStatus(LobbyStatus status) {
        this.status = status;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public void setHostPlayerId(String hostPlayerId) {
        this.hostPlayerId = hostPlayerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public List<LobbyPlayerView> getPlayers() {
        return players;
    }

    public void setPlayers(List<LobbyPlayerView> players) {
        this.players = players;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
