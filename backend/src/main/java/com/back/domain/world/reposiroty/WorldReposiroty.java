package com.back.domain.world.reposiroty;

import com.back.domain.world.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldReposiroty extends JpaRepository<World,Integer>
{

}
