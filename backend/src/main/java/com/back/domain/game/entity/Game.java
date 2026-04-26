package com.back.domain.game.entity;


import com.back.domain.player.entity.Player;
import com.back.domain.world.entity.World;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class Game extends BaseEntity {
    World world;
    Player[]  players =  new Player[4];

}
