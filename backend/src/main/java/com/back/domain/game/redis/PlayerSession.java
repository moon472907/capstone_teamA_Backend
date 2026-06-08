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
    private Integer prevTileId; // 직전 턴에 밟고 온 칸 — 다음 이동에서 즉시 U턴(되돌아가기) 금지에 사용. 워프/버스 이동 시 null로 초기화.
    private int coins;
    private int gpa;
    private int stars;          // 점수 = 획득한 스타 개수
    private int defenseCards;   // 보유 중인 방어 카드(곰두리의 수호) 수 — 무제한 보유
    private boolean skipNextTurn; // "그렇게 과CC를..." 카드: 다음 턴 1회 스킵
    private boolean connected;
}
