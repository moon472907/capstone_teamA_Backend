package com.back.domain.game.service;

import com.back.domain.game.entity.Game;
import com.back.domain.game.entity.GameState;
import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.game.repository.GameRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.player.entity.Player;
import com.back.domain.player.repository.PlayerRepository;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;
import com.back.domain.world.entity.World;
import com.back.domain.world.handler.TileEventHandlerFactory;
import com.back.domain.world.repository.NodeRepository;
import com.back.domain.world.repository.WorldRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.redis.RedisGameStateService;
import com.back.global.redis.RedisLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameRepository gameRepository;
    @Mock PlayerRepository playerRepository;
    @Mock WorldRepository worldRepository;
    @Mock NodeRepository nodeRepository;
    @Mock RedisGameStateService redisGameStateService;
    @Mock RedisLockService redisLockService;
    @Mock TileEventHandlerFactory tileEventHandlerFactory;
    @Mock SimpMessagingTemplate messagingTemplate;

    @InjectMocks GameService gameService;

    private Member member1;
    private Member member2;
    private Member member3;
    private Member member4;
    private World world;
    private Node startNode;

    @BeforeEach
    void setUp() {
        member1 = new Member(1, "player1@test.com");
        member2 = new Member(2, "player2@test.com");
        member3 = new Member(3, "player3@test.com");
        member4 = new Member(4, "player4@test.com");

        startNode = Node.builder()
                .tileIndex(0)
                .tileType(TileType.NORMAL)
                .nextEdges(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(startNode, "id", 10);

        world = World.builder()
                .mapName("테스트 보드")
                .nodes(List.of(startNode))
                .build();
        ReflectionTestUtils.setField(world, "id", 1);
    }

    // ── startGame ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("4명이 모두 참여하면 게임이 정상 시작된다")
    void startGame_createsSessionInRedisWithTurnStartState() {
        Game game = buildWaitingGame();
        Player p1 = buildPlayer(game, member1, 0);
        Player p2 = buildPlayer(game, member2, 1);
        Player p3 = buildPlayer(game, member3, 2);
        Player p4 = buildPlayer(game, member4, 3);

        given(gameRepository.findById(anyInt())).willReturn(Optional.of(game));
        given(playerRepository.findByGameId(anyInt())).willReturn(List.of(p1, p2, p3, p4));
        given(gameRepository.save(any())).willReturn(game);

        gameService.startGame(1, member1);

        verify(redisGameStateService).saveSession(any(GameSession.class));
    }

    @Test
    @DisplayName("3명 이하이면 게임 시작을 거부한다")
    void startGame_rejectsIfLessThan4Players() {
        Game game = buildWaitingGame();
        Player p1 = buildPlayer(game, member1, 0);
        Player p2 = buildPlayer(game, member2, 1);
        Player p3 = buildPlayer(game, member3, 2);

        given(gameRepository.findById(anyInt())).willReturn(Optional.of(game));
        given(playerRepository.findByGameId(anyInt())).willReturn(List.of(p1, p2, p3));

        assertThatThrownBy(() -> gameService.startGame(1, member1))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("4명이 모두");
    }

    @Test
    @DisplayName("이미 진행 중인 게임은 시작을 거부한다")
    void startGame_rejectsIfNotWaiting() {
        Game game = buildWaitingGame();
        game.setState(GameState.IN_PROGRESS);

        given(gameRepository.findById(anyInt())).willReturn(Optional.of(game));

        assertThatThrownBy(() -> gameService.startGame(1, member1))
                .isInstanceOf(CustomException.class);
    }

    // ── rollDice state-machine ─────────────────────────────────────────────────

    @Test
    @DisplayName("TURN_START 상태가 아닐 때 주사위 굴리기를 거부한다")
    void rollDice_rejectsInvalidState() {
        GameSession session = buildSession(GameState.DICE_ROLL, 0);

        given(redisLockService.tryLock(anyInt(), anyString())).willReturn(true);
        given(redisGameStateService.loadSession(anyInt())).willReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.rollDice(1, member1))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("현재 차례가 아닌 플레이어의 주사위 굴리기를 거부한다")
    void rollDice_rejectsWrongPlayer() {
        GameSession session = buildSession(GameState.TURN_START, 0); // index 0 = member1

        given(redisLockService.tryLock(anyInt(), anyString())).willReturn(true);
        given(redisGameStateService.loadSession(anyInt())).willReturn(Optional.of(session));

        assertThatThrownBy(() -> gameService.rollDice(1, member2))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.NOT_YOUR_TURN);
    }

    @Test
    @DisplayName("다른 액션이 처리 중일 때 주사위 굴리기를 거부한다")
    void rollDice_rejectsWhenLocked() {
        given(redisLockService.tryLock(anyInt(), anyString())).willReturn(false);

        assertThatThrownBy(() -> gameService.rollDice(1, member1))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.GAME_ACTION_LOCKED);
    }

    // ── GameSession state machine ──────────────────────────────────────────────

    @Test
    @DisplayName("advanceTurn: 마지막 플레이어(index=3) 이후 라운드가 증가한다")
    void gameSession_advanceTurnIncrementsRound() {
        GameSession session = buildSession(GameState.TURN_START, 3); // 4 players, last one
        int prevRound = session.getRound();

        session.advanceTurn();

        assertThat(session.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(session.getRound()).isEqualTo(prevRound + 1);
    }

    @Test
    @DisplayName("advanceTurn: 중간 플레이어는 라운드가 증가하지 않는다")
    void gameSession_advanceTurnMidTurnNoRoundIncrement() {
        GameSession session = buildSession(GameState.TURN_START, 0);
        int prevRound = session.getRound();

        session.advanceTurn();

        assertThat(session.getCurrentPlayerIndex()).isEqualTo(1);
        assertThat(session.getRound()).isEqualTo(prevRound);
    }

    @Test
    @DisplayName("8라운드 마지막 플레이어(index=3) 이후 게임 완료로 판정된다")
    void gameSession_isGameCompleteAfterLastPlayerLastRound() {
        GameSession session = buildSession(GameState.TURN_START, 3);
        session.setRound(8);
        session.setMaxRounds(8);

        assertThat(session.isGameComplete()).isTrue();
    }

    @Test
    @DisplayName("8라운드 첫 번째 플레이어 차례에는 게임 미완료로 판정된다")
    void gameSession_isNotCompleteAfterFirstPlayerLastRound() {
        GameSession session = buildSession(GameState.TURN_START, 0);
        session.setRound(8);
        session.setMaxRounds(8);

        assertThat(session.isGameComplete()).isFalse();
    }

    @Test
    @DisplayName("8라운드 4명: 총 32번의 턴 후 게임 완료 판정이 된다")
    void gameSession_simulateFull8Rounds() {
        GameSession session = buildSession(GameState.TURN_START, 0);
        session.setRound(1);
        session.setMaxRounds(8);

        // Mirrors the real turn loop: act first, then check completion, then advance.
        int turnCount = 0;
        for (;;) {
            turnCount++;                       // this player takes their turn
            if (session.isGameComplete()) break;
            session.advanceTurn();
        }
        // 4 players × 8 rounds = 32 turns total
        assertThat(turnCount).isEqualTo(32);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Game buildWaitingGame() {
        Game game = Game.builder()
                .state(GameState.WAITING)
                .maxPlayers(4)
                .maxRounds(8)
                .world(world)
                .players(new ArrayList<>())
                .build();
        ReflectionTestUtils.setField(game, "id", 1);
        return game;
    }

    private Player buildPlayer(Game game, Member member, int index) {
        Player player = Player.builder()
                .game(game)
                .member(member)
                .nickname("Player" + (index + 1))
                .playerIndex(index)
                .startTileId(10)
                .build();
        ReflectionTestUtils.setField(player, "id", 100 + index);
        return player;
    }

    private GameSession buildSession(GameState state, int currentPlayerIndex) {
        List<PlayerSession> players = new ArrayList<>();
        players.add(PlayerSession.builder()
                .playerId(100).memberId(member1.getId())
                .nickname("Player1").tileId(10).coins(10).connected(true).build());
        players.add(PlayerSession.builder()
                .playerId(101).memberId(member2.getId())
                .nickname("Player2").tileId(10).coins(10).connected(true).build());
        players.add(PlayerSession.builder()
                .playerId(102).memberId(member3.getId())
                .nickname("Player3").tileId(10).coins(10).connected(true).build());
        players.add(PlayerSession.builder()
                .playerId(103).memberId(member4.getId())
                .nickname("Player4").tileId(10).coins(10).connected(true).build());

        return GameSession.builder()
                .gameId(1).boardId(1)
                .state(state)
                .currentPlayerIndex(currentPlayerIndex)
                .round(1).maxRounds(8).maxPlayers(4)
                .turnStartTime(System.currentTimeMillis())
                .turnTimeoutSeconds(30)
                .players(players)
                .build();
    }
}
