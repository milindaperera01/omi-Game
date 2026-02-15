package com.game.game_service.dto;

import com.game.game_service.domain.PlayedCard;
import com.game.game_service.domain.Team;

import java.util.ArrayList;
import java.util.List;

public class TrickSummaryView {
    private int trickNumber;
    private String winnerPlayerId;
    private Team winnerTeam;
    private List<PlayedCard> playedCards = new ArrayList<>();

    public int getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(int trickNumber) {
        this.trickNumber = trickNumber;
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

    public List<PlayedCard> getPlayedCards() {
        return playedCards;
    }

    public void setPlayedCards(List<PlayedCard> playedCards) {
        this.playedCards = playedCards;
    }
}
