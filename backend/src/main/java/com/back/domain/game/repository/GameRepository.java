package com.back.domain.game.repository;

import com.back.domain.game.entity.Game;
import com.back.domain.game.entity.GameState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Integer> {

    List<Game> findByState(GameState state);
}
