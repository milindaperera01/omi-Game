package com.game.game_service.dto;

import com.game.game_service.domain.Card;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlayCardRequest {
    @NotBlank
    private String playerId;

    @NotNull
    @Valid
    private Card card;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
