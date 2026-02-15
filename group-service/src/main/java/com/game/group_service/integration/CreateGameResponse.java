package com.game.group_service.integration;

import java.util.UUID;

public class CreateGameResponse {
    private UUID gameId;

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }
}
