package com.example.logicservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public class ChooseMoveRequest {
    @NotBlank
    private String gameId;

    @NotBlank
    private String botPlayerId;

    @Min(0)
    @Max(3)
    private int botSeatIndex;

    @NotNull
    private Suit trumpSuit;

    @NotNull
    @Size(min = 1, max = 8)
    @Valid
    private List<CardDto> hand;

    @NotNull
    @Size(max = 3)
    @Valid
    private List<PlayedCardDto> currentTrick;

    private Integer trickNumber;

    @Valid
    private List<List<PlayedCardDto>> trickHistory;

    @Valid
    private TeamInfoDto teamInfo;

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

    public List<CardDto> getHand() {
        return hand;
    }

    public void setHand(List<CardDto> hand) {
        this.hand = hand;
    }

    public List<PlayedCardDto> getCurrentTrick() {
        return currentTrick;
    }

    public void setCurrentTrick(List<PlayedCardDto> currentTrick) {
        this.currentTrick = currentTrick;
    }

    public Integer getTrickNumber() {
        return trickNumber;
    }

    public void setTrickNumber(Integer trickNumber) {
        this.trickNumber = trickNumber;
    }

    public List<List<PlayedCardDto>> getTrickHistory() {
        return trickHistory;
    }

    public void setTrickHistory(List<List<PlayedCardDto>> trickHistory) {
        this.trickHistory = trickHistory;
    }

    public TeamInfoDto getTeamInfo() {
        return teamInfo;
    }

    public void setTeamInfo(TeamInfoDto teamInfo) {
        this.teamInfo = teamInfo;
    }

    public Map<Integer, Integer> getRemainingCardsCountBySeat() {
        return remainingCardsCountBySeat;
    }

    public void setRemainingCardsCountBySeat(Map<Integer, Integer> remainingCardsCountBySeat) {
        this.remainingCardsCountBySeat = remainingCardsCountBySeat;
    }
}
