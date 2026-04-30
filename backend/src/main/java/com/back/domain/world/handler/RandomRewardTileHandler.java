package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomRewardTileHandler implements TileEventHandler {

    private final Random random = new Random();

    @Override
    public TileType getSupportedType() {
        return TileType.RANDOM_REWARD;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        int reward = random.nextInt(10) + 5;  // 5–14 coins
        return TileEventResult.builder()
                .coinsChange(reward)
                .description("행운 칸! +" + reward + " 코인을 획득했습니다.")
                .build();
    }
}
