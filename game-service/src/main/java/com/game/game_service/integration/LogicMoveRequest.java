package com.game.game_service.integration;

import com.game.game_service.domain.Card;
import com.game.game_service.domain.PlayedCard;
import com.game.game_service.domain.Suit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogicMoveRequest {
    private String gameId;
    private String botPlayerId;
    private int botSeatIndex;
    private Suit trumpSuit;
    private List<Card> hand = new ArrayList<>();
    private List<PlayedCard> currentTrick = new ArrayList<>();
    private Integer trickNumber;
    private List<List<PlayedCard>> trickHistory = new ArrayList<>();
    private Map<Integer, Integer> remainingCardsCountBySeat;

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

    public int getBotSeatIndex() {
        return botSeatIndex;
    }

    public void setBotSeatIndex(int botSeatIndex) {
        this.botSeatIndex = botSeatIndex;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public List<PlayedCard> getCurrentTrick() {
        return currentTrick;
    }

    public void setCurrentTrick(List<PlayedCard> currentTrick) {
        this.currentTrick = currentTrick;
    }

    public Integer getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(Integer trickNumber) {
        this.trickNumber = trickNumber;
    }

    public List<List<PlayedCard>> getTrickHistory() {
        return trickHistory;
    }

    public void setTrickHistory(List<List<PlayedCard>> trickHistory) {
        this.trickHistory = trickHistory;
    }

    public Map<Integer, Integer> getRemainingCardsCountBySeat() {
        return remainingCardsCountBySeat;
    }

    public void setRemainingCardsCountBySeat(Map<Integer, Integer> remainingCardsCountBySeat) {
        this.remainingCardsCountBySeat = remainingCardsCountBySeat;
    }
}
