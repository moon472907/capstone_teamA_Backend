package com.back.domain.game.entity;

public enum GameState {
    WAITING,
    IN_PROGRESS,
    TURN_START,
    DICE_ROLL,
    MOVE,
    BRANCH_SELECT,
    TILE_EVENT,
    CARD_TARGET_SELECT,  // 공격 카드: 현재 플레이어가 대상(상대) 지정 대기
    CARD_DEFENSE,        // 피격 대상이 방어 카드 사용 여부 선택 대기
    BUS_SELECT,          // 두리버스: 현재 플레이어가 도착 정류장 선택 대기
    TURN_END,
    GAME_END
}