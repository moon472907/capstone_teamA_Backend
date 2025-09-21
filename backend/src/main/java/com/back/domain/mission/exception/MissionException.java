package com.back.domain.mission.exception;

import org.springframework.http.HttpStatus;

public class MissionException extends RuntimeException{
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public MissionException(MissionErrorCode memberErrorCode) {
        super(memberErrorCode.getMessage());
        this.httpStatus = memberErrorCode.getHttpStatus();
        this.code = memberErrorCode.getCode();
        this.message = memberErrorCode.getMessage();
    }


}
