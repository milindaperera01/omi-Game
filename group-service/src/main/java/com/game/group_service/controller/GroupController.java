package com.game.group_service.controller;

import com.game.group_service.dto.CreateMultiplayerLobbyRequest;
import com.game.group_service.dto.CreateSinglePlayerRequest;
import com.game.group_service.dto.InviteResponseRequest;
import com.game.group_service.dto.LobbyView;
import com.game.group_service.model.LobbyState;
import com.game.group_service.service.GroupService;
import com.game.group_service.service.LobbyViewMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;
    private final LobbyViewMapper mapper;

    public GroupController(GroupService groupService, LobbyViewMapper mapper) {
        this.groupService = groupService;
        this.mapper = mapper;
    }

    @PostMapping("/single-player")
    public LobbyView createSinglePlayer(@Valid @RequestBody CreateSinglePlayerRequest request) {
        LobbyState state = groupService.createSinglePlayer(request);
        return mapper.toView(state);
    }

    @PostMapping("/multiplayer")
    public LobbyView createMultiplayer(@Valid @RequestBody CreateMultiplayerLobbyRequest request) {
        LobbyState state = groupService.createMultiplayerLobby(request);
        return mapper.toView(state);
    }

    @PostMapping("/{lobbyId}/responses")
    public LobbyView respondInvite(@PathVariable UUID lobbyId,
                                   @Valid @RequestBody InviteResponseRequest request) {
        LobbyState state = groupService.respondInvite(lobbyId, request);
        return mapper.toView(state);
    }

    @GetMapping("/{lobbyId}")
    public LobbyView getLobby(@PathVariable UUID lobbyId) {
        return mapper.toView(groupService.getLobby(lobbyId));
    }
}
