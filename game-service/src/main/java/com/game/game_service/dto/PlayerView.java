package com.game.game_service.dto;

import com.game.game_service.domain.Card;
import com.game.game_service.domain.PlayerType;
import com.game.game_service.domain.Team;

import java.util.ArrayList;
import java.util.List;

public class PlayerView {
    private String playerId;
    private String displayName;
    private int seatIndex;
    private PlayerType playerType;
    private Team team;
    private List<Card> hand = new ArrayList<>();
    private Integer handCount;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(int seatIndex) {
        this.seatIndex = seatIndex;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public Integer getHandCount() {
        return handCount;
    }

    public void setHandCount(Integer handCount) {
        this.handCount = handCount;
    }
}
