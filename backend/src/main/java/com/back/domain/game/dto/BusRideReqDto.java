package com.back.domain.game.dto;

import jakarta.validation.constraints.NotNull;

/** 두리버스 도착 정류장 선택 요청. */
public record BusRideReqDto(@NotNull Integer destinationTileId) {}
