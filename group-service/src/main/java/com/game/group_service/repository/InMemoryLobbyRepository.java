package com.game.group_service.repository;

import com.game.group_service.model.LobbyState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "app.persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryLobbyRepository implements LobbyRepository {

    private final Map<UUID, LobbyState> store = new ConcurrentHashMap<>();

    @Override
    public LobbyState save(LobbyState state) {
        store.put(state.getLobbyId(), state);
        return state;
    }

    @Override
    public Optional<LobbyState> findById(UUID lobbyId) {
        return Optional.ofNullable(store.get(lobbyId));
    }
}
