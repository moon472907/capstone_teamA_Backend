package com.back.domain.game.dto;

import jakarta.validation.constraints.NotNull;

/** 방어 카드 사용 여부 선택 요청 (피격 대상이 호출). */
public record DefenseReqDto(@NotNull Boolean useDefense) {}
