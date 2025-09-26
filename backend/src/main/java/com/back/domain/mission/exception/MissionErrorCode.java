package com.back.domain.mission.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MissionErrorCode {
    // Mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION-404", "미션을 찾을 수 없습니다."),
    MISSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "MISSION-402", "미션은 최대 5개까지 생성 가능합니다."),
    MISSION_NOT_EDITABLE(HttpStatus.BAD_REQUEST, "MISSION-403", "수정할 수 없는 미션입니다."),

    // SubGoal
    SUBGOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBGOAL-404", "서브골을 찾을 수 없습니다."),
    ALREADY_EDITED(HttpStatus.BAD_REQUEST, "SUBGOAL-400", "이미 수정된 주차입니다."),
    NOT_EDITABLE(HttpStatus.BAD_REQUEST, "SUBGOAL-401", "수정할 수 없는 주차입니다."),

    // Task
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "TASK-404", "해당 Task를 찾을 수 없습니다."),
    TASK_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "TASK-400", "이미 완료된 Task입니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-404", "멤버를 찾을 수 없습니다."),
    MEMBER_FORBIDDEN(HttpStatus.FORBIDDEN, "MEMBER-403", "접근 권한이 없습니다."),
    NOT_PARTY_MEMBER(HttpStatus.FORBIDDEN, "MEMBER-402", "파티 멤버가 아닙니다."),

    // General
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-400", "잘못된 요청입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
