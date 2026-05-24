package com.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Generic
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INPUT-400", "요청이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "인증되지 않았습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-401-01", "토큰이 만료되었습니다. 다시 로그인 해주세요."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-401-02", "유효하지 않은 토큰입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "리소스를 찾을 수 없습니다."),
    INVALID_PARTY_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "PM-400", "현재 파티 멤버 상태로는 요청하신 작업을 수행할 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT-409", "요청이 충돌합니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER-500", "서버 내부 오류가 발생하였습니다."),

    // Game
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME-404", "게임을 찾을 수 없습니다."),
    GAME_NOT_WAITING(HttpStatus.BAD_REQUEST, "GAME-400-01", "게임이 대기 상태가 아닙니다."),
    GAME_FULL(HttpStatus.BAD_REQUEST, "GAME-400-02", "게임 정원이 찼습니다."),
    GAME_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "GAME-400-03", "게임이 진행 중이 아닙니다."),
    GAME_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "GAME-400-04", "이미 종료된 게임입니다."),
    GAME_NOT_ENOUGH_PLAYERS(HttpStatus.BAD_REQUEST, "GAME-400-05", "4명이 모두 참여해야 게임을 시작할 수 있습니다."),
    GAME_NOT_ALL_READY(HttpStatus.BAD_REQUEST, "GAME-400-07", "모든 플레이어가 준비해야 게임을 시작할 수 있습니다."),
    NOT_YOUR_TURN(HttpStatus.FORBIDDEN, "GAME-403-01", "현재 당신의 턴이 아닙니다."),
    NOT_HOST(HttpStatus.FORBIDDEN, "GAME-403-02", "방장만 게임을 시작할 수 있습니다."),
    INVALID_BRANCH_SELECTION(HttpStatus.BAD_REQUEST, "GAME-400-08", "선택할 수 없는 분기점입니다."),
    INVALID_STATE_TRANSITION(HttpStatus.BAD_REQUEST, "GAME-400-06", "현재 게임 상태에서 허용되지 않는 요청입니다."),
    PLAYER_ALREADY_IN_GAME(HttpStatus.CONFLICT, "GAME-409-01", "이미 해당 게임에 참여 중입니다."),
    GAME_ACTION_LOCKED(HttpStatus.CONFLICT, "GAME-409-02", "다른 액션이 처리 중입니다. 잠시 후 다시 시도해주세요."),

    // Board
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD-404", "보드를 찾을 수 없습니다."),

    // Character
    CHARACTER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAR-404", "존재하지 않는 캐릭터입니다."),
    CHARACTER_ALREADY_TAKEN(HttpStatus.CONFLICT, "CHAR-409", "이미 다른 플레이어가 선택한 캐릭터입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
