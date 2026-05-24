package com.back.domain.game.service;

import com.back.domain.character.entity.GameCharacter;
import com.back.domain.character.repository.CharacterRepository;
import com.back.domain.game.dto.BranchSelectReqDto;
import com.back.domain.game.dto.CreateGameReqDto;
import com.back.domain.game.dto.GameRoomDto;
import com.back.domain.game.dto.GameStateSnapshotDto;
import com.back.domain.game.dto.JoinGameReqDto;
import com.back.domain.game.dto.RollResultDto;
import com.back.domain.game.entity.Game;
import com.back.domain.game.entity.GameState;
import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.game.repository.GameRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.player.entity.Player;
import com.back.domain.player.repository.PlayerRepository;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.World;
import com.back.domain.world.handler.TileEventHandlerFactory;
import com.back.domain.world.handler.TileEventResult;
import com.back.domain.world.repository.NodeRepository;
import com.back.domain.world.repository.WorldRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.global.redis.RedisGameStateService;
import com.back.global.redis.RedisLockService;
import com.back.global.websocket.GameMessage;
import com.back.global.websocket.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private static final int MAX_PLAYERS = 4;
    private static final int MAX_ROUNDS = 8;
    private static final int INITIAL_COINS = 10;
    private static final int TURN_TIMEOUT_SECONDS = 30;
    private static final int BRANCH_TIMEOUT_SECONDS = 20;

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final WorldRepository worldRepository;
    private final NodeRepository nodeRepository;
    private final CharacterRepository characterRepository;
    private final RedisGameStateService redisGameStateService;
    private final RedisLockService redisLockService;
    private final TileEventHandlerFactory tileEventHandlerFactory;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberService memberService;
    private final Random random = new Random();

    // ─────────────────────────────────────────────
    //  List Waiting Games
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<GameRoomDto> listWaitingGames() {
        return gameRepository.findByState(GameState.WAITING)
                .stream()
                .map(GameRoomDto::from)
                .toList();
    }

    // ─────────────────────────────────────────────
    //  Create Game
    // ─────────────────────────────────────────────

    @Transactional
    public Game createGame(Member host, CreateGameReqDto req) {
        GameCharacter character = resolveCharacter(req.characterKey());

        World world = resolveBoard(req.boardId());

        Game game = Game.builder()
                .title(req.title())
                .hostMemberId(host.getId())
                .state(GameState.WAITING)
                .maxPlayers(MAX_PLAYERS)
                .maxRounds(MAX_ROUNDS)
                .world(world)
                .build();
        Game saved = gameRepository.save(game);

        joinGameInternal(saved, world, host, req.hostNickname(), character.getCharacterKey());

        log.info("Game created: id={}, maxPlayers={}", saved.getId(), MAX_PLAYERS);
        return saved;
    }

    // ─────────────────────────────────────────────
    //  Leave Game
    // ─────────────────────────────────────────────

    @Transactional
    public void leaveGame(Integer gameId, Member member) {
        Game game = findGame(gameId);
        if (game.getState() != GameState.WAITING) {
            throw new CustomException(ErrorCode.GAME_NOT_WAITING);
        }

        Player player = playerRepository.findByGameIdAndMemberId(gameId, member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        String nickname = player.getNickname();
        playerRepository.delete(player);

        List<Player> remaining = playerRepository.findByGameId(gameId);

        if (remaining.isEmpty()) {
            gameRepository.delete(game);
            return;
        }

        // 방장이 나갔으면 playerIndex가 가장 낮은 플레이어에게 위임
        if (game.getHostMemberId().equals(member.getId())) {
            Player newHost = remaining.stream()
                    .min(Comparator.comparingInt(Player::getPlayerIndex))
                    .orElseThrow();
            game.setHostMemberId(newHost.getMember().getId());
            gameRepository.save(game);

            broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_LEFT, gameId,
                    Map.of("memberId", member.getId(),
                            "nickname", nickname,
                            "newHostMemberId", newHost.getMember().getId(),
                            "newHostNickname", newHost.getNickname(),
                            "remainingCount", remaining.size())));
        } else {
            broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_LEFT, gameId,
                    Map.of("memberId", member.getId(),
                            "nickname", nickname,
                            "remainingCount", remaining.size())));
        }
    }

    // ─────────────────────────────────────────────
    //  Ready
    // ─────────────────────────────────────────────

    @Transactional
    public void ready(Integer gameId, Member member) {
        Game game = findGame(gameId);
        if (game.getState() != GameState.WAITING) {
            throw new CustomException(ErrorCode.GAME_NOT_WAITING);
        }

        Player player = playerRepository.findByGameIdAndMemberId(gameId, member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        player.setReady(!player.isReady());
        playerRepository.save(player);

        List<Player> players = playerRepository.findByGameId(gameId);
        long readyCount = players.stream().filter(Player::isReady).count();

        broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_READY, gameId,
                Map.of("memberId", member.getId(),
                        "nickname", player.getNickname(),
                        "ready", player.isReady(),
                        "readyCount", readyCount,
                        "totalCount", players.size())));
    }

    // ─────────────────────────────────────────────
    //  Join Game
    // ─────────────────────────────────────────────

    @Transactional
    public Player joinGame(Integer gameId, Member member, JoinGameReqDto req) {
        Game game = findGame(gameId);

        if (game.getState() != GameState.WAITING) {
            throw new CustomException(ErrorCode.GAME_NOT_WAITING);
        }
        if (game.isFull()) {
            throw new CustomException(ErrorCode.GAME_FULL);
        }
        if (playerRepository.existsByGameIdAndMemberId(gameId, member.getId())) {
            throw new CustomException(ErrorCode.PLAYER_ALREADY_IN_GAME);
        }

        GameCharacter character = resolveCharacter(req.characterKey());
        boolean characterTaken = playerRepository.findByGameId(gameId).stream()
                .anyMatch(p -> req.characterKey().equals(p.getCharacterKey()));
        if (characterTaken) {
            throw new CustomException(ErrorCode.CHARACTER_ALREADY_TAKEN);
        }

        World world = game.getWorld();
        Player player = joinGameInternal(game, world, member, req.nickname(), character.getCharacterKey());

        broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_JOINED, gameId,
                Map.of("playerId", player.getId(),
                        "nickname", player.getNickname(),
                        "playerIndex", player.getPlayerIndex(),
                        "playerCount", game.getPlayerCount())));

        log.info("Player joined: gameId={}, playerId={}, nickname={}", gameId, player.getId(), req.nickname());
        return player;
    }

    private Player joinGameInternal(Game game, World world, Member member, String nickname, String characterKey) {
        int playerIndex = game.getPlayerCount();
        Node startNode = world.getStartNode();

        Player player = Player.builder()
                .game(game)
                .member(member)
                .nickname(nickname)
                .characterKey(characterKey)
                .playerIndex(playerIndex)
                .startTileId(startNode.getId())
                .build();
        return playerRepository.save(player);
    }

    // ─────────────────────────────────────────────
    //  Start Game
    // ─────────────────────────────────────────────

    @Transactional
    public GameStateSnapshotDto startGame(Integer gameId, Member requester) {
        Game game = findGame(gameId);

        if (game.getState() != GameState.WAITING) {
            throw new CustomException(ErrorCode.GAME_NOT_WAITING);
        }
        if (!game.getHostMemberId().equals(requester.getId())) {
            throw new CustomException(ErrorCode.NOT_HOST);
        }

        List<Player> players = playerRepository.findByGameId(gameId);
        if (players.size() < MAX_PLAYERS) {
            throw new CustomException(ErrorCode.GAME_NOT_ENOUGH_PLAYERS);
        }
        boolean allReady = players.stream().allMatch(Player::isReady);
        if (!allReady) {
            throw new CustomException(ErrorCode.GAME_NOT_ALL_READY);
        }

        Node startNode = game.getWorld().getStartNode();

        List<PlayerSession> playerSessions = players.stream()
                .sorted(Comparator.comparingInt(Player::getPlayerIndex))
                .map(p -> PlayerSession.builder()
                        .playerId(p.getId())
                        .memberId(p.getMember().getId())
                        .nickname(p.getNickname())
                        .characterKey(p.getCharacterKey())
                        .tileId(startNode.getId())
                        .coins(INITIAL_COINS)
                        .connected(true)
                        .build())
                .toList();

        GameSession session = GameSession.builder()
                .gameId(gameId)
                .boardId(game.getWorld().getId())
                .state(GameState.TURN_START)
                .currentPlayerIndex(0)
                .round(1)
                .maxRounds(MAX_ROUNDS)
                .maxPlayers(game.getMaxPlayers())
                .turnStartTime(System.currentTimeMillis())
                .turnTimeoutSeconds(TURN_TIMEOUT_SECONDS)
                .players(new ArrayList<>(playerSessions))
                .build();

        redisGameStateService.saveSession(session);

        game.setState(GameState.IN_PROGRESS);
        gameRepository.save(game);

        GameStateSnapshotDto snapshot = GameStateSnapshotDto.from(session);
        broadcastToGame(gameId, GameMessage.of(MessageType.GAME_STARTED, gameId, snapshot));
        broadcastTurnStart(session);

        log.info("Game started: id={}, players={}", gameId, players.size());
        return snapshot;
    }

    // ─────────────────────────────────────────────
    //  Roll Dice  (main turn action)
    // ─────────────────────────────────────────────

    @Transactional
    public RollResultDto rollDice(Integer gameId, Member member) {
        String lockValue = UUID.randomUUID().toString();
        if (!redisLockService.tryLock(gameId, lockValue)) {
            throw new CustomException(ErrorCode.GAME_ACTION_LOCKED);
        }
        try {
            return executeTurn(gameId, member);
        } finally {
            redisLockService.unlock(gameId, lockValue);
        }
    }

    private RollResultDto executeTurn(Integer gameId, Member member) {
        GameSession session = loadActiveSession(gameId);
        requireState(session, GameState.TURN_START);
        requireCurrentPlayer(session, member);

        PlayerSession currentPlayer = session.getCurrentPlayer();
        int fromTileId = currentPlayer.getTileId();

        // ── 1. Dice roll ──────────────────────────
        session.setState(GameState.DICE_ROLL);
        int diceValue = random.nextInt(6) + 1;

        broadcastToGame(gameId, GameMessage.of(MessageType.DICE_ROLLED, gameId, Map.of(
                "playerId", currentPlayer.getPlayerId(),
                "nickname", currentPlayer.getNickname(),
                "diceValue", diceValue)));

        // ── 2. Move ──────────────────────────────
        session.setState(GameState.MOVE);
        TraversalResult traversal = traverseGraph(fromTileId, diceValue);

        if (traversal.isBranchRequired()) {
            currentPlayer.setTileId(traversal.currentNodeId());
            session.setState(GameState.BRANCH_SELECT);
            session.setPendingBranchNodeIds(traversal.branchNodeIds());
            session.setPendingRemainingSteps(traversal.remainingSteps());
            session.setBranchSelectStartTime(System.currentTimeMillis());
            session.setBranchTimeoutSeconds(BRANCH_TIMEOUT_SECONDS);
            redisGameStateService.saveSession(session);

            broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_MOVED, gameId, Map.of(
                    "playerId", currentPlayer.getPlayerId(),
                    "fromTileId", fromTileId,
                    "toTileId", traversal.currentNodeId())));

            broadcastToGame(gameId, GameMessage.of(MessageType.BRANCH_REQUIRED, gameId, Map.of(
                    "playerId", currentPlayer.getPlayerId(),
                    "branchOptions", traversal.branchNodeIds(),
                    "timeoutSeconds", BRANCH_TIMEOUT_SECONDS)));

            return RollResultDto.builder()
                    .diceValue(diceValue)
                    .fromTileId(fromTileId)
                    .toTileId(traversal.currentNodeId())
                    .nextState(GameState.BRANCH_SELECT)
                    .gameEnded(false)
                    .branchOptions(traversal.branchNodeIds())
                    .build();
        }

        return completeTurn(session, gameId, currentPlayer, fromTileId, diceValue, traversal.destination());
    }

    // ─────────────────────────────────────────────
    //  Get Snapshot  (reconnection support)
    // ─────────────────────────────────────────────

    public GameStateSnapshotDto getSnapshot(Integer gameId, Member member) {
        GameSession session = loadActiveSession(gameId);

        session.getPlayers().stream()
                .filter(p -> p.getMemberId().equals(member.getId()))
                .findFirst()
                .ifPresent(p -> p.setConnected(true));

        redisGameStateService.saveSession(session);
        return GameStateSnapshotDto.from(session);
    }

    // ─────────────────────────────────────────────
    //  Branch Select
    // ─────────────────────────────────────────────

    @Transactional
    public RollResultDto selectBranch(Integer gameId, Member member, BranchSelectReqDto req) {
        String lockValue = UUID.randomUUID().toString();
        if (!redisLockService.tryLock(gameId, lockValue)) {
            throw new CustomException(ErrorCode.GAME_ACTION_LOCKED);
        }
        try {
            GameSession session = loadActiveSession(gameId);
            requireState(session, GameState.BRANCH_SELECT);
            requireCurrentPlayer(session, member);

            if (!session.getPendingBranchNodeIds().contains(req.selectedNodeId())) {
                throw new CustomException(ErrorCode.INVALID_BRANCH_SELECTION);
            }

            PlayerSession currentPlayer = session.getCurrentPlayer();
            int fromTileId = currentPlayer.getTileId();

            TraversalResult traversal = traverseGraph(req.selectedNodeId(), session.getPendingRemainingSteps());

            if (traversal.isBranchRequired()) {
                currentPlayer.setTileId(traversal.currentNodeId());
                session.setPendingBranchNodeIds(traversal.branchNodeIds());
                session.setPendingRemainingSteps(traversal.remainingSteps());
                session.setBranchSelectStartTime(System.currentTimeMillis());
                redisGameStateService.saveSession(session);

                broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_MOVED, gameId, Map.of(
                        "playerId", currentPlayer.getPlayerId(),
                        "fromTileId", fromTileId,
                        "toTileId", traversal.currentNodeId())));

                broadcastToGame(gameId, GameMessage.of(MessageType.BRANCH_REQUIRED, gameId, Map.of(
                        "playerId", currentPlayer.getPlayerId(),
                        "branchOptions", traversal.branchNodeIds(),
                        "timeoutSeconds", BRANCH_TIMEOUT_SECONDS)));

                return RollResultDto.builder()
                        .fromTileId(fromTileId)
                        .toTileId(traversal.currentNodeId())
                        .nextState(GameState.BRANCH_SELECT)
                        .gameEnded(false)
                        .branchOptions(traversal.branchNodeIds())
                        .build();
            }

            return completeTurn(session, gameId, currentPlayer, fromTileId, 0, traversal.destination());
        } finally {
            redisLockService.unlock(gameId, lockValue);
        }
    }

    // ─────────────────────────────────────────────
    //  Auto-roll on timeout  (called by scheduler)
    // ─────────────────────────────────────────────

    @Transactional
    public void autoSelectBranchIfTimeout(Integer gameId) {
        GameSession session = redisGameStateService.loadSession(gameId).orElse(null);
        if (session == null || session.getState() != GameState.BRANCH_SELECT) return;

        long elapsed = System.currentTimeMillis() - session.getBranchSelectStartTime();
        if (elapsed < (long) session.getBranchTimeoutSeconds() * 1000) return;

        PlayerSession current = session.getCurrentPlayer();
        List<Integer> options = session.getPendingBranchNodeIds();
        int randomNodeId = options.get(random.nextInt(options.size()));

        log.info("Branch timeout auto-select: gameId={}, player={}, nodeId={}", gameId, current.getNickname(), randomNodeId);

        Member autoMember = new Member(current.getMemberId(), "");
        try {
            selectBranch(gameId, autoMember, new BranchSelectReqDto(randomNodeId));
        } catch (Exception e) {
            log.error("Auto branch select failed: gameId={}", gameId, e);
        }
    }

    @Transactional
    public void autoRollIfTimeout(Integer gameId) {
        GameSession session = redisGameStateService.loadSession(gameId).orElse(null);
        if (session == null || session.getState() != GameState.TURN_START) return;

        long elapsed = System.currentTimeMillis() - session.getTurnStartTime();
        if (elapsed < (long) session.getTurnTimeoutSeconds() * 1000) return;

        PlayerSession current = session.getCurrentPlayer();
        log.info("Timeout auto-roll: gameId={}, player={}", gameId, current.getNickname());

        // Create a synthetic member representing the current player for validation bypass
        Member autoMember = new Member(current.getMemberId(), "");
        try {
            rollDice(gameId, autoMember);
        } catch (Exception e) {
            log.error("Auto-roll failed: gameId={}", gameId, e);
        }
    }

    // ─────────────────────────────────────────────
    //  Internal helpers
    // ─────────────────────────────────────────────

    private RollResultDto completeTurn(GameSession session, Integer gameId,
                                       PlayerSession currentPlayer, int fromTileId,
                                       int diceValue, Node destination) {
        currentPlayer.setTileId(destination.getId());

        broadcastToGame(gameId, GameMessage.of(MessageType.PLAYER_MOVED, gameId, Map.of(
                "playerId", currentPlayer.getPlayerId(),
                "fromTileId", fromTileId,
                "toTileId", destination.getId(),
                "tileIndex", destination.getTileIndex())));

        session.setState(GameState.TILE_EVENT);
        TileEventResult eventResult = tileEventHandlerFactory
                .getHandler(destination.getTileType())
                .handle(currentPlayer, session, destination);

        applyTileEffect(currentPlayer, eventResult);

        broadcastToGame(gameId, GameMessage.of(MessageType.TILE_TRIGGERED, gameId,
                buildTilePayload(currentPlayer, destination, eventResult)));

        session.setState(GameState.TURN_END);
        boolean gameOver = session.isGameComplete();

        if (gameOver) {
            session.setState(GameState.GAME_END);
            redisGameStateService.saveSession(session);
            persistGameEnd(gameId, session);
        } else {
            session.advanceTurn();
            session.setState(GameState.TURN_START);
            redisGameStateService.saveSession(session);
            broadcastTurnStart(session);
        }

        return RollResultDto.builder()
                .diceValue(diceValue)
                .fromTileId(fromTileId)
                .toTileId(currentPlayer.getTileId())
                .tileIndex(destination.getTileIndex())
                .tileType(destination.getTileType().name())
                .coinsChange(eventResult.getCoinsChange())
                .totalCoins(currentPlayer.getCoins())
                .tileEventDescription(eventResult.getDescription())
                .nextState(session.getState())
                .gameEnded(gameOver)
                .build();
    }

    /**
     * Traverses the board graph step by step.
     * Stops at the first branch point and returns options + remaining steps.
     * If no branch is encountered, returns the final destination.
     */
    private TraversalResult traverseGraph(Integer startNodeId, int steps) {
        Node current = nodeRepository.findByIdWithEdges(startNodeId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        for (int i = 0; i < steps; i++) {
            List<Node> nextNodes = current.getNextNodes();
            if (nextNodes.isEmpty()) break;

            if (nextNodes.size() > 1) {
                // Branch point — stop and return options
                List<Integer> branchNodeIds = nextNodes.stream()
                        .map(Node::getId)
                        .toList();
                return TraversalResult.branch(current.getId(), branchNodeIds, steps - i - 1);
            }

            Node next = nextNodes.get(0);
            current = nodeRepository.findByIdWithEdges(next.getId()).orElse(next);
        }
        return TraversalResult.completed(current);
    }

    private record TraversalResult(Node destination, Integer currentNodeId,
                                   List<Integer> branchNodeIds, int remainingSteps) {
        static TraversalResult completed(Node destination) {
            return new TraversalResult(destination, null, null, 0);
        }

        static TraversalResult branch(Integer currentNodeId, List<Integer> options, int remaining) {
            return new TraversalResult(null, currentNodeId, options, remaining);
        }

        boolean isBranchRequired() {
            return branchNodeIds != null;
        }
    }

    private void applyTileEffect(PlayerSession player, TileEventResult result) {
        // Coins can never go below 0
        int newCoins = Math.max(0, player.getCoins() + result.getCoinsChange());
        player.setCoins(newCoins);

        if (result.getTeleportTileId() != null) {
            player.setTileId(result.getTeleportTileId());
        }
    }

    private Map<String, Object> buildTilePayload(PlayerSession player,
                                                  Node tile,
                                                  TileEventResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("playerId", player.getPlayerId());
        map.put("tileType", tile.getTileType().name());
        map.put("coinsChange", result.getCoinsChange());
        map.put("totalCoins", player.getCoins());
        map.put("description", result.getDescription());
        if (result.getTeleportTileId() != null) {
            map.put("teleportTileId", result.getTeleportTileId());
        }
        return map;
    }

    @Transactional
    protected void persistGameEnd(Integer gameId, GameSession session) {
        List<PlayerSession> ranked = session.getPlayers().stream()
                .sorted(Comparator.comparingInt(PlayerSession::getCoins).reversed())
                .toList();

        for (int rank = 0; rank < ranked.size(); rank++) {
            PlayerSession ps = ranked.get(rank);
            int finalRank = rank + 1;
            playerRepository.findById(ps.getPlayerId()).ifPresent(p -> {
                p.setFinalCoins(ps.getCoins());
                p.setFinalGpa(ps.getGpa());
                p.setFinalRank(finalRank);
                playerRepository.save(p);
            });
        }

        gameRepository.findById(gameId).ifPresent(g -> {
            g.setState(GameState.GAME_END);
            gameRepository.save(g);
        });

        List<Map<String, Object>> results = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            PlayerSession ps = ranked.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("playerId", ps.getPlayerId());
            entry.put("nickname", ps.getNickname());
            entry.put("coins", ps.getCoins());
            entry.put("rank", i + 1);
            results.add(entry);
        }

        // 게임 종료된 플레이어들의 기존 토큰을 모두 무효화한 뒤 결과를 broadcast.
        // 순서가 뒤집히면 클라이언트가 GAME_ENDED 수신 직후 /refresh 로 토큰을 연장할 수 있음.
        for (PlayerSession ps : ranked) {
            memberService.invalidateTokens(ps.getMemberId());
        }

        broadcastToGame(gameId, GameMessage.of(MessageType.GAME_ENDED, gameId, Map.of("results", results)));
        redisGameStateService.deleteSession(gameId);

        log.info("Game ended: id={}, winner={}", gameId, ranked.isEmpty() ? "none" : ranked.get(0).getNickname());
    }

    private void broadcastTurnStart(GameSession session) {
        PlayerSession current = session.getCurrentPlayer();
        broadcastToGame(session.getGameId(), GameMessage.of(MessageType.TURN_CHANGED, session.getGameId(),
                Map.of("currentPlayerIndex", session.getCurrentPlayerIndex(),
                        "currentPlayerId", current.getPlayerId(),
                        "currentPlayerNickname", current.getNickname(),
                        "round", session.getRound(),
                        "maxRounds", session.getMaxRounds())));
    }

    private void broadcastToGame(Integer gameId, GameMessage<?> message) {
        messagingTemplate.convertAndSend("/topic/game/" + gameId, message);
    }

    private GameSession loadActiveSession(Integer gameId) {
        return redisGameStateService.loadSession(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_IN_PROGRESS));
    }

    private Game findGame(Integer gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));
    }

    private GameCharacter resolveCharacter(String characterKey) {
        return characterRepository.findByCharacterKey(characterKey)
                .orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_NOT_FOUND));
    }

    private World resolveBoard(Integer boardId) {
        if (boardId != null) {
            return worldRepository.findById(boardId)
                    .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        }
        return worldRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND,
                        "기본 보드가 없습니다. 먼저 보드를 초기화해주세요."));
    }

    private void requireState(GameSession session, GameState expected) {
        if (session.getState() != expected) {
            throw new CustomException(ErrorCode.INVALID_STATE_TRANSITION,
                    "현재 상태: " + session.getState() + ", 필요 상태: " + expected);
        }
    }

    private void requireCurrentPlayer(GameSession session, Member member) {
        PlayerSession current = session.getCurrentPlayer();
        if (!current.getMemberId().equals(member.getId())) {
            throw new CustomException(ErrorCode.NOT_YOUR_TURN);
        }
    }
}
