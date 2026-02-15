package com.game.game_service.domain;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState implements Serializable {
    private UUID gameId;
    private GameStatus status;
    private List<PlayerState> players = new ArrayList<>();
    private String dealerPlayerId;
    private String trumpChooserPlayerId;
    private Suit trumpSuit;
    private String leaderPlayerId;
    private String currentTurnPlayerId;
    private int currentTrickNumber;
    private List<PlayedCard> currentTrick = new ArrayList<>();
    private List<TrickRecord> trickHistory = new ArrayList<>();
    private int handTricksRed;
    private int handTricksBlue;
    private int matchHandsRed;
    private int matchHandsBlue;
    private List<Card> deck = new ArrayList<>();
    private Instant createdAt;
    private Instant updatedAt;

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

    public List<PlayerState> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerState> players) {
        this.players = players;
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

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public String getLeaderPlayerId() {
        return leaderPlayerId;
    }

    public void setLeaderPlayerId(String leaderPlayerId) {
        this.leaderPlayerId = leaderPlayerId;
    }

    public String getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(String currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
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

    public List<TrickRecord> getTrickHistory() {
        return trickHistory;
    }

    public void setTrickHistory(List<TrickRecord> trickHistory) {
        this.trickHistory = trickHistory;
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

    public List<Card> getDeck() {
        return deck;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
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
