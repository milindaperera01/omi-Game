package com.game.game_service.domain;

import java.io.Serializable;

public class PlayedCard implements Serializable {
    private String playerId;
    private int seatIndex;
    private Card card;

    public PlayedCard() {
    }

    public PlayedCard(String playerId, int seatIndex, Card card) {
        this.playerId = playerId;
        this.seatIndex = seatIndex;
        this.card = card;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
