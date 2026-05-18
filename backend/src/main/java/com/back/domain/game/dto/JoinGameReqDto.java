package com.back.domain.game.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinGameReqDto(
        @NotBlank String nickname,
        @NotBlank String characterKey
) {}
