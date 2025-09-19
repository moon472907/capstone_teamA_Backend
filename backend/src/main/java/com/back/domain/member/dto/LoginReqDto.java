package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginReqDto(
        @NotBlank
        String email,
        @NotBlank
        String password
) {

}