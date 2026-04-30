package com.back.global.initData;

import com.back.domain.member.service.MemberService;
import com.back.domain.world.entity.Edge;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import com.back.domain.world.entity.World;
import com.back.domain.world.repository.NodeRepository;
import com.back.domain.world.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;

    private final MemberService memberService;
    private final WorldRepository worldRepository;
    private final NodeRepository nodeRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.initAllData();
    }

    @Transactional
    public void initAllData() {
        if (worldRepository.count() == 0) {
            initDefaultBoard();
        }
    }

    /**
     * Creates a 20-tile board with a branch path.
     *
     * Main ring: 0 → 1 → 2 → … → 19 → 0
     * Branch at tile 5: 5 also connects to tile 10 (shortcut, skips 6-9)
     *
     * Tile types:
     *   NORMAL        : 0,1,3,6,7,9,11,12,14,16,19
     *   RANDOM_REWARD : 2,10,13,18
     *   TRAP          : 4,8,17
     *   TELEPORT      : 5,15
     */
    private void initDefaultBoard() {
        World world = World.builder()
                .mapName("기본 보드")
                .nodes(new ArrayList<>())
                .build();
        worldRepository.save(world);

        TileType[] types = {
                TileType.NORMAL,        // 0  start
                TileType.NORMAL,        // 1
                TileType.RANDOM_REWARD, // 2
                TileType.NORMAL,        // 3
                TileType.TRAP,          // 4
                TileType.TELEPORT,      // 5  branch point
                TileType.NORMAL,        // 6
                TileType.NORMAL,        // 7
                TileType.TRAP,          // 8
                TileType.NORMAL,        // 9
                TileType.RANDOM_REWARD, // 10 merge point / shortcut target from 5
                TileType.NORMAL,        // 11
                TileType.NORMAL,        // 12
                TileType.RANDOM_REWARD, // 13
                TileType.NORMAL,        // 14
                TileType.TELEPORT,      // 15
                TileType.NORMAL,        // 16
                TileType.TRAP,          // 17
                TileType.RANDOM_REWARD, // 18
                TileType.NORMAL,        // 19
        };

        // Create and save all 20 nodes first (no edges yet)
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            Node node = Node.builder()
                    .world(world)
                    .tileIndex(i)
                    .tileType(types[i])
                    .nextEdges(new ArrayList<>())
                    .build();
            nodes.add(nodeRepository.save(node));
        }

        // Wire edges: circular ring  (i → i+1, last → 0)
        for (int i = 0; i < nodes.size(); i++) {
            Node from = nodes.get(i);
            Node to = nodes.get((i + 1) % nodes.size());
            addEdge(from, to);
        }

        // Extra branch: tile 5 → tile 10 (shortcut)
        addEdge(nodes.get(5), nodes.get(10));

        // Persist edge changes
        for (Node node : nodes) {
            nodeRepository.save(node);
        }

        log.info("Default board initialised: '{}', {} tiles", world.getMapName(), nodes.size());
    }

    private void addEdge(Node from, Node to) {
        Edge edge = Edge.builder()
                .fromNode(from)
                .toNode(to)
                .build();
        from.getNextEdges().add(edge);
    }
}
