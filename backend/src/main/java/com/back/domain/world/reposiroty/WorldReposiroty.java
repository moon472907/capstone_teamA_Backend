package com.back.domain.world.reposiroty;

import com.back.domain.world.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @deprecated Use {@link com.back.domain.world.repository.WorldRepository} instead.
 */
@Deprecated
public interface WorldReposiroty extends JpaRepository<World, Integer> {
}
