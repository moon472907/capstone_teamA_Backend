package com.back.domain.game.controller;

import com.back.domain.game.dto.BranchSelectReqDto;
import com.back.domain.game.dto.BusRideReqDto;
import com.back.domain.game.dto.CardTargetReqDto;
import com.back.domain.game.dto.CreateGameReqDto;
import com.back.domain.game.dto.DefenseReqDto;
import com.back.domain.game.dto.GameDetailDto;
import com.back.domain.game.dto.GameRoomDto;
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

import java.util.List;

@Tag(name = "Game", description = "멀티플레이어 보드 게임 API")
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final Rq rq;

    /**
     * GET /api/v1/games
     * Returns all game rooms currently in WAITING state.
     */
    @Operation(summary = "대기 중인 방 목록 조회")
    @GetMapping
    public ApiResponse<List<GameRoomDto>> listWaitingGames() {
        return ApiResponse.success("200", "대기 중인 방 목록입니다.", gameService.listWaitingGames());
    }

    /**
     * GET /api/v1/games/{gameId}
     * 방 상세(현재 로스터). 로비에서 실시간 슬롯/레디 구성에 사용.
     */
    @Operation(summary = "방 상세 조회 (로스터)")
    @GetMapping("/{gameId}")
    public ApiResponse<GameDetailDto> getGameDetail(@PathVariable Integer gameId) {
        return ApiResponse.success("200", "방 상세입니다.", gameService.getGameDetail(gameId));
    }

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
     * POST /api/v1/games/{gameId}/leave
     * Leaves the waiting room. Host transfer occurs if the host leaves.
     * Deletes the game if no players remain.
     */
    @Operation(summary = "방 나가기")
    @PostMapping("/{gameId}/leave")
    public ApiResponse<Void> leaveGame(@PathVariable Integer gameId) {
        gameService.leaveGame(gameId, rq.getActor());
        return ApiResponse.success("200", "방에서 나갔습니다.");
    }

    /**
     * POST /api/v1/games/{gameId}/ready
     * Toggles the calling player's ready status. Broadcasts PLAYER_READY event.
     */
    @Operation(summary = "레디 토글")
    @PostMapping("/{gameId}/ready")
    public ApiResponse<Void> ready(@PathVariable Integer gameId) {
        gameService.ready(gameId, rq.getActor());
        return ApiResponse.success("200", "레디 상태가 변경되었습니다.");
    }

    /**
     * POST /api/v1/games/{gameId}/start
     * Starts the game. Requires all 4 players to have joined.
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
     * POST /api/v1/games/{gameId}/branch
     * Current player selects a branch path. Only valid in BRANCH_SELECT state.
     */
    @Operation(summary = "분기점 선택")
    @PostMapping("/{gameId}/branch")
    public ApiResponse<RollResultDto> selectBranch(@PathVariable Integer gameId,
                                                    @Valid @RequestBody BranchSelectReqDto req) {
        RollResultDto result = gameService.selectBranch(gameId, rq.getActor(), req);
        return ApiResponse.success("200", "분기점을 선택했습니다.", result);
    }

    /**
     * POST /api/v1/games/{gameId}/card/target
     * Current player picks the opponent for an OPPONENT-targeted attack card.
     * Only valid in CARD_TARGET_SELECT state for the current player.
     */
    @Operation(summary = "공격 카드 대상 지정")
    @PostMapping("/{gameId}/card/target")
    public ApiResponse<RollResultDto> selectCardTarget(@PathVariable Integer gameId,
                                                       @Valid @RequestBody CardTargetReqDto req) {
        RollResultDto result = gameService.selectCardTarget(gameId, rq.getActor(), req);
        return ApiResponse.success("200", "카드 대상을 지정했습니다.", result);
    }

    /**
     * POST /api/v1/games/{gameId}/card/defense
     * Targeted player decides whether to spend a defense card. Only valid in
     * CARD_DEFENSE state for the targeted player.
     */
    @Operation(summary = "방어 카드 사용 여부 선택")
    @PostMapping("/{gameId}/card/defense")
    public ApiResponse<RollResultDto> resolveDefense(@PathVariable Integer gameId,
                                                     @Valid @RequestBody DefenseReqDto req) {
        RollResultDto result = gameService.resolveDefense(gameId, rq.getActor(), req);
        return ApiResponse.success("200", "방어 여부를 처리했습니다.", result);
    }

    /**
     * POST /api/v1/games/{gameId}/bus
     * Current player selects which bus stop to teleport to. Only valid in
     * BUS_SELECT state for the current player.
     */
    @Operation(summary = "두리버스 도착 정류장 선택")
    @PostMapping("/{gameId}/bus")
    public ApiResponse<RollResultDto> selectBusDestination(@PathVariable Integer gameId,
                                                           @Valid @RequestBody BusRideReqDto req) {
        RollResultDto result = gameService.selectBusDestination(gameId, rq.getActor(), req);
        return ApiResponse.success("200", "정류장을 선택했습니다.", result);
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
