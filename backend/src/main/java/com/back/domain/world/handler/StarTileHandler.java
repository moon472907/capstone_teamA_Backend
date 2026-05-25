package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

/**
 * 스타 칸: 도착 시 스타 +1. 점수 = 스타 개수.
 */
@Component
public class StarTileHandler implements TileEventHandler {

    @Override
    public TileType getSupportedType() {
        return TileType.STAR;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        return TileEventResult.builder()
                .starsChange(1)
                .description("⭐ 스타 칸! 스타 +1")
                .build();
    }
}
