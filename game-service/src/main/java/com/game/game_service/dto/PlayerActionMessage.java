package com.game.game_service.dto;

import com.game.game_service.domain.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerActionMessage {
    private String actionType;
    private UUID gameId;
    private String playerId;
    private List<Card> cards = new ArrayList<>();

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }
}
