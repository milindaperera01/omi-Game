package com.example.logicservice.service;

import com.example.logicservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveStrategyTest {

    private MoveStrategy moveStrategy;

    @BeforeEach
    void setUp() {
        moveStrategy = new MoveStrategy(new RulesHelper());
    }

    @Test
    void chooseMoveReturnsCardInHandAndLegal() {
        ChooseMoveRequest request = new ChooseMoveRequest();
        request.setGameId("f19d93db-7792-43ba-9722-f68d3968f2d9");
        request.setBotPlayerId("bot-1");
        request.setBotSeatIndex(1);
        request.setTrumpSuit(Suit.SPADES);
        request.setHand(List.of(
                new CardDto(Suit.HEARTS, Rank.KING),
                new CardDto(Suit.CLUBS, Rank.ACE),
                new CardDto(Suit.DIAMONDS, Rank.SEVEN)
        ));
        request.setCurrentTrick(List.of(
                played(0, Suit.HEARTS, Rank.QUEEN)
        ));

        ChooseMoveResponse response = moveStrategy.chooseMove(request);

        assertNotNull(response.getChosenCard());
        assertTrue(request.getHand().contains(response.getChosenCard()));
        assertEquals(Suit.HEARTS, response.getChosenCard().getSuit());
    }

    @Test
    void whenTeammateWinningBotDumpsLowestLegal() {
        ChooseMoveRequest request = new ChooseMoveRequest();
        request.setGameId("f19d93db-7792-43ba-9722-f68d3968f2d9");
        request.setBotPlayerId("bot-2");
        request.setBotSeatIndex(2);
        request.setTrumpSuit(Suit.SPADES);
        request.setHand(List.of(
                new CardDto(Suit.HEARTS, Rank.ACE),
                new CardDto(Suit.HEARTS, Rank.SEVEN),
                new CardDto(Suit.CLUBS, Rank.KING)
        ));
        request.setCurrentTrick(List.of(
                played(0, Suit.HEARTS, Rank.JACK),
                played(1, Suit.HEARTS, Rank.KING)
        ));

        TeamInfoDto team = new TeamInfoDto();
        team.setBotSeatIndex(2);
        team.setTeammateSeatIndex(0);
        request.setTeamInfo(team);

        ChooseMoveResponse response = moveStrategy.chooseMove(request);
        assertEquals(Suit.HEARTS, response.getChosenCard().getSuit());
        assertEquals(Rank.SEVEN, response.getChosenCard().getRank());
    }

    @Test
    void whenCannotFollowAndTeammateWinningBotAvoidsTrumping() {
        ChooseMoveRequest request = new ChooseMoveRequest();
        request.setGameId("f19d93db-7792-43ba-9722-f68d3968f2d9");
        request.setBotPlayerId("bot-2");
        request.setBotSeatIndex(2);
        request.setTrumpSuit(Suit.SPADES);
        request.setHand(List.of(
                new CardDto(Suit.SPADES, Rank.ACE),
                new CardDto(Suit.CLUBS, Rank.SEVEN)
        ));
        request.setCurrentTrick(List.of(
                played(0, Suit.HEARTS, Rank.ACE)
        ));

        TeamInfoDto team = new TeamInfoDto();
        team.setBotSeatIndex(2);
        team.setTeammateSeatIndex(0);
        request.setTeamInfo(team);

        ChooseMoveResponse response = moveStrategy.chooseMove(request);
        assertEquals(Suit.CLUBS, response.getChosenCard().getSuit());
        assertEquals(Rank.SEVEN, response.getChosenCard().getRank());
    }

    private PlayedCardDto played(int seat, Suit suit, Rank rank) {
        PlayedCardDto p = new PlayedCardDto();
        p.setSeatIndex(seat);
        p.setCard(new CardDto(suit, rank));
        return p;
    }
}
