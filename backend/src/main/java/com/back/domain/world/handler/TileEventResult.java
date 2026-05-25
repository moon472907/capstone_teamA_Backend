package com.back.domain.world.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TileEventResult {
    private int coinsChange;
    private int starsChange;
    private Integer teleportTileId;
    private String description;
}
