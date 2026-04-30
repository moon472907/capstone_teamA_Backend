package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TrapTileHandler implements TileEventHandler {

    private final Random random = new Random();

    @Override
    public TileType getSupportedType() {
        return TileType.TRAP;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        int penalty = random.nextInt(5) + 3;  // 3–7 coins
        int actual = Math.min(penalty, player.getCoins());  // cannot drop below 0
        return TileEventResult.builder()
                .coinsChange(-actual)
                .description("함정 칸! -" + actual + " 코인을 잃었습니다.")
                .build();
    }
}
