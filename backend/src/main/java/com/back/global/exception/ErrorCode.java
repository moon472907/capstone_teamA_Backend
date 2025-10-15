package com.back.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "INPUT-400", "요청이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "인증되지 않았습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "USER-404", "리소스를 찾을 수 없습니다."),
    PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTY-404", "해당 파티를 찾을 수 없습니다."),
    PARTY_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "PM-404", "해당 파티 멤버 관계를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND_BY_CODE(HttpStatus.NOT_FOUND, "MEMBER-404", "해당 초대 코드를 가진 멤버를 찾을 수 없습니다."),
    PARTY_FORBIDDEN(HttpStatus.FORBIDDEN, "PARTY-403", "파티에 대한 권한이 없습니다. (리더 전용 기능)"),
    ALREADY_PARTY_MEMBER(HttpStatus.CONFLICT, "PM-409", "이미 해당 파티의 멤버(또는 대기자)입니다."),
    PARTY_CAPACITY_FULL(HttpStatus.CONFLICT, "PARTY-409", "파티 정원이 가득 찼습니다."),
    INVALID_PARTY_MEMBER_STATUS(HttpStatus.BAD_REQUEST, "PM-400", "현재 파티 멤버 상태로는 요청하신 작업을 수행할 수 없습니다."),
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
