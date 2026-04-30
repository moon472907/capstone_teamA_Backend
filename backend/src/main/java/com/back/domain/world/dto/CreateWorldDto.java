package com.back.domain.world.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWorldDto(@NotBlank String mapName) {}
