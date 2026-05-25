package com.back.domain.player.entity;

import com.back.domain.game.entity.Game;
import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // EAGER required: Member uses @SoftDelete, which Hibernate 6 cannot load lazily
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String nickname;

    private String characterKey;

    /** Turn order within the game (0-based). */
    private int playerIndex;

    /** Whether this player has pressed ready in the waiting room. */
    private boolean ready;

    /** Starting tile ID (set at game start). */
    private int startTileId;

    /** Final coin count — populated when game ends. */
    private int finalCoins;

    /** Final star count (= score) — populated when game ends. */
    private int finalStars;

    /** Final GPA score — populated when game ends. */
    private int finalGpa;

    /** Final rank (1 = winner) — populated when game ends. */
    private int finalRank;
}
