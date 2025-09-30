package com.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INPUT-400", "요청이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "인증되지 않았습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT-409", "요청이 충돌합니다"),
    LEVEL_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "LEVEL-404", "레벨업 기준 데이터(LevelXP)를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER-500", "서버 내부 오류가 발생하였습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
