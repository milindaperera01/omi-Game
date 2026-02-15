package com.game.game_service.dto;

import com.game.game_service.domain.Suit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrumpRequest {
    @NotBlank
    private String chooserPlayerId;

    @NotNull
    private Suit trumpSuit;

    public String getChooserPlayerId() {
        return chooserPlayerId;
    }

    public void setChooserPlayerId(String chooserPlayerId) {
        this.chooserPlayerId = chooserPlayerId;
    }

    public Suit getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }
}
