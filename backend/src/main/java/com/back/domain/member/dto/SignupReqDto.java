package com.back.domain.member.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupReqDto(
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotBlank
        String name
) {

}