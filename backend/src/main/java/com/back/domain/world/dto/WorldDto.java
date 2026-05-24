package com.back.domain.world.dto;

import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.World;

import java.util.Comparator;
import java.util.List;

/**
 * 보드 조회용 DTO. World 엔티티를 그대로 직렬화하면 Edge→Node 순환 참조와
 * 지연 로딩 문제가 생기므로, 클라이언트 렌더링에 필요한 정보만 평탄화한다.
 */
public record WorldDto(
        Integer id,
        String mapName,
        List<NodeDto> nodes
) {
    public record NodeDto(
            Integer id,
            int tileIndex,
            String tileType,
            List<Integer> nextNodeIds
    ) {}

    /** 노드 + 엣지를 포함한 전체 보드. 트랜잭션 안에서 호출해야 한다(지연 로딩). */
    public static WorldDto full(World world) {
        List<NodeDto> nodeDtos = world.getNodes().stream()
                .sorted(Comparator.comparingInt(Node::getTileIndex))
                .map(n -> new NodeDto(
                        n.getId(),
                        n.getTileIndex(),
                        n.getTileType().name(),
                        n.getNextNodes().stream().map(Node::getId).toList()
                ))
                .toList();
        return new WorldDto(world.getId(), world.getMapName(), nodeDtos);
    }

    /** 목록용 요약(노드 제외). */
    public static WorldDto summary(World world) {
        return new WorldDto(world.getId(), world.getMapName(), null);
    }
}
