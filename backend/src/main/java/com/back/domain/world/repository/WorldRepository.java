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
}
