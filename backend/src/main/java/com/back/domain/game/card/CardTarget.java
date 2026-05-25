package com.back.domain.game.card;

public enum CardTarget {
    SELF,      // 뽑은 본인에게 적용
    OPPONENT,  // 뽑은 사람이 상대를 지정해 적용
    NONE       // 즉시 적용 대상 없음 (방어 카드: 보관)
}
