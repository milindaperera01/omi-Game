package com.example.logicservice.service;

import com.example.logicservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrumpStrategyTest {

    private TrumpStrategy trumpStrategy;

    @BeforeEach
    void setUp() {
        trumpStrategy = new TrumpStrategy(new RulesHelper());
    }

    @Test
    void choosesExpectedSuitForKnownCards() {
        ChooseTrumpRequest request = new ChooseTrumpRequest();
        request.setBotPlayerId("bot-1");
        request.setSeatIndex(1);
        request.setFirstFourCards(List.of(
                card(Suit.HEARTS, Rank.ACE),
                card(Suit.HEARTS, Rank.KING),
                card(Suit.SPADES, Rank.SEVEN),
                card(Suit.CLUBS, Rank.SEVEN)
        ));

        ChooseTrumpResponse response = trumpStrategy.chooseTrump(request);
        assertEquals(Suit.HEARTS, response.getChosenSuit());
    }

    private CardDto card(Suit suit, Rank rank) {
        return new CardDto(suit, rank);
    }
}
