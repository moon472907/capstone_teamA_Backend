package com.back.domain.game.redis;

import com.back.domain.game.entity.GameState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class
GameSession {

    private Integer gameId;
    private Integer boardId;
    private GameState state;
    private int currentPlayerIndex;
    private int round;
    private int maxRounds;
    private int maxPlayers;
    private long turnStartTime;
    private int turnTimeoutSeconds;
    private List<PlayerSession> players;

    // 분기점 선택 대기 상태
    private List<Integer> pendingBranchNodeIds;
    private int pendingRemainingSteps;
    private long branchSelectStartTime;
    private int branchTimeoutSeconds;

    // 이벤트 카드 대기 상태 (CARD_TARGET_SELECT / CARD_DEFENSE)
    private String pendingCardKey;
    private String pendingCardType;        // ATTACK / DEFENSE / SCHOLARSHIP
    private String pendingCardTitle;
    private String pendingCardDescription;
    private int pendingCardStars;          // 적용할 스타 증감 (예: -2)
    private Integer pendingTargetPlayerId; // 공격 대상으로 지정된 플레이어
    private long cardActionStartTime;
    private int cardTimeoutSeconds;

    // 두리버스 대기 상태 (BUS_SELECT)
    private List<Integer> pendingBusStops; // 이동 가능한 정류장 tileId 목록
    private long busSelectStartTime;
    private int busTimeoutSeconds;

    @JsonIgnore
    public PlayerSession getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Returns true if every player in the current round has taken their turn.
     */
    @JsonIgnore
    public boolean isRoundComplete() {
        return currentPlayerIndex >= players.size() - 1;
    }

    /**
     * Returns true when the last player finishes the final round.
     */
    @JsonIgnore
    public boolean isGameComplete() {
        return round >= maxRounds && isRoundComplete();
    }

    /**
     * Moves to the next player's turn. Increments round when all players have gone.
     */
    public void advanceTurn() {
        if (currentPlayerIndex < players.size() - 1) {
            currentPlayerIndex++;
        } else {
            currentPlayerIndex = 0;
            round++;
        }
        turnStartTime = System.currentTimeMillis();
    }
}
