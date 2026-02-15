package com.game.game_service.service;

import com.game.game_service.domain.*;
import com.game.game_service.dto.CreateGameRequest;
import com.game.game_service.dto.PlayCardRequest;
import com.game.game_service.dto.PlayerInput;
import com.game.game_service.dto.TrumpRequest;
import com.game.game_service.exception.BadRequestException;
import com.game.game_service.exception.NotFoundException;
import com.game.game_service.integration.LogicServiceClient;
import com.game.game_service.repository.GameStateRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

@Service
public class GameService {

    private static final int PLAYER_COUNT = 4;
    private static final int CARDS_PER_PLAYER = 8;

    private final GameStateRepository repository;
    private final GameBroadcastService broadcastService;
    private final LogicServiceClient logicServiceClient;

    public GameService(GameStateRepository repository,
                       GameBroadcastService broadcastService,
                       LogicServiceClient logicServiceClient) {
        this.repository = repository;
        this.broadcastService = broadcastService;
        this.logicServiceClient = logicServiceClient;
    }

    public GameState createGame(CreateGameRequest request) {
        validateCreateRequest(request);

        GameState gameState = new GameState();
        gameState.setGameId(UUID.randomUUID());
        gameState.setStatus(GameStatus.WAITING_TRUMP);
        gameState.setPlayers(toPlayerStates(request.getPlayers()));
        gameState.setDealerPlayerId(request.getDealerPlayerId());
        gameState.setCurrentTrickNumber(0);
        gameState.setCreatedAt(Instant.now());
        gameState.setUpdatedAt(Instant.now());

        List<Card> deck = buildDeck();
        Collections.shuffle(deck);
        gameState.setDeck(deck);

        int dealerSeat = getPlayerById(gameState, request.getDealerPlayerId()).getSeatIndex();
        int startSeat = nextSeat(dealerSeat);

        dealCards(gameState, startSeat, 4);

        String trumpChooser = getPlayerBySeat(gameState, startSeat).getPlayerId();
        gameState.setTrumpChooserPlayerId(trumpChooser);
        gameState.setCurrentTurnPlayerId(null);
        gameState.setLeaderPlayerId(null);

        GameState saved = save(gameState);
        broadcastService.publish(saved);
        return handleTrumpChooser(saved);
    }

    public GameState chooseTrump(UUID gameId, TrumpRequest request) {
        GameState gameState = getGame(gameId);
        applyTrumpChoice(gameState, request.getChooserPlayerId(), request.getTrumpSuit());
        GameState saved = save(gameState);
        broadcastService.publish(saved);
        return maybeProcessBotTurns(saved);
    }

    private void applyTrumpChoice(GameState gameState, String chooserPlayerId, Suit trumpSuit) {

        if (gameState.getStatus() != GameStatus.WAITING_TRUMP) {
            throw new BadRequestException("Trump can only be chosen in WAITING_TRUMP status");
        }
        if (!gameState.getTrumpChooserPlayerId().equals(chooserPlayerId)) {
            throw new BadRequestException("Only trump chooser can choose trump");
        }

        gameState.setTrumpSuit(trumpSuit);

        int dealerSeat = getPlayerById(gameState, gameState.getDealerPlayerId()).getSeatIndex();
        int startSeat = nextSeat(dealerSeat);
        dealCards(gameState, startSeat, 4);

        if (!gameState.getDeck().isEmpty()) {
            throw new IllegalStateException("Deck should be empty after second deal");
        }

        gameState.setStatus(GameStatus.IN_PROGRESS);
        gameState.setLeaderPlayerId(gameState.getTrumpChooserPlayerId());
        gameState.setCurrentTurnPlayerId(gameState.getTrumpChooserPlayerId());
    }

    public GameState playCard(UUID gameId, PlayCardRequest request) {
        GameState gameState = getGame(gameId);

        if (gameState.getStatus() != GameStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only play cards when game is IN_PROGRESS");
        }

        PlayerState player = getPlayerById(gameState, request.getPlayerId());
        if (!Objects.equals(gameState.getCurrentTurnPlayerId(), player.getPlayerId())) {
            throw new BadRequestException("It is not this player's turn");
        }

        applyPlayedCard(gameState, player, request.getCard());

        GameState saved = save(gameState);
        broadcastService.publish(saved);
        return maybeProcessBotTurns(saved);
    }

