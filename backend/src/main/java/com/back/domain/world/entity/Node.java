package com.back.domain.world.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Node extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @Column(nullable = false)
    private int tileIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TileType tileType;

    /** Optional JSON string for type-specific configuration. */
    private String metadata;

    @OneToMany(mappedBy = "fromNode", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Edge> nextEdges = new ArrayList<>();

    public List<Node> getNextNodes() {
        return nextEdges.stream()
                .map(Edge::getToNode)
                .toList();
    }
}
