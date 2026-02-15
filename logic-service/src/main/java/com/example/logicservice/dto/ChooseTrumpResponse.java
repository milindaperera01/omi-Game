package com.example.logicservice.dto;

public class ChooseTrumpResponse {
    private String botPlayerId;
    private Suit chosenSuit;
    private String reason;

    public String getBotPlayerId() {
        return botPlayerId;
    }

    public void setBotPlayerId(String botPlayerId) {
        this.botPlayerId = botPlayerId;
    }

    public Suit getChosenSuit() {
        return chosenSuit;
    }

    public void setChosenSuit(Suit chosenSuit) {
        this.chosenSuit = chosenSuit;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
