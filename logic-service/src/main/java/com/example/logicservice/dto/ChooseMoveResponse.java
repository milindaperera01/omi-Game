package com.example.logicservice.dto;

public class ChooseMoveResponse {
    private String gameId;
    private String botPlayerId;
    private CardDto chosenCard;
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

    public CardDto getChosenCard() {
        return chosenCard;
    }

    public void setChosenCard(CardDto chosenCard) {
        this.chosenCard = chosenCard;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
