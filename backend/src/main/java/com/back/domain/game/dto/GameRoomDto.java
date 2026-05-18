package com.back.domain.game.dto;

import com.back.domain.game.entity.Game;

public record GameRoomDto(
        Integer gameId,
        String title,
        int currentPlayers,
        int maxPlayers,
        String state,
        Integer hostMemberId
) {
    public static GameRoomDto from(Game game) {
        return new GameRoomDto(
                game.getId(),
                game.getTitle(),
                game.getPlayerCount(),
                game.getMaxPlayers(),
                game.getState().name(),
                game.getHostMemberId()
        );
    }
}
