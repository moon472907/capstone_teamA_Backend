package com.back.domain.game.controller;

import com.back.domain.game.dto.CreateGameReqDto;
import com.back.domain.game.dto.GameStateSnapshotDto;
import com.back.domain.game.dto.JoinGameReqDto;
import com.back.domain.game.dto.RollResultDto;
import com.back.domain.game.entity.Game;
import com.back.domain.game.service.GameService;
import com.back.domain.player.entity.Player;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Game", description = "멀티플레이어 보드 게임 API")
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final Rq rq;

    /**
     * POST /api/v1/games
     * Creates a new game room. The caller automatically becomes the host and first player.
     */
    @Operation(summary = "게임 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Integer> createGame(@Valid @RequestBody CreateGameReqDto req) {
        Game game = gameService.createGame(rq.getActor(), req);
        return ApiResponse.success("201", "게임이 생성되었습니다.", game.getId());
    }

    /**
     * POST /api/v1/games/{gameId}/join
     * Joins a game that is still in WAITING state.
     */
    @Operation(summary = "게임 참여")
    @PostMapping("/{gameId}/join")
    public ApiResponse<Integer> joinGame(@PathVariable Integer gameId,
                                         @Valid @RequestBody JoinGameReqDto req) {
        Player player = gameService.joinGame(gameId, rq.getActor(), req);
        return ApiResponse.success("200", "게임에 참여했습니다.", player.getId());
    }

    /**
     * POST /api/v1/games/{gameId}/start
     * Starts the game. Requires at least 2 players.
     */
    @Operation(summary = "게임 시작")
    @PostMapping("/{gameId}/start")
    public ApiResponse<GameStateSnapshotDto> startGame(@PathVariable Integer gameId) {
        GameStateSnapshotDto snapshot = gameService.startGame(gameId, rq.getActor());
        return ApiResponse.success("200", "게임이 시작되었습니다.", snapshot);
    }

    /**
     * POST /api/v1/games/{gameId}/roll
     * Current player rolls the dice. Server executes the full turn:
     * dice roll → movement → tile event → advance turn.
     * Only valid in TURN_START state for the current player.
     */
    @Operation(summary = "주사위 굴리기")
    @PostMapping("/{gameId}/roll")
    public ApiResponse<RollResultDto> rollDice(@PathVariable Integer gameId) {
        RollResultDto result = gameService.rollDice(gameId, rq.getActor());
        return ApiResponse.success("200", "주사위를 굴렸습니다.", result);
    }

    /**
     * GET /api/v1/games/{gameId}/state
     * Returns the current game state snapshot. Used for reconnection.
     */
    @Operation(summary = "게임 상태 조회 (재접속용)")
    @GetMapping("/{gameId}/state")
    public ApiResponse<GameStateSnapshotDto> getGameState(@PathVariable Integer gameId) {
        GameStateSnapshotDto snapshot = gameService.getSnapshot(gameId, rq.getActor());
        return ApiResponse.success("200", "게임 상태를 조회했습니다.", snapshot);
    }
}
