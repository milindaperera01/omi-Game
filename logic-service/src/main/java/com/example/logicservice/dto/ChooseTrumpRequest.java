package com.example.logicservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ChooseTrumpRequest {
    @NotBlank
    private String botPlayerId;

    @Min(0)
    @Max(3)
    private int seatIndex;

    @NotNull
    @Size(min = 4, max = 4)
    @Valid
    private List<CardDto> firstFourCards;

    @Min(0)
    @Max(3)
    private Integer dealerSeatIndex;

    @Min(0)
    @Max(3)
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

    public List<CardDto> getFirstFourCards() {
        return firstFourCards;
    }

    public void setFirstFourCards(List<CardDto> firstFourCards) {
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
