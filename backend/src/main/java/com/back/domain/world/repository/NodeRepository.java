package com.back.domain.world.repository;

import com.back.domain.world.entity.Node;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Integer> {

    List<Node> findByWorldId(Integer worldId);

    /** Fetches a node together with its outgoing edges and each edge's destination node. */
    @EntityGraph(attributePaths = {"nextEdges", "nextEdges.toNode"})
    @Query("SELECT n FROM Node n WHERE n.id = :id")
    Optional<Node> findByIdWithEdges(@Param("id") Integer id);

    /**
     * 이 칸으로 들어오는 간선의 출발 칸들 — 즉, 뒤로(역방향) 이동하면 갈 수 있는 이웃 칸.
     * 정방향 이웃(nextEdges)과 합쳐 양방향 이동 후보를 만든다.
     */
    @Query("SELECT e.fromNode FROM Edge e WHERE e.toNode.id = :id")
    List<Node> findIncomingNeighborNodes(@Param("id") Integer id);
}
