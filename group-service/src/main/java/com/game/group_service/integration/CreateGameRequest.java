package com.game.group_service.integration;

import java.util.ArrayList;
import java.util.List;

public class CreateGameRequest {
    private String dealerPlayerId;
    private List<CreateGamePlayer> players = new ArrayList<>();

    public String getDealerPlayerId() {
        return dealerPlayerId;
    }

    public void setDealerPlayerId(String dealerPlayerId) {
        this.dealerPlayerId = dealerPlayerId;
    }

    public List<CreateGamePlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<CreateGamePlayer> players) {
        this.players = players;
    }
}
