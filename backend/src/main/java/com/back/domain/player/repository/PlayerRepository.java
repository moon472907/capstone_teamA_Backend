package com.back.domain.player.repository;

import com.back.domain.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

    List<Player> findByGameId(Integer gameId);

    Optional<Player> findByGameIdAndMemberId(Integer gameId, Integer memberId);

    boolean existsByGameIdAndMemberId(Integer gameId, Integer memberId);
}
