package com.back.domain.world.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class World extends BaseEntity {
    private String mapName;
    private List<ArrayList> NodeList;
    private List<ArrayList> EdgeList;
}
enum NodeType
{
    normal,
    extra
}

