package com.example.logicservice.service;

import com.example.logicservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RulesHelperTest {

    private RulesHelper rulesHelper;

    @BeforeEach
    void setUp() {
        rulesHelper = new RulesHelper();
    }

    @Test
    void legalMoveEnforcesFollowSuit() {
        List<CardDto> hand = List.of(
                new CardDto(Suit.HEARTS, Rank.ACE),
                new CardDto(Suit.CLUBS, Rank.KING)
        );

        List<CardDto> legal = rulesHelper.legalMoves(hand, Suit.HEARTS);
        assertEquals(1, legal.size());
        assertEquals(Suit.HEARTS, legal.getFirst().getSuit());
    }

    @Test
    void winnerEvaluationRespectsTrump() {
        List<PlayedCardDto> trick = List.of(
                played(0, Suit.CLUBS, Rank.ACE),
                played(1, Suit.HEARTS, Rank.SEVEN),
                played(2, Suit.CLUBS, Rank.KING)
        );

        PlayedCardDto winner = rulesHelper.currentWinningCard(trick, Suit.HEARTS);
        assertEquals(Suit.HEARTS, winner.getCard().getSuit());
        assertEquals(Rank.SEVEN, winner.getCard().getRank());
    }

    private PlayedCardDto played(int seat, Suit suit, Rank rank) {
        PlayedCardDto p = new PlayedCardDto();
        p.setSeatIndex(seat);
        p.setCard(new CardDto(suit, rank));
        return p;
    }
}
