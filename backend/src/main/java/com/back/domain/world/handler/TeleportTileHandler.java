package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import com.back.domain.world.repository.NodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class TeleportTileHandler implements TileEventHandler {

    private final NodeRepository nodeRepository;
    private final Random random = new Random();

    @Override
    public TileType getSupportedType() {
        return TileType.TELEPORT;
    }

    @Override
    public TileEventResult handle(PlayerSession player, GameSession session, Node tile) {
        List<Node> allNodes = nodeRepository.findByWorldId(session.getBoardId());
        if (allNodes.isEmpty()) {
            return TileEventResult.builder()
                    .coinsChange(0)
                    .description("텔레포트 칸! (이동 가능한 칸 없음)")
                    .build();
        }
        Node destination = allNodes.get(random.nextInt(allNodes.size()));
        return TileEventResult.builder()
                .coinsChange(0)
                .teleportTileId(destination.getId())
                .description("텔레포트 칸! " + destination.getTileIndex() + "번 칸으로 이동합니다.")
                .build();
    }
}
