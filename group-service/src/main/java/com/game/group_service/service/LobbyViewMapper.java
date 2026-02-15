package com.game.group_service.service;

import com.game.group_service.dto.LobbyPlayerView;
import com.game.group_service.dto.LobbyView;
import com.game.group_service.model.LobbyPlayer;
import com.game.group_service.model.LobbyState;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class LobbyViewMapper {

    public LobbyView toView(LobbyState state) {
        LobbyView view = new LobbyView();
        view.setLobbyId(state.getLobbyId());
        view.setMode(state.getMode());
        view.setStatus(state.getStatus());
        view.setHostPlayerId(state.getHostPlayerId());
        view.setGameId(state.getGameId());
        view.setCreatedAt(state.getCreatedAt());
        view.setUpdatedAt(state.getUpdatedAt());

        view.setPlayers(state.getPlayers().stream()
                .sorted(Comparator.comparingInt(LobbyPlayer::getSeatIndex))
                .map(this::mapPlayer)
                .toList());

        return view;
    }

    private LobbyPlayerView mapPlayer(LobbyPlayer player) {
        LobbyPlayerView v = new LobbyPlayerView();
        v.setPlayerId(player.getPlayerId());
        v.setDisplayName(player.getDisplayName());
        v.setPlayerType(player.getPlayerType());
        v.setSeatIndex(player.getSeatIndex());
        v.setTeam(player.getTeam());
        v.setAccepted(player.isAccepted());
        return v;
    }
}