    public GameState getGame(UUID gameId) {
        return repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameId));
    }

    private GameState save(GameState gameState) {
        gameState.setUpdatedAt(Instant.now());
        return repository.save(gameState);
    }

    private void resolveTrick(GameState gameState) {
        List<PlayedCard> trickCards = new ArrayList<>(gameState.getCurrentTrick());
        Suit leadSuit = trickCards.getFirst().getCard().getSuit();

        Predicate<PlayedCard> trumpFilter = pc -> gameState.getTrumpSuit() != null && pc.getCard().getSuit() == gameState.getTrumpSuit();
        List<PlayedCard> trumpCards = trickCards.stream().filter(trumpFilter).toList();

        List<PlayedCard> winnerPool = trumpCards.isEmpty()
                ? trickCards.stream().filter(pc -> pc.getCard().getSuit() == leadSuit).toList()
                : trumpCards;

        PlayedCard winnerCard = winnerPool.stream()
                .max(Comparator.comparingInt(pc -> pc.getCard().getRank().getStrength()))
                .orElseThrow();

        PlayerState winner = getPlayerById(gameState, winnerCard.getPlayerId());

        TrickRecord record = new TrickRecord();
        record.setTrickNumber(gameState.getCurrentTrickNumber() + 1);
        record.setPlayedCards(trickCards);
        record.setWinnerPlayerId(winner.getPlayerId());
        record.setWinnerTeam(winner.getTeam());
        record.setLeadSuit(leadSuit);
        record.setTrumpSuit(gameState.getTrumpSuit());

        gameState.getTrickHistory().add(record);
        gameState.getCurrentTrick().clear();

        if (winner.getTeam() == Team.RED) {
            gameState.setHandTricksRed(gameState.getHandTricksRed() + 1);
        } else {
            gameState.setHandTricksBlue(gameState.getHandTricksBlue() + 1);
        }

        gameState.setCurrentTrickNumber(gameState.getCurrentTrickNumber() + 1);

        if (gameState.getCurrentTrickNumber() == CARDS_PER_PLAYER) {
            finishHandAndPrepareNext(gameState);
            return;
        }

        gameState.setLeaderPlayerId(winner.getPlayerId());
        gameState.setCurrentTurnPlayerId(winner.getPlayerId());
    }

    private void finishHandAndPrepareNext(GameState gameState) {
        if (gameState.getHandTricksRed() > gameState.getHandTricksBlue()) {
            gameState.setMatchHandsRed(gameState.getMatchHandsRed() + 1);
        } else if (gameState.getHandTricksBlue() > gameState.getHandTricksRed()) {
            gameState.setMatchHandsBlue(gameState.getMatchHandsBlue() + 1);
        }

        int currentDealerSeat = getPlayerById(gameState, gameState.getDealerPlayerId()).getSeatIndex();
        int nextDealerSeat = nextSeat(currentDealerSeat);
        int nextTrumpChooserSeat = nextSeat(nextDealerSeat);

        gameState.setDealerPlayerId(getPlayerBySeat(gameState, nextDealerSeat).getPlayerId());
        gameState.setTrumpChooserPlayerId(getPlayerBySeat(gameState, nextTrumpChooserSeat).getPlayerId());

        gameState.setStatus(GameStatus.WAITING_TRUMP);
        gameState.setTrumpSuit(null);
        gameState.setLeaderPlayerId(null);
        gameState.setCurrentTurnPlayerId(null);
        gameState.setCurrentTrickNumber(0);
        gameState.getCurrentTrick().clear();
        gameState.getTrickHistory().clear();
        gameState.setHandTricksRed(0);
        gameState.setHandTricksBlue(0);

        for (PlayerState player : gameState.getPlayers()) {
            player.getHand().clear();
        }

        List<Card> deck = buildDeck();
        Collections.shuffle(deck);
        gameState.setDeck(deck);

        dealCards(gameState, nextTrumpChooserSeat, 4);
    }

    private void validateFollowSuit(GameState gameState, PlayerState player, Card playedCard) {
        if (gameState.getCurrentTrick().isEmpty()) {
            return;
        }

        Suit leadSuit = gameState.getCurrentTrick().getFirst().getCard().getSuit();
        if (playedCard.getSuit() == leadSuit) {
            return;
        }

        boolean hasLeadSuit = player.getHand().stream().anyMatch(card -> card.getSuit() == leadSuit);
        if (hasLeadSuit) {
            throw new BadRequestException("Player must follow lead suit");
        }
    }

    private Optional<Card> findAndRemoveCard(PlayerState player, Card requestedCard) {
        for (int i = 0; i < player.getHand().size(); i++) {
            Card card = player.getHand().get(i);
            if (card.equals(requestedCard)) {
                player.getHand().remove(i);
                return Optional.of(card);
            }
        }
        return Optional.empty();
    }

    private void validateCreateRequest(CreateGameRequest request) {
        Set<String> playerIds = new HashSet<>();
        Set<Integer> seats = new HashSet<>();

        for (PlayerInput player : request.getPlayers()) {
            if (!playerIds.add(player.getPlayerId())) {
                throw new BadRequestException("Duplicate playerId: " + player.getPlayerId());
            }
            if (!seats.add(player.getSeatIndex())) {
                throw new BadRequestException("Duplicate seatIndex: " + player.getSeatIndex());
            }
        }

        for (int seat = 0; seat < PLAYER_COUNT; seat++) {
            if (!seats.contains(seat)) {
                throw new BadRequestException("Missing seatIndex: " + seat);
            }
        }

        boolean dealerExists = request.getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(request.getDealerPlayerId()));
        if (!dealerExists) {
            throw new BadRequestException("dealerPlayerId not present in players");
        }

        PlayerInput s0 = bySeat(request.getPlayers(), 0);
        PlayerInput s1 = bySeat(request.getPlayers(), 1);
        PlayerInput s2 = bySeat(request.getPlayers(), 2);
        PlayerInput s3 = bySeat(request.getPlayers(), 3);
        if (s0.getTeam() != s2.getTeam() || s1.getTeam() != s3.getTeam() || s0.getTeam() == s1.getTeam()) {
            throw new BadRequestException("Teams must satisfy seat0+seat2 vs seat1+seat3");
        }
    }

    private PlayerInput bySeat(List<PlayerInput> players, int seat) {
        return players.stream()
                .filter(p -> p.getSeatIndex() == seat)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Missing seatIndex: " + seat));
    }

    private List<PlayerState> toPlayerStates(List<PlayerInput> playerInputs) {
        return playerInputs.stream()
                .map(input -> {
                    PlayerState state = new PlayerState();
                    state.setPlayerId(input.getPlayerId());
                    state.setDisplayName(input.getDisplayName());
                    state.setSeatIndex(input.getSeatIndex());
                    state.setPlayerType(input.getPlayerType());
                    state.setTeam(input.getTeam());
                    return state;
                })
                .sorted(Comparator.comparingInt(PlayerState::getSeatIndex))
                .toList();
    }

    private List<Card> buildDeck() {
        List<Card> cards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
        return cards;
    }

    private void dealCards(GameState gameState, int startSeat, int cardsPerPlayer) {
        for (int i = 0; i < cardsPerPlayer; i++) {
            for (int offset = 0; offset < PLAYER_COUNT; offset++) {
                int seat = (startSeat + offset) % PLAYER_COUNT;
                PlayerState player = getPlayerBySeat(gameState, seat);
                if (gameState.getDeck().isEmpty()) {
                    throw new IllegalStateException("Deck exhausted unexpectedly");
                }
                Card card = gameState.getDeck().removeFirst();
                player.getHand().add(card);
            }
        }
    }

    private PlayerState getPlayerById(GameState gameState, String playerId) {
        return gameState.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown playerId: " + playerId));
    }

    private PlayerState getPlayerBySeat(GameState gameState, int seatIndex) {
        return gameState.getPlayers().stream()
                .filter(p -> p.getSeatIndex() == seatIndex)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown seatIndex: " + seatIndex));
    }

    private int nextSeat(int currentSeat) {
        return (currentSeat + 1) % PLAYER_COUNT;
    }

    private GameState handleTrumpChooser(GameState gameState) {
        PlayerState chooser = getPlayerById(gameState, gameState.getTrumpChooserPlayerId());
        if (chooser.getPlayerType() == PlayerType.HUMAN) {
            broadcastService.publishChooseTrumpAction(gameState, chooser);
            return gameState;
        }

        Suit botSuit = logicServiceClient.chooseTrump(chooser.getPlayerId(), chooser.getSeatIndex(), chooser.getHand())
                .orElseGet(() -> chooseFallbackTrump(chooser.getHand()));

        applyTrumpChoice(gameState, chooser.getPlayerId(), botSuit);
        GameState saved = save(gameState);
        broadcastService.publish(saved);
        return maybeProcessBotTurns(saved);
    }

    private GameState maybeProcessBotTurns(GameState gameState) {
        GameState current = gameState;

        while (current.getStatus() == GameStatus.IN_PROGRESS && current.getCurrentTurnPlayerId() != null) {
            PlayerState currentPlayer = getPlayerById(current, current.getCurrentTurnPlayerId());
            if (currentPlayer.getPlayerType() == PlayerType.HUMAN) {
                return current;
            }
            GameState stateSnapshot = current;
            PlayerState playerSnapshot = currentPlayer;

            Card botMove = logicServiceClient.chooseMove(
                            stateSnapshot.getGameId().toString(),
                            playerSnapshot.getPlayerId(),
                            playerSnapshot.getSeatIndex(),
                            stateSnapshot.getTrumpSuit(),
                            new ArrayList<>(playerSnapshot.getHand()),
                            new ArrayList<>(stateSnapshot.getCurrentTrick()),
                            stateSnapshot.getCurrentTrickNumber() + 1,
                            toTrickHistoryForLogic(stateSnapshot),
                            remainingCardsCountBySeat(stateSnapshot)
                    )
                    .filter(card -> isLegalForCurrentTurn(stateSnapshot, playerSnapshot, card))
                    .orElseGet(() -> chooseFallbackMove(stateSnapshot, playerSnapshot));

            applyPlayedCard(current, currentPlayer, botMove);
            current = save(current);
            broadcastService.publish(current);
        }

        if (current.getStatus() == GameStatus.WAITING_TRUMP && current.getTrumpChooserPlayerId() != null) {
            PlayerState chooser = getPlayerById(current, current.getTrumpChooserPlayerId());
            if (chooser.getPlayerType() == PlayerType.BOT) {
                return handleTrumpChooser(current);
            }
        }

        return current;
    }

    private void applyPlayedCard(GameState gameState, PlayerState player, Card requestedCard) {
        Card playedCard = findAndRemoveCard(player, requestedCard)
                .orElseThrow(() -> new BadRequestException("Card not found in player's hand"));

        validateFollowSuit(gameState, player, playedCard);

        gameState.getCurrentTrick().add(new PlayedCard(player.getPlayerId(), player.getSeatIndex(), playedCard));

        if (gameState.getCurrentTrick().size() < PLAYER_COUNT) {
            gameState.setCurrentTurnPlayerId(getPlayerBySeat(gameState, nextSeat(player.getSeatIndex())).getPlayerId());
            return;
        }

        resolveTrick(gameState);
    }

    private Card chooseFallbackMove(GameState gameState, PlayerState player) {
        Suit leadSuit = gameState.getCurrentTrick().isEmpty() ? null : gameState.getCurrentTrick().getFirst().getCard().getSuit();
        List<Card> legal = legalMoves(player.getHand(), leadSuit);
        return legal.stream()
                .min(Comparator.comparingInt(card -> card.getRank().getStrength()))
                .orElseThrow(() -> new BadRequestException("No legal bot move available"));
    }

    private boolean isLegalForCurrentTurn(GameState gameState, PlayerState player, Card card) {
        if (card == null) {
            return false;
        }
        if (!player.getHand().contains(card)) {
            return false;
        }
        Suit leadSuit = gameState.getCurrentTrick().isEmpty() ? null : gameState.getCurrentTrick().getFirst().getCard().getSuit();
        List<Card> legal = legalMoves(player.getHand(), leadSuit);
        return legal.contains(card);
    }

    private List<Card> legalMoves(List<Card> hand, Suit leadSuit) {
        if (leadSuit == null) {
            return hand;
        }
        List<Card> followSuit = hand.stream().filter(card -> card.getSuit() == leadSuit).toList();
        return followSuit.isEmpty() ? hand : followSuit;
    }

    private List<List<PlayedCard>> toTrickHistoryForLogic(GameState gameState) {
        return gameState.getTrickHistory().stream()
                .<List<PlayedCard>>map(record -> new ArrayList<>(record.getPlayedCards()))
                .toList();
    }

    private Map<Integer, Integer> remainingCardsCountBySeat(GameState gameState) {
        Map<Integer, Integer> result = new HashMap<>();
        for (PlayerState player : gameState.getPlayers()) {
            result.put(player.getSeatIndex(), player.getHand().size());
        }
        return result;
    }

    private Suit chooseFallbackTrump(List<Card> hand) {
        Map<Suit, Integer> suitCounts = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            suitCounts.put(suit, 0);
        }
        for (Card card : hand) {
            suitCounts.put(card.getSuit(), suitCounts.get(card.getSuit()) + 1);
        }
        return suitCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Suit.SPADES);
    }
}
