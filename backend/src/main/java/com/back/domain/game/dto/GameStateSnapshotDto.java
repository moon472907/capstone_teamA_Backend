package com.back.domain.game.dto;

import com.back.domain.game.entity.GameState;
import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateSnapshotDto {

    private Integer gameId;
    private GameState state;
    private int round;
    private int maxRounds;
    private int currentPlayerIndex;
    private String currentPlayerNickname;
    private List<PlayerSnapshotDto> players;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerSnapshotDto {
        private Integer playerId;
        private String nickname;
        private String characterKey;
        private Integer tileId;
        private int coins;
        private int gpa;
        private boolean connected;
    }

    public static GameStateSnapshotDto from(GameSession session) {
        List<PlayerSnapshotDto> playerDtos = session.getPlayers().stream()
                .map(p -> PlayerSnapshotDto.builder()
                        .playerId(p.getPlayerId())
                        .nickname(p.getNickname())
                        .characterKey(p.getCharacterKey())
                        .tileId(p.getTileId())
                        .coins(p.getCoins())
                        .gpa(p.getGpa())
                        .connected(p.isConnected())
                        .build())
                .toList();

        PlayerSession current = session.getCurrentPlayer();

        return GameStateSnapshotDto.builder()
                .gameId(session.getGameId())
                .state(session.getState())
                .round(session.getRound())
                .maxRounds(session.getMaxRounds())
                .currentPlayerIndex(session.getCurrentPlayerIndex())
                .currentPlayerNickname(current.getNickname())
                .players(playerDtos)
                .build();
    }
}
