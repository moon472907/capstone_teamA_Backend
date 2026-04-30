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
}
