package com.back.domain.member.dto;

import java.time.LocalDate;

public record ModifyReqDto(
        String name,
        LocalDate birth
) {
}
