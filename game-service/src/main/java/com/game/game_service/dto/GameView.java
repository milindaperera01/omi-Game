package com.game.game_service.dto;

import com.game.game_service.domain.GameStatus;
import com.game.game_service.domain.PlayedCard;
import com.game.game_service.domain.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameView {
    private UUID gameId;
    private GameStatus status;
    private Suit trumpSuit;
    private String dealerPlayerId;
    private String trumpChooserPlayerId;
    private String currentTurnPlayerId;
    private String leaderPlayerId;
    private int currentTrickNumber;
    private List<PlayedCard> currentTrick = new ArrayList<>();
    private int handTricksRed;
    private int handTricksBlue;
    private int matchHandsRed;
    private int matchHandsBlue;
    private List<PlayerView> players = new ArrayList<>();
    private List<TrickSummaryView> trickHistory = new ArrayList<>();

    public UUID getGameId() {
        return gameId;
    }

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public String getDealerPlayerId() {
        return dealerPlayerId;
    }

    public void setDealerPlayerId(String dealerPlayerId) {
        this.dealerPlayerId = dealerPlayerId;
    }

    public String getTrumpChooserPlayerId() {
        return trumpChooserPlayerId;
    }

    public void setTrumpChooserPlayerId(String trumpChooserPlayerId) {
        this.trumpChooserPlayerId = trumpChooserPlayerId;
    }

    public String getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(String currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public String getLeaderPlayerId() {
        return leaderPlayerId;
    }

    public void setLeaderPlayerId(String leaderPlayerId) {
        this.leaderPlayerId = leaderPlayerId;
    }

    public int getCurrentTrickNumber() {
        return currentTrickNumber;
    }

    public void setCurrentTrickNumber(int currentTrickNumber) {
        this.currentTrickNumber = currentTrickNumber;
    }

    public List<PlayedCard> getCurrentTrick() {
        return currentTrick;
    }

    public void setCurrentTrick(List<PlayedCard> currentTrick) {
        this.currentTrick = currentTrick;
    }

    public int getHandTricksRed() {
        return handTricksRed;
    }

    public void setHandTricksRed(int handTricksRed) {
        this.handTricksRed = handTricksRed;
    }

    public int getHandTricksBlue() {
        return handTricksBlue;
    }

    public void setHandTricksBlue(int handTricksBlue) {
        this.handTricksBlue = handTricksBlue;
    }

    public int getMatchHandsRed() {
        return matchHandsRed;
    }

    public void setMatchHandsRed(int matchHandsRed) {
        this.matchHandsRed = matchHandsRed;
    }

    public int getMatchHandsBlue() {
        return matchHandsBlue;
    }

    public void setMatchHandsBlue(int matchHandsBlue) {
        this.matchHandsBlue = matchHandsBlue;
    }

    public List<PlayerView> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerView> players) {
        this.players = players;
    }

    public List<TrickSummaryView> getTrickHistory() {
        return trickHistory;
    }

    public void setTrickHistory(List<TrickSummaryView> trickHistory) {
        this.trickHistory = trickHistory;
    }
}
