package com.back.domain.game.dto;

import jakarta.validation.constraints.NotNull;

public record BranchSelectReqDto(@NotNull Integer selectedNodeId) {}
