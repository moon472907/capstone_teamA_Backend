package com.back.domain.mission.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MissionErrorCode {


    MEMBER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-401", "회원 인증이 필요합니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-404","회원이 존재하지 않습니다."),
    MEMBER_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-403", "권한이 없습니다."),


    MISSION_PERIOD_EXCEEDED(HttpStatus.BAD_REQUEST, "MISSION-400", "미션 기간은 최대 15주까지 가능합니다."),
    MISSION_START_MUST_BE_MONDAY(HttpStatus.BAD_REQUEST, "MISSION-401", "미션 시작일은 월요일이어야 합니다."),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION-404", "존재하지 않는 미션입니다."),
    MISSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MISSION-402", "미션 개수 제한을 초과했습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
