package com.game.group_service.service;

import com.game.group_service.dto.CreateMultiplayerLobbyRequest;
import com.game.group_service.dto.CreateSinglePlayerRequest;
import com.game.group_service.dto.InviteResponseRequest;
import com.game.group_service.exception.BadRequestException;
import com.game.group_service.exception.NotFoundException;
import com.game.group_service.integration.CreateGamePlayer;
import com.game.group_service.integration.CreateGameRequest;
import com.game.group_service.integration.GameServiceClient;
import com.game.group_service.model.*;
import com.game.group_service.repository.LobbyRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class GroupService {

    private static final List<String> BOT_NAMES = List.of("Bot Alpha", "Bot Bravo", "Bot Charlie", "Bot Delta");

    private final LobbyRepository repository;
    private final GameServiceClient gameServiceClient;
    private final LobbyBroadcastService broadcastService;

    public GroupService(LobbyRepository repository,
                        GameServiceClient gameServiceClient,
                        LobbyBroadcastService broadcastService) {
        this.repository = repository;
        this.gameServiceClient = gameServiceClient;
        this.broadcastService = broadcastService;
    }

    public LobbyState createSinglePlayer(CreateSinglePlayerRequest request) {
        LobbyState state = new LobbyState();
        state.setLobbyId(UUID.randomUUID());
        state.setMode(LobbyMode.SINGLE_PLAYER);
        state.setStatus(LobbyStatus.READY);
        state.setHostPlayerId(request.getPlayerId());
        state.setCreatedAt(Instant.now());
        state.setUpdatedAt(Instant.now());

        List<LobbyPlayer> players = new ArrayList<>();
        players.add(player(request.getPlayerId(), request.getDisplayName(), PlayerType.HUMAN, 0, Team.RED, true));
        players.add(player("bot-1-" + state.getLobbyId(), BOT_NAMES.get(0), PlayerType.BOT, 1, Team.BLUE, true));
        players.add(player("bot-2-" + state.getLobbyId(), BOT_NAMES.get(1), PlayerType.BOT, 2, Team.RED, true));
        players.add(player("bot-3-" + state.getLobbyId(), BOT_NAMES.get(2), PlayerType.BOT, 3, Team.BLUE, true));
        state.setPlayers(players);

        createGameForLobby(state);
        LobbyState saved = save(state);
        broadcastService.publish(saved);
        return saved;
    }

    public LobbyState createMultiplayerLobby(CreateMultiplayerLobbyRequest request) {
        validateMultiplayerRequest(request);

        LobbyState state = new LobbyState();
        state.setLobbyId(UUID.randomUUID());
        state.setMode(LobbyMode.MULTIPLAYER);
        state.setStatus(LobbyStatus.WAITING_FOR_PLAYERS);
        state.setHostPlayerId(request.getHostPlayerId());
        state.setCreatedAt(Instant.now());
        state.setUpdatedAt(Instant.now());

        List<LobbyPlayer> players = new ArrayList<>();
        players.add(player(request.getHostPlayerId(), request.getHostDisplayName(), PlayerType.HUMAN, 0, Team.RED, true));

        int seat = 1;
        for (String invitedId : request.getInvitedPlayerIds()) {
            Team team = (seat % 2 == 0) ? Team.RED : Team.BLUE;
            players.add(player(invitedId, "Pending", PlayerType.HUMAN, seat, team, false));
            seat++;
        }
        state.setPlayers(players);

        LobbyState saved = save(state);
        broadcastService.publish(saved);
        return saved;
    }

    public LobbyState respondInvite(UUID lobbyId, InviteResponseRequest request) {
        LobbyState state = getLobby(lobbyId);
        if (state.getMode() != LobbyMode.MULTIPLAYER) {
            throw new BadRequestException("Invite response is only valid for multiplayer lobbies");
        }
        if (state.getStatus() == LobbyStatus.GAME_CREATED || state.getStatus() == LobbyStatus.CANCELLED) {
            throw new BadRequestException("Lobby is already finalized");
        }

        LobbyPlayer player = state.getPlayers().stream()
                .filter(p -> p.getPlayerId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Player is not part of this lobby"));

        if (request.getAccepted()) {
            player.setAccepted(true);
            player.setDisplayName(request.getDisplayName());
        } else {
            state.setStatus(LobbyStatus.CANCELLED);
            LobbyState saved = save(state);
            broadcastService.publish(saved);
            return saved;
        }

        if (state.getPlayers().stream().allMatch(LobbyPlayer::isAccepted)) {
            state.setStatus(LobbyStatus.READY);
            createGameForLobby(state);
        }

        LobbyState saved = save(state);
        broadcastService.publish(saved);
        return saved;
    }

    public LobbyState getLobby(UUID lobbyId) {
        return repository.findById(lobbyId)
                .orElseThrow(() -> new NotFoundException("Lobby not found: " + lobbyId));
    }

    private void createGameForLobby(LobbyState state) {
        CreateGameRequest createGameRequest = new CreateGameRequest();
        createGameRequest.setDealerPlayerId(state.getPlayers().stream()
                .filter(p -> p.getSeatIndex() == 3)
                .findFirst()
                .map(LobbyPlayer::getPlayerId)
                .orElseThrow(() -> new BadRequestException("Seat 3 player not found")));

        List<CreateGamePlayer> players = state.getPlayers().stream().map(p -> {
            CreateGamePlayer gp = new CreateGamePlayer();
            gp.setPlayerId(p.getPlayerId());
            gp.setDisplayName(p.getDisplayName());
            gp.setPlayerType(p.getPlayerType());
            gp.setTeam(p.getTeam());
            gp.setSeatIndex(p.getSeatIndex());
            return gp;
        }).toList();
        createGameRequest.setPlayers(players);

        String gameId = gameServiceClient.createGame(createGameRequest);
        state.setGameId(gameId);
        state.setStatus(LobbyStatus.GAME_CREATED);
    }

    private LobbyState save(LobbyState state) {
        state.setUpdatedAt(Instant.now());
        return repository.save(state);
    }

    private LobbyPlayer player(String id, String name, PlayerType type, int seat, Team team, boolean accepted) {
        LobbyPlayer p = new LobbyPlayer();
        p.setPlayerId(id);
        p.setDisplayName(name);
        p.setPlayerType(type);
        p.setSeatIndex(seat);
        p.setTeam(team);
        p.setAccepted(accepted);
        return p;
    }

    private void validateMultiplayerRequest(CreateMultiplayerLobbyRequest request) {
        Set<String> ids = new HashSet<>(request.getInvitedPlayerIds());
        if (ids.size() != request.getInvitedPlayerIds().size()) {
            throw new BadRequestException("invitedPlayerIds must be unique");
        }
        if (ids.contains(request.getHostPlayerId())) {
            throw new BadRequestException("Host cannot be in invitedPlayerIds");
        }
    }
}
