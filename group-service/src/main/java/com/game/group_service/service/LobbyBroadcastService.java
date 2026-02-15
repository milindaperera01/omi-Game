package com.game.group_service.service;

import com.game.group_service.model.LobbyState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class LobbyBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final LobbyViewMapper mapper;

    public LobbyBroadcastService(SimpMessagingTemplate messagingTemplate, LobbyViewMapper mapper) {
        this.messagingTemplate = messagingTemplate;
        this.mapper = mapper;
    }

    public void publish(LobbyState state) {
        messagingTemplate.convertAndSend("/topic/lobbies/" + state.getLobbyId(), mapper.toView(state));
    }
}
