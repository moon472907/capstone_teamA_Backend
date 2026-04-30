package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import com.back.domain.world.repository.NodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TileEventHandlerTest {

    @Mock NodeRepository nodeRepository;

    private PlayerSession buildPlayer(int coins) {
        return PlayerSession.builder()
                .playerId(1).memberId(1).nickname("Test")
                .tileId(0).coins(coins).connected(true)
                .build();
    }

    private GameSession buildSession() {
        return GameSession.builder()
                .gameId(1).boardId(1)
                .players(new ArrayList<>())
                .build();
    }

    private Node buildNode(TileType type) {
        Node n = Node.builder().tileIndex(5).tileType(type).nextEdges(new ArrayList<>()).build();
        ReflectionTestUtils.setField(n, "id", 5);
        return n;
    }

    @Test
    @DisplayName("NORMAL 타일: 코인 변화 없음, 텔레포트 없음")
    void normal_noEffect() {
        NormalTileHandler handler = new NormalTileHandler();
        TileEventResult result = handler.handle(buildPlayer(10), buildSession(), buildNode(TileType.NORMAL));
        assertThat(result.getCoinsChange()).isEqualTo(0);
        assertThat(result.getTeleportTileId()).isNull();
    }

    @Test
    @DisplayName("RANDOM_REWARD 타일: 5~14 코인 획득")
    void randomReward_gainsBetween5And14() {
        RandomRewardTileHandler handler = new RandomRewardTileHandler();
        TileEventResult result = handler.handle(buildPlayer(10), buildSession(), buildNode(TileType.RANDOM_REWARD));
        assertThat(result.getCoinsChange()).isBetween(5, 14);
    }

    @Test
    @DisplayName("TRAP 타일: 코인이 0일 때 추가 감소 없음")
    void trap_zeroCoinsPlayerLosesNothing() {
        TrapTileHandler handler = new TrapTileHandler();
        TileEventResult result = handler.handle(buildPlayer(0), buildSession(), buildNode(TileType.TRAP));
        assertThat(result.getCoinsChange()).isEqualTo(0);
    }

    @Test
    @DisplayName("TRAP 타일: 코인이 많을 때 3~7 감소")
    void trap_richPlayerLoses3To7() {
        TrapTileHandler handler = new TrapTileHandler();
        TileEventResult result = handler.handle(buildPlayer(100), buildSession(), buildNode(TileType.TRAP));
        assertThat(result.getCoinsChange()).isBetween(-7, -3);
    }

    @Test
    @DisplayName("TELEPORT 타일: 목적지 tileId가 설정된다")
    void teleport_setsTeleportTileId() {
        TeleportTileHandler handler = new TeleportTileHandler(nodeRepository);

        Node destination = buildNode(TileType.NORMAL);
        ReflectionTestUtils.setField(destination, "id", 99);
        given(nodeRepository.findByWorldId(anyInt())).willReturn(List.of(destination));

        TileEventResult result = handler.handle(buildPlayer(10), buildSession(), buildNode(TileType.TELEPORT));
        assertThat(result.getTeleportTileId()).isEqualTo(99);
        assertThat(result.getCoinsChange()).isEqualTo(0);
    }

    @Test
    @DisplayName("TileEventHandlerFactory: TileType마다 올바른 핸들러를 반환한다")
    void factory_routesCorrectly() {
        NormalTileHandler normal = new NormalTileHandler();
        RandomRewardTileHandler reward = new RandomRewardTileHandler();
        TrapTileHandler trap = new TrapTileHandler();
        TeleportTileHandler teleport = new TeleportTileHandler(nodeRepository);

        TileEventHandlerFactory factory = new TileEventHandlerFactory(List.of(normal, reward, trap, teleport));

        assertThat(factory.getHandler(TileType.NORMAL)).isInstanceOf(NormalTileHandler.class);
        assertThat(factory.getHandler(TileType.RANDOM_REWARD)).isInstanceOf(RandomRewardTileHandler.class);
        assertThat(factory.getHandler(TileType.TRAP)).isInstanceOf(TrapTileHandler.class);
        assertThat(factory.getHandler(TileType.TELEPORT)).isInstanceOf(TeleportTileHandler.class);
    }
}
