package com.example.logicservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.List;

public class TeamInfoDto {
    @Min(0)
    @Max(3)
    private int botSeatIndex;

    @Min(0)
    @Max(3)
    private int teammateSeatIndex;

    @Size(min = 2, max = 2)
    private List<Integer> redSeats;

    @Size(min = 2, max = 2)
    private List<Integer> blueSeats;

    public int getBotSeatIndex() {
        return botSeatIndex;
    }

    public void setBotSeatIndex(int botSeatIndex) {
        this.botSeatIndex = botSeatIndex;
    }

    public int getTeammateSeatIndex() {
        return teammateSeatIndex;
    }

    public void setTeammateSeatIndex(int teammateSeatIndex) {
        this.teammateSeatIndex = teammateSeatIndex;
    }

    public List<Integer> getRedSeats() {
        return redSeats;
    }

    public void setRedSeats(List<Integer> redSeats) {
        this.redSeats = redSeats;
    }

    public List<Integer> getBlueSeats() {
        return blueSeats;
    }

    public void setBlueSeats(List<Integer> blueSeats) {
        this.blueSeats = blueSeats;
    }
}
