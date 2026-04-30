package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

@Component
public class NormalTileHandler implements TileEventHandler {

    @Override
    public TileType getSupportedType() {
        return TileType.NORMAL;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        return TileEventResult.builder()
                .coinsChange(0)
                .description("보통 칸입니다. 아무 일도 일어나지 않습니다.")
                .build();
    }
}
