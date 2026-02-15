package com.game.game_service.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrickRecord implements Serializable {
    private int trickNumber;
    private List<PlayedCard> playedCards = new ArrayList<>();
    private String winnerPlayerId;
    private Team winnerTeam;
    private Suit leadSuit;
    private Suit trumpSuit;

    public int getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(int trickNumber) {
        this.trickNumber = trickNumber;
    }

    public List<PlayedCard> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(List<PlayedCard> playedCards) {
        this.playedCards = playedCards;
    }

    public String getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public void setWinnerPlayerId(String winnerPlayerId) {
        this.winnerPlayerId = winnerPlayerId;
    }

    public Team getWinnerTeam() {
        return winnerTeam;
    }

    public void setWinnerTeam(Team winnerTeam) {
        this.winnerTeam = winnerTeam;
    }

    public Suit getLeadSuit() {
        return leadSuit;
    }

    public void setLeadSuit(Suit leadSuit) {
        this.leadSuit = leadSuit;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }
}
