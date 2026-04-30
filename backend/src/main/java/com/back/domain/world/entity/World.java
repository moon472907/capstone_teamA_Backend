package com.back.domain.world.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class World extends BaseEntity {

    @Column(nullable = false)
    private String mapName;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Node> nodes = new ArrayList<>();

    public Node getStartNode() {
        return nodes.stream()
                .filter(n -> n.getTileIndex() == 0)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("보드에 시작 타일(index=0)이 없습니다."));
    }

    public Node getNodeById(Integer id) {
        return nodes.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Node not found: id=" + id));
    }
}
