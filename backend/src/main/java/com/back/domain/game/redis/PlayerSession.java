package com.back.domain.game.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSession {
    private Integer playerId;
    private Integer memberId;
    private String nickname;
    private String characterKey;
    private Integer tileId;
    private int coins;
    private int gpa;
    private int stars;          // 점수 = 획득한 스타 개수
    private int defenseCards;   // 보유 중인 방어 카드(곰두리의 수호) 수 — 무제한 보유
    private boolean skipNextTurn; // "그렇게 과CC를..." 카드: 다음 턴 1회 스킵
    private boolean connected;
}
