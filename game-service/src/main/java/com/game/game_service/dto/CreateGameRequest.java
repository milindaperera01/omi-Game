package com.game.game_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateGameRequest {
    @NotBlank
    private String dealerPlayerId;

    @NotNull
    @Size(min = 4, max = 4)
    @Valid
    private List<PlayerInput> players;

    public String getDealerPlayerId() {
        return dealerPlayerId;
    }

    public void setDealerPlayerId(String dealerPlayerId) {
        this.dealerPlayerId = dealerPlayerId;
    }

    public List<PlayerInput> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerInput> players) {
        this.players = players;
    }
}
