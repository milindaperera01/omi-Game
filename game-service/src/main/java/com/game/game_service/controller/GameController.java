package com.game.game_service.controller;

import com.game.game_service.domain.GameState;
import com.game.game_service.dto.CreateGameRequest;
import com.game.game_service.dto.GameView;
import com.game.game_service.dto.PlayCardRequest;
import com.game.game_service.dto.TrumpRequest;
import com.game.game_service.service.GameService;
import com.game.game_service.service.GameViewMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameViewMapper gameViewMapper;

    public GameController(GameService gameService, GameViewMapper gameViewMapper) {
        this.gameService = gameService;
        this.gameViewMapper = gameViewMapper;
    }

    @PostMapping
    public GameView createGame(@Valid @RequestBody CreateGameRequest request,
                               @RequestParam(required = false) String viewerPlayerId) {
        GameState state = gameService.createGame(request);
        return gameViewMapper.toView(state, viewerPlayerId);
    }

    @PostMapping("/{gameId}/trump")
    public GameView chooseTrump(@PathVariable UUID gameId,
                                @Valid @RequestBody TrumpRequest request,
                                @RequestParam(required = false) String viewerPlayerId) {
        GameState state = gameService.chooseTrump(gameId, request);
        return gameViewMapper.toView(state, viewerPlayerId);
    }

    @PostMapping("/{gameId}/play")
    public GameView playCard(@PathVariable UUID gameId,
                             @Valid @RequestBody PlayCardRequest request,
                             @RequestParam(required = false) String viewerPlayerId) {
        GameState state = gameService.playCard(gameId, request);
        return gameViewMapper.toView(state, viewerPlayerId);
    }

    @GetMapping("/{gameId}")
    public GameView getGame(@PathVariable UUID gameId,
                            @RequestParam String viewerPlayerId) {
        GameState state = gameService.getGame(gameId);
        return gameViewMapper.toView(state, viewerPlayerId);
    }
}
