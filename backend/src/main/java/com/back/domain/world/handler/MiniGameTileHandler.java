package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 미니게임 칸 (v1 임시 구현).
 * 실제 미니게임 대신 무작위 등수(1~4)를 정해 등수별 스타를 지급한다.
 * 실제 미니게임은 추후 구현 예정.
 */
@Component
public class MiniGameTileHandler implements TileEventHandler {

    private static final int[] STARS_BY_RANK = {3, 2, 1, 0}; // 1등→+3 ... 4등→0
    private final Random random = new Random();

    @Override
    public TileType getSupportedType() {
        return TileType.MINIGAME;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        int rank = random.nextInt(STARS_BY_RANK.length) + 1; // 1~4
        int stars = STARS_BY_RANK[rank - 1];
        return TileEventResult.builder()
                .starsChange(stars)
                .description("🎮 미니게임 " + rank + "등! 스타 +" + stars)
                .build();
    }
}
