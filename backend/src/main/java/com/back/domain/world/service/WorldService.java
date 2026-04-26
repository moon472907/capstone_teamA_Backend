package com.back.domain.world.service;


import com.back.domain.world.dto.CreateWorldDto;
import com.back.domain.world.entity.World;
import com.back.domain.world.reposiroty.WorldReposiroty;
import org.springframework.stereotype.Service;

@Service
public class WorldService {
    private WorldReposiroty worldReposiroty;
    public void createWorld(CreateWorldDto createWorldDto)
    {
        World world = new World();
        world.setMapName(createWorldDto.mapName());
        world.setNodeList(createWorldDto.NodeList());
        world.setEdgeList(createWorldDto.EdgeList());
        worldReposiroty.save(world);
    }


}
