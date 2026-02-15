package com.game.game_service.integration;

import com.game.game_service.domain.Suit;

public class LogicTrumpResponse {
    private Suit chosenSuit;

    public Suit getChosenSuit() {
        return chosenSuit;
    }

    public void setChosenSuit(Suit chosenSuit) {
        this.chosenSuit = chosenSuit;
    }
}
