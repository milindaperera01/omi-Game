package com.game.group_service.repository;

import com.game.group_service.model.LobbyState;

import java.util.Optional;
import java.util.UUID;

public interface LobbyRepository {
    LobbyState save(LobbyState state);

    Optional<LobbyState> findById(UUID lobbyId);
}
