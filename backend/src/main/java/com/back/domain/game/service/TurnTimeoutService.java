package com.back.domain.game.service;

import com.back.domain.game.entity.GameState;
import com.back.domain.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnTimeoutService {

    private final GameRepository gameRepository;
    private final GameService gameService;

    /**
     * Every 5 seconds, checks all in-progress games for turn timeouts.
     * If a player has not acted within their allotted time the server auto-rolls.
     */
    @Scheduled(fixedDelay = 5000)
    public void checkTimeouts() {
        gameRepository.findByState(GameState.IN_PROGRESS).forEach(game -> {
            try {
                gameService.autoRollIfTimeout(game.getId());
                gameService.autoSelectBranchIfTimeout(game.getId());
            } catch (Exception e) {
                log.error("Timeout check failed for game {}", game.getId(), e);
            }
        });
    }
}
