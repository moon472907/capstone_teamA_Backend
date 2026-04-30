package com.back.domain.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateGameReqDto(
        @Min(2) @Max(4) int maxPlayers,
        Integer boardId,
        @NotBlank String hostNickname
) {}
