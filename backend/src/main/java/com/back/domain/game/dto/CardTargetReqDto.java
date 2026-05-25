package com.back.domain.game.dto;

import jakarta.validation.constraints.NotNull;

/** 공격 카드의 대상(상대) 지정 요청. */
public record CardTargetReqDto(@NotNull Integer targetPlayerId) {}
