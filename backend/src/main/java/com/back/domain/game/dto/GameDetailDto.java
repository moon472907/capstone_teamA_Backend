package com.back.domain.game.dto;

import com.back.domain.game.entity.Game;
import com.back.domain.player.entity.Player;

import java.util.Comparator;
import java.util.List;

/** 대기방 상세 — 로비에서 실시간 슬롯/레디 구성을 위한 현재 로스터. */
public record GameDetailDto(
        Integer gameId,
        String title,
        String state,
        int maxPlayers,
        int currentPlayers,
        Integer hostMemberId,
        List<RoomPlayerDto> players
) {
    public record RoomPlayerDto(
            Integer playerId,
            Integer memberId,
            String nickname,
            String characterKey,
            int playerIndex,
            boolean ready
    ) {}

    public static GameDetailDto from(Game game, List<Player> players) {
        List<RoomPlayerDto> roster = players.stream()
                .sorted(Comparator.comparingInt(Player::getPlayerIndex))
                .map(p -> new RoomPlayerDto(
                        p.getId(),
                        p.getMember().getId(),
                        p.getNickname(),
                        p.getCharacterKey(),
                        p.getPlayerIndex(),
                        p.isReady()))
                .toList();

        return new GameDetailDto(
                game.getId(),
                game.getTitle(),
                game.getState().name(),
                game.getMaxPlayers(),
                roster.size(),
                game.getHostMemberId(),
                roster
        );
    }
}
