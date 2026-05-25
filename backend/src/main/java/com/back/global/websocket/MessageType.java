package com.back.global.websocket;

public enum MessageType {
    GAME_STARTED,
    TURN_CHANGED,
    DICE_ROLLED,
    PLAYER_MOVED,
    BRANCH_REQUIRED,
    TILE_TRIGGERED,
    CARD_DRAWN,           // 이벤트 카드를 뽑음 (전체에게 공개)
    CARD_TARGET_REQUIRED, // 공격 카드: 현재 플레이어에게 대상 지정 요청
    DEFENSE_PROMPT,       // 피격 대상에게 방어 카드 사용 여부 요청
    BUS_RIDE_REQUIRED,    // 두리버스: 도착 정류장 선택 요청
    TURN_SKIPPED,         // 스킵 카드로 턴을 건너뜀
    GAME_ENDED,
    PLAYER_JOINED,
    PLAYER_LEFT,
    PLAYER_READY,
    STATE_SNAPSHOT,
    ERROR
}