package com.back.domain.game.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGameReqDto(
        Integer boardId,
        @NotBlank String hostNickname,
        @NotBlank String title,
        @NotBlank String characterKey
) {}
