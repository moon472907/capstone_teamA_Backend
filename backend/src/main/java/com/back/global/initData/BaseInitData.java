package com.back.global.initData;

import com.back.domain.character.entity.GameCharacter;
import com.back.domain.character.repository.CharacterRepository;
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
import java.util.Arrays;
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
    private final CharacterRepository characterRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> self.initAllData();
    }

    @Transactional
    public void initAllData() {
        if (characterRepository.count() == 0) {
            initCharacters();
        }
        if (worldRepository.count() == 0) {
            initDefaultBoard();
        }
    }

    private void initCharacters() {
        List<GameCharacter> characters = List.of(
                GameCharacter.builder().characterKey("gomduri").name("곰두리").icon("🐻‍❄️").description("강원대 대표 마스코트").build(),
                GameCharacter.builder().characterKey("narae").name("나래").icon("🕊️").description("하늘을 나는 비둘기").build(),
                GameCharacter.builder().characterKey("daramji").name("다람쥐").icon("🐿️").description("캠퍼스 다람쥐").build(),
                GameCharacter.builder().characterKey("bunny").name("토끼").icon("🐰").description("춘천 옥토끼").build(),
                GameCharacter.builder().characterKey("fox").name("여우").icon("🦊").description("영리한 산여우").build(),
                GameCharacter.builder().characterKey("cat").name("고양이").icon("🐱").description("캠퍼스 길고양이").build()
        );
        characterRepository.saveAll(characters);
        log.info("Characters initialised: {} characters", characters.size());
    }

    /**
     * Creates the 46-tile campus board matching the frontend Tiled map
     * (public/assets/map_data.json).
     *
     * Mapping: tileIndex i  ↔  Phaser "node{i+1}"  (node1 = start at index 0,
     * node46 = finish at index 45). Edge order is [next, next2] so branch
     * options line up with the client's "1번 길 / 2번 길" buttons.
     *
     * The map is a race graph (node1 → node46) with several branch points.
     * Orphan nodes (14, 31, 32, 33, 34) exist in the Tiled file with no
     * outgoing edges; they are recreated here to keep index alignment but
     * are unreachable.
     */
    private void initDefaultBoard() {
        final int NODE_COUNT = 46;

        World world = World.builder()
                .mapName("강대마블 캠퍼스 보드")
                .nodes(new ArrayList<>())
                .build();
        worldRepository.save(world);

        // Tile types (1-based node numbers). Default NORMAL, with a spread of
        // reward/trap tiles to drive the coin economy.
        TileType[] types = new TileType[NODE_COUNT];
        Arrays.fill(types, TileType.NORMAL);
        for (int n : new int[]{3, 9, 16, 22, 26, 35, 42}) types[n - 1] = TileType.RANDOM_REWARD;
        for (int n : new int[]{5, 12, 19, 27, 36, 44}) types[n - 1] = TileType.TRAP;

        // Create and save all nodes first (no edges yet)
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < NODE_COUNT; i++) {
            Node node = Node.builder()
                    .world(world)
                    .tileIndex(i)
                    .tileType(types[i])
                    .nextEdges(new ArrayList<>())
                    .build();
            nodes.add(nodeRepository.save(node));
        }

        // Edges keyed by 1-based node number → [next, next2] (Tiled map graph)
        int[][] edges = new int[NODE_COUNT + 1][];
        edges[1]  = new int[]{8, 2};
        edges[2]  = new int[]{3};
        edges[3]  = new int[]{4};
        edges[4]  = new int[]{5};
        edges[5]  = new int[]{6};
        edges[6]  = new int[]{7};
        edges[7]  = new int[]{13, 12};
        edges[8]  = new int[]{9};
        edges[9]  = new int[]{30, 10};
        edges[10] = new int[]{19};
        edges[11] = new int[]{10};
        edges[12] = new int[]{17, 11};
        edges[13] = new int[]{15, 17};
        // node14 orphan
        edges[15] = new int[]{16};
        edges[16] = new int[]{18};
        edges[17] = new int[]{18};
        edges[18] = new int[]{22};
        edges[19] = new int[]{20};
        edges[20] = new int[]{21};
        edges[21] = new int[]{23};
        edges[22] = new int[]{23};
        edges[23] = new int[]{24};
        edges[24] = new int[]{25, 29};
        edges[25] = new int[]{26};
        edges[26] = new int[]{27};
        edges[27] = new int[]{28};
        edges[28] = new int[]{45};
        edges[29] = new int[]{45};
        edges[30] = new int[]{35};
        // node31~34 orphans
        edges[35] = new int[]{36};
        edges[36] = new int[]{37};
        edges[37] = new int[]{38, 39};
        edges[38] = new int[]{18, 13};
        edges[39] = new int[]{41, 40};
        edges[40] = new int[]{13, 18};
        edges[41] = new int[]{42};
        edges[42] = new int[]{43};
        edges[43] = new int[]{44};
        edges[44] = new int[]{45};
        edges[45] = new int[]{46};
        // node46 finish (no outgoing edge)

        for (int num = 1; num <= NODE_COUNT; num++) {
            int[] targets = edges[num];
            if (targets == null) continue;
            Node from = nodes.get(num - 1);
            for (int t : targets) {
                addEdge(from, nodes.get(t - 1));
            }
        }

        for (Node node : nodes) {
            nodeRepository.save(node);
        }

        log.info("Campus board initialised: '{}', {} tiles", world.getMapName(), nodes.size());
    }

    private void addEdge(Node from, Node to) {
        Edge edge = Edge.builder()
                .fromNode(from)
                .toNode(to)
                .build();
        from.getNextEdges().add(edge);
    }
}
