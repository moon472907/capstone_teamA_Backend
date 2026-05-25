package com.back.domain.game.entity;

import com.back.domain.player.entity.Player;
import com.back.domain.world.entity.World;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer hostMemberId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    private GameState state;

    @Column(nullable = false)
    private int maxPlayers;

    @Column(nullable = false)
    private int maxRounds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
}
