package com.game.group_service.service;

import com.game.group_service.dto.CreateMultiplayerLobbyRequest;
import com.game.group_service.dto.CreateSinglePlayerRequest;
import com.game.group_service.dto.InviteResponseRequest;
import com.game.group_service.integration.GameServiceClient;
import com.game.group_service.model.LobbyState;
import com.game.group_service.model.LobbyStatus;
import com.game.group_service.repository.InMemoryLobbyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GroupServiceTest {

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        GameServiceClient gameClient = Mockito.mock(GameServiceClient.class);
        Mockito.when(gameClient.createGame(Mockito.any())).thenReturn("game-123");
        groupService = new GroupService(new InMemoryLobbyRepository(), gameClient, Mockito.mock(LobbyBroadcastService.class));
    }

    @Test
    void singlePlayerCreatesGameImmediately() {
        CreateSinglePlayerRequest req = new CreateSinglePlayerRequest();
        req.setPlayerId("p0");
        req.setDisplayName("Milinda");

        LobbyState state = groupService.createSinglePlayer(req);

        assertNotNull(state.getLobbyId());
        assertEquals(LobbyStatus.GAME_CREATED, state.getStatus());
        assertEquals("game-123", state.getGameId());
        assertEquals(4, state.getPlayers().size());
    }

    @Test
    void multiplayerCreatesGameWhenAllAccepted() {
        CreateMultiplayerLobbyRequest req = new CreateMultiplayerLobbyRequest();
        req.setHostPlayerId("h1");
        req.setHostDisplayName("Host");
        req.setInvitedPlayerIds(List.of("p1", "p2", "p3"));

        LobbyState lobby = groupService.createMultiplayerLobby(req);
        assertEquals(LobbyStatus.WAITING_FOR_PLAYERS, lobby.getStatus());

        groupService.respondInvite(lobby.getLobbyId(), response("p1", true));
        groupService.respondInvite(lobby.getLobbyId(), response("p2", true));
        LobbyState finalState = groupService.respondInvite(lobby.getLobbyId(), response("p3", true));

        assertEquals(LobbyStatus.GAME_CREATED, finalState.getStatus());
        assertEquals("game-123", finalState.getGameId());
    }

    private InviteResponseRequest response(String playerId, boolean accepted) {
        InviteResponseRequest request = new InviteResponseRequest();
        request.setPlayerId(playerId);
        request.setDisplayName(playerId.toUpperCase());
        request.setAccepted(accepted);
        return request;
    }
}
