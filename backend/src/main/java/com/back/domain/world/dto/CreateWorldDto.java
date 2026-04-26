package com.back.domain.world.dto;

import com.back.domain.world.entity.World;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

public record CreateWorldDto(
        @NotBlank String mapName,
        @NotNull List<ArrayList> NodeList,
        @NotNull List<ArrayList> EdgeList) {

    public CreateWorldDto(List<ArrayList> nodeList, List<ArrayList> edgeList, String mapName) {
        this(mapName, nodeList, edgeList);
    }

    public CreateWorldDto(World world) {
        this(
                world.getEdgeList(),
                world.getNodeList(),
                world.getMapName()
        );
}
}
