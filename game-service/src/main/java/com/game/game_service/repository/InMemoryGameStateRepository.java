package com.game.game_service.repository;

import com.game.game_service.domain.GameState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "app.persistence", havingValue = "memory", matchIfMissing = true)
public class InMemoryGameStateRepository implements GameStateRepository {

    private final Map<UUID, GameState> store = new ConcurrentHashMap<>();

    @Override
    public GameState save(GameState gameState) {
        store.put(gameState.getGameId(), gameState);
        return gameState;
    }

    @Override
    public Optional<GameState> findById(UUID gameId) {
        return Optional.ofNullable(store.get(gameId));
    }

    @Override
    public void delete(UUID gameId) {
        store.remove(gameId);
    }
}
