package com.game.game_service.integration;

import com.game.game_service.domain.Card;

import java.util.ArrayList;
import java.util.List;

public class LogicTrumpRequest {
    private String botPlayerId;
    private int seatIndex;
    private List<Card> firstFourCards = new ArrayList<>();
    private Integer dealerSeatIndex;
    private Integer teammateSeatIndex;

    public String getBotPlayerId() {
        return botPlayerId;
    }

    public void setBotPlayerId(String botPlayerId) {
        this.botPlayerId = botPlayerId;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public List<Card> getFirstFourCards() {
        return firstFourCards;
    }

    public void setFirstFourCards(List<Card> firstFourCards) {
        this.firstFourCards = firstFourCards;
    }

    public Integer getDealerSeatIndex() {
        return dealerSeatIndex;
    }

    public void setDealerSeatIndex(Integer dealerSeatIndex) {
        this.dealerSeatIndex = dealerSeatIndex;
    }

    public Integer getTeammateSeatIndex() {
        return teammateSeatIndex;
    }

    public void setTeammateSeatIndex(Integer teammateSeatIndex) {
        this.teammateSeatIndex = teammateSeatIndex;
    }
}
