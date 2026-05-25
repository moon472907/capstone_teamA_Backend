package com.back.domain.world.repository;

import com.back.domain.world.entity.World;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WorldRepository extends JpaRepository<World, Integer> {

    @EntityGraph(attributePaths = {"nodes"})
    @Query("SELECT w FROM World w WHERE w.id = :id")
    Optional<World> findByIdWithNodes(@Param("id") Integer id);

    boolean existsByMapName(String mapName);

    /** 가장 최근에 시드된(= id가 가장 큰) 보드. 기본 보드 선택에 사용. */
    Optional<World> findTopByOrderByIdDesc();
}
