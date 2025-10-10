package com.back.domain.mission.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class MissionException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public MissionException(MissionErrorCode missionErrorCode) {
        super(missionErrorCode.getMessage());
        this.httpStatus = missionErrorCode.getHttpStatus();
        this.code = missionErrorCode.getCode();
        this.message = missionErrorCode.getMessage();
    }


}
