package com.game.game_service.service;

import com.game.game_service.domain.*;
import com.game.game_service.dto.CreateGameRequest;
import com.game.game_service.dto.PlayCardRequest;
import com.game.game_service.dto.PlayerInput;
import com.game.game_service.dto.TrumpRequest;
import com.game.game_service.exception.BadRequestException;
import com.game.game_service.integration.LogicServiceClient;
import com.game.game_service.repository.InMemoryGameStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GameServiceRulesTest {

    private InMemoryGameStateRepository repository;
    private GameService gameService;
    private LogicServiceClient logicServiceClient;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGameStateRepository();
        logicServiceClient = Mockito.mock(LogicServiceClient.class);
        Mockito.when(logicServiceClient.chooseTrump(Mockito.any(), Mockito.any(), Mockito.anyList()))
                .thenReturn(Optional.empty());
        gameService = new GameService(repository, Mockito.mock(GameBroadcastService.class), logicServiceClient);
    }

    @Test
    void deckHas32UniqueCardsOnCreate() {
        GameState state = gameService.createGame(defaultCreateRequest());

        List<Card> allCards = new ArrayList<>();
        state.getPlayers().forEach(p -> allCards.addAll(p.getHand()));
        allCards.addAll(state.getDeck());

        assertEquals(32, allCards.size());
        assertEquals(32, new HashSet<>(allCards).size());
    }

    @Test
    void dealingGives4Then8CardsPerPlayer() {
        GameState created = gameService.createGame(defaultCreateRequest());
        created.getPlayers().forEach(p -> assertEquals(4, p.getHand().size()));
        assertEquals(GameStatus.WAITING_TRUMP, created.getStatus());

        TrumpRequest trumpRequest = new TrumpRequest();
        trumpRequest.setChooserPlayerId(created.getTrumpChooserPlayerId());
        trumpRequest.setTrumpSuit(Suit.HEARTS);

        GameState started = gameService.chooseTrump(created.getGameId(), trumpRequest);

        started.getPlayers().forEach(p -> assertEquals(8, p.getHand().size()));
        assertEquals(0, started.getDeck().size());
        assertEquals(GameStatus.IN_PROGRESS, started.getStatus());
    }

    @Test
    void chooseTrumpOnlyAllowedInWaitingTrump() {
        GameState created = gameService.createGame(defaultCreateRequest());

        TrumpRequest trumpRequest = new TrumpRequest();
        trumpRequest.setChooserPlayerId(created.getTrumpChooserPlayerId());
        trumpRequest.setTrumpSuit(Suit.SPADES);

        gameService.chooseTrump(created.getGameId(), trumpRequest);

        assertThrows(BadRequestException.class, () -> gameService.chooseTrump(created.getGameId(), trumpRequest));
    }

    @Test
    void followSuitValidationWorks() {
        GameState game = customBaseGame();
        game.setTrumpSuit(Suit.SPADES);
        game.setCurrentTurnPlayerId("p0");
        game.setLeaderPlayerId("p0");
        game.getPlayers().get(0).setHand(new ArrayList<>(List.of(
                new Card(Suit.CLUBS, Rank.ACE),
                new Card(Suit.HEARTS, Rank.SEVEN)
        )));
        game.getPlayers().get(1).setHand(new ArrayList<>(List.of(
                new Card(Suit.CLUBS, Rank.KING),
                new Card(Suit.DIAMONDS, Rank.ACE)
        )));
        game.getPlayers().get(2).setHand(new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.ACE))));
        game.getPlayers().get(3).setHand(new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.KING))));

        gameService.getGame(game.getGameId());

        PlayCardRequest lead = new PlayCardRequest();
        lead.setPlayerId("p0");
        lead.setCard(new Card(Suit.CLUBS, Rank.ACE));
        gameService.playCard(game.getGameId(), lead);

        PlayCardRequest illegal = new PlayCardRequest();
        illegal.setPlayerId("p1");
        illegal.setCard(new Card(Suit.DIAMONDS, Rank.ACE));

        assertThrows(BadRequestException.class, () -> gameService.playCard(game.getGameId(), illegal));
    }

    @Test
    void trickWinnerLogicWithAndWithoutTrump() {
        GameState noTrumpWin = customBaseGame();
        noTrumpWin.setTrumpSuit(Suit.SPADES);
        noTrumpWin.setCurrentTurnPlayerId("p0");
        noTrumpWin.setLeaderPlayerId("p0");
        noTrumpWin.getPlayers().get(0).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.KING))));
        noTrumpWin.getPlayers().get(1).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.ACE))));
        noTrumpWin.getPlayers().get(2).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.QUEEN))));
        noTrumpWin.getPlayers().get(3).setHand(new ArrayList<>(List.of(new Card(Suit.DIAMONDS, Rank.ACE))));

        playWholeTrick(noTrumpWin.getGameId(), List.of(
                play("p0", Suit.CLUBS, Rank.KING),
                play("p1", Suit.CLUBS, Rank.ACE),
                play("p2", Suit.CLUBS, Rank.QUEEN),
                play("p3", Suit.DIAMONDS, Rank.ACE)
        ));

        GameState resolvedNoTrump = gameService.getGame(noTrumpWin.getGameId());
        assertEquals("p1", resolvedNoTrump.getLeaderPlayerId());

        GameState trumpWin = customBaseGame();
        trumpWin.setTrumpSuit(Suit.HEARTS);
        trumpWin.setCurrentTurnPlayerId("p0");
        trumpWin.setLeaderPlayerId("p0");
        trumpWin.getPlayers().get(0).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.ACE))));
        trumpWin.getPlayers().get(1).setHand(new ArrayList<>(List.of(new Card(Suit.HEARTS, Rank.SEVEN))));
        trumpWin.getPlayers().get(2).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.KING))));
        trumpWin.getPlayers().get(3).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.QUEEN))));

        playWholeTrick(trumpWin.getGameId(), List.of(
                play("p0", Suit.CLUBS, Rank.ACE),
                play("p1", Suit.HEARTS, Rank.SEVEN),
                play("p2", Suit.CLUBS, Rank.KING),
                play("p3", Suit.CLUBS, Rank.QUEEN)
        ));

        GameState resolvedTrump = gameService.getGame(trumpWin.getGameId());
        assertEquals("p1", resolvedTrump.getLeaderPlayerId());
    }

    @Test
    void after8TricksHandCompleteAndTrickCountsSumTo8() {
        GameState game = customBaseGame();
        game.setTrumpSuit(Suit.HEARTS);
        game.setCurrentTurnPlayerId("p0");
        game.setLeaderPlayerId("p0");
        game.setCurrentTrickNumber(7);
        game.setHandTricksRed(4);
        game.setHandTricksBlue(3);
        game.getPlayers().get(0).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.ACE))));
        game.getPlayers().get(1).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.KING))));
        game.getPlayers().get(2).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.QUEEN))));
        game.getPlayers().get(3).setHand(new ArrayList<>(List.of(new Card(Suit.CLUBS, Rank.JACK))));

        playWholeTrick(game.getGameId(), List.of(
                play("p0", Suit.CLUBS, Rank.ACE),
                play("p1", Suit.CLUBS, Rank.KING),
                play("p2", Suit.CLUBS, Rank.QUEEN),
                play("p3", Suit.CLUBS, Rank.JACK)
        ));

        GameState resolved = gameService.getGame(game.getGameId());
        assertEquals(GameStatus.HAND_COMPLETE, resolved.getStatus());
        assertEquals(8, resolved.getHandTricksRed() + resolved.getHandTricksBlue());
    }

    private void playWholeTrick(UUID gameId, List<PlayCardRequest> plays) {
        for (PlayCardRequest play : plays) {
            gameService.playCard(gameId, play);
        }
    }

    private PlayCardRequest play(String playerId, Suit suit, Rank rank) {
        PlayCardRequest req = new PlayCardRequest();
        req.setPlayerId(playerId);
        req.setCard(new Card(suit, rank));
        return req;
    }

    private CreateGameRequest defaultCreateRequest() {
        CreateGameRequest req = new CreateGameRequest();
        req.setDealerPlayerId("p2");

        List<PlayerInput> players = new ArrayList<>();
        players.add(player("p0", "Milinda", PlayerType.HUMAN, Team.RED, 0));
        players.add(player("p1", "Bot1", PlayerType.BOT, Team.BLUE, 1));
        players.add(player("p2", "Bot2", PlayerType.BOT, Team.RED, 2));
        players.add(player("p3", "Human3", PlayerType.HUMAN, Team.BLUE, 3));
        req.setPlayers(players);

        return req;
    }

    private PlayerInput player(String id, String name, PlayerType type, Team team, int seat) {
        PlayerInput player = new PlayerInput();
        player.setPlayerId(id);
        player.setDisplayName(name);
        player.setPlayerType(type);
        player.setTeam(team);
        player.setSeatIndex(seat);
        return player;
    }

    private GameState customBaseGame() {
        GameState game = new GameState();
        game.setGameId(UUID.randomUUID());
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setDealerPlayerId("p3");
        game.setTrumpChooserPlayerId("p0");
        game.setCurrentTrickNumber(0);
        game.setDeck(new ArrayList<>());
        game.setTrickHistory(new ArrayList<>());
        game.setCurrentTrick(new ArrayList<>());
        game.setCreatedAt(Instant.now());
        game.setUpdatedAt(Instant.now());

        game.setPlayers(new ArrayList<>(List.of(
                playerState("p0", "P0", PlayerType.HUMAN, Team.RED, 0),
                playerState("p1", "P1", PlayerType.BOT, Team.BLUE, 1),
                playerState("p2", "P2", PlayerType.BOT, Team.RED, 2),
                playerState("p3", "P3", PlayerType.BOT, Team.BLUE, 3)
        )));

        repository.save(game);
        return game;
    }

    private PlayerState playerState(String id, String name, PlayerType type, Team team, int seat) {
        PlayerState player = new PlayerState();
        player.setPlayerId(id);
        player.setDisplayName(name);
        player.setPlayerType(type);
        player.setTeam(team);
        player.setSeatIndex(seat);
        player.setHand(new ArrayList<>());
        return player;
    }
}
