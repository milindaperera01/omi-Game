package com.game.game_service.service;

import com.game.game_service.domain.GameState;
import com.game.game_service.domain.PlayerState;
import com.game.game_service.dto.PlayerActionMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class GameBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameViewMapper viewMapper;

    public GameBroadcastService(SimpMessagingTemplate messagingTemplate, GameViewMapper viewMapper) {
        this.messagingTemplate = messagingTemplate;
        this.viewMapper = viewMapper;
    }

    public void publish(GameState gameState) {
        messagingTemplate.convertAndSend(
                "/topic/games/" + gameState.getGameId(),
                viewMapper.toView(gameState, null)
        );
    }

    public void publishChooseTrumpAction(GameState gameState, PlayerState chooser) {
        PlayerActionMessage action = new PlayerActionMessage();
        action.setActionType("CHOOSE_TRUMP");
        action.setGameId(gameState.getGameId());
        action.setPlayerId(chooser.getPlayerId());
        action.setCards(new ArrayList<>(chooser.getHand()));

        messagingTemplate.convertAndSend(
                "/topic/players/" + chooser.getPlayerId() + "/actions",
                action
        );
    }
}
