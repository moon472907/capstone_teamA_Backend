package com.back.domain.game.dto;

import com.back.domain.game.entity.GameState;
import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStateSnapshotDto {

    private Integer gameId;
    private Integer boardId;
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
        private Integer tileNumber;  // 프론트 매핑용: "node{tileNumber}" (1~53)
        private int coins;
        private int stars;
        private int defenseCards;
        private int gpa;
        private boolean connected;
    }

    /**
     * @param tileIdToNumber DB Node id → nodeNumber(1~53) 매핑. null이면 tileId를 그대로 사용.
     */
    public static GameStateSnapshotDto from(GameSession session, Map<Integer, Integer> tileIdToNumber) {
        List<PlayerSnapshotDto> playerDtos = session.getPlayers().stream()
                .map(p -> PlayerSnapshotDto.builder()
                        .playerId(p.getPlayerId())
                        .nickname(p.getNickname())
                        .characterKey(p.getCharacterKey())
                        .tileId(p.getTileId())
                        .tileNumber(tileIdToNumber == null
                                ? p.getTileId()
                                : tileIdToNumber.getOrDefault(p.getTileId(), p.getTileId()))
                        .coins(p.getCoins())
                        .stars(p.getStars())
                        .defenseCards(p.getDefenseCards())
                        .gpa(p.getGpa())
                        .connected(p.isConnected())
                        .build())
                .toList();

        PlayerSession current = session.getCurrentPlayer();

        return GameStateSnapshotDto.builder()
                .gameId(session.getGameId())
                .boardId(session.getBoardId())
                .state(session.getState())
                .round(session.getRound())
                .maxRounds(session.getMaxRounds())
                .currentPlayerIndex(session.getCurrentPlayerIndex())
                .currentPlayerNickname(current.getNickname())
                .players(playerDtos)
                .build();
    }
}
