package com.game.game_service.integration;

import com.game.game_service.domain.Card;

public class LogicMoveResponse {
    private String gameId;
    private String botPlayerId;
    private Card chosenCard;
    private String reason;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBotPlayerId() {
        return botPlayerId;
    }

    public void setBotPlayerId(String botPlayerId) {
        this.botPlayerId = botPlayerId;
    }

    public Card getChosenCard() {
        return chosenCard;
    }

    public void setChosenCard(Card chosenCard) {
        this.chosenCard = chosenCard;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
