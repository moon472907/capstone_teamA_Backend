# 강대마블 프로젝트 구조

## 기술 스택

| 분류 | 기술 |
|------|------|
| 언어 / 런타임 | Java 21, Spring Boot 3.5.5 |
| 데이터베이스 | H2 (개발), MySQL (운영) / JPA (Hibernate 6) |
| 인메모리 스토어 | Redis (게임 세션, 분산 락) |
| 실시간 통신 | STOMP over WebSocket (SockJS 폴백) |
| 인증 | JWT (쿠키 기반 accessToken) |
| API 문서 | Swagger UI (`/swagger-ui/index.html`) |
| 빌드 | Gradle |

---

## 실행 방법

**필요 환경**
- Java 21+
- Redis (`localhost:6379`)

**`.env` 파일 생성 (루트)**
```
CUSTOM__JWT__SECRET_KEY=your-secret-key-here
```

**서버 실행**
```bash
./gradlew bootRun
```

**개발 도구**
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- H2 Console: `http://localhost:8080/h2-console`

---

## 디렉터리 구조

```
src/main/java/com/back/
├── BackApplication.java
│
├── domain/
│   ├── character/                        ← 캐릭터 도메인
│   │   ├── controller/CharacterController.java
│   │   ├── dto/CharacterDto.java
│   │   ├── entity/GameCharacter.java
│   │   ├── repository/CharacterRepository.java
│   │   └── service/CharacterService.java
│   │
│   ├── game/                             ← 게임 도메인 (핵심)
│   │   ├── controller/GameController.java
│   │   ├── dto/
│   │   │   ├── BranchSelectReqDto.java
│   │   │   ├── CreateGameReqDto.java
│   │   │   ├── GameRoomDto.java
│   │   │   ├── GameStateSnapshotDto.java
│   │   │   ├── JoinGameReqDto.java
│   │   │   └── RollResultDto.java
│   │   ├── entity/
│   │   │   ├── Game.java
│   │   │   └── GameState.java            ← 상태 머신 enum
│   │   ├── redis/
│   │   │   ├── GameSession.java          ← Redis 직렬화 POJO
│   │   │   └── PlayerSession.java
│   │   ├── repository/GameRepository.java
│   │   └── service/
│   │       ├── GameService.java          ← 게임 로직 전체
│   │       └── TurnTimeoutService.java   ← 자동 롤/분기 스케줄러
│   │
│   ├── member/                           ← 회원 도메인
│   │   ├── controller/ApiV1MemberController.java
│   │   ├── dto/
│   │   ├── entity/Member.java            ← @SoftDelete 적용
│   │   ├── repository/MemberRepository.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       └── MemberService.java
│   │
│   ├── player/                           ← 플레이어 도메인
│   │   ├── entity/Player.java            ← 게임 종료 후 최종 결과 저장
│   │   └── repository/PlayerRepository.java
│   │
│   └── world/                            ← 보드 도메인
│       ├── entity/
│       │   ├── World.java
│       │   ├── Node.java                 ← 타일 노드 (그래프 정점)
│       │   ├── Edge.java                 ← 타일 간 연결 (그래프 간선)
│       │   └── TileType.java
│       ├── handler/                      ← 타일 이벤트 전략 패턴
│       │   ├── TileEventHandler.java
│       │   ├── NormalTileHandler.java
│       │   ├── RandomRewardTileHandler.java
│       │   ├── TrapTileHandler.java
│       │   ├── TeleportTileHandler.java
│       │   ├── TileEventHandlerFactory.java
│       │   └── TileEventResult.java
│       ├── repository/
│       └── service/WorldService.java
│
└── global/
    ├── common/ApiResponse.java           ← 공통 응답 래퍼
    ├── config/
    │   ├── RedisConfig.java
    │   ├── SchedulerConfig.java
    │   └── WebSocketConfig.java          ← STOMP + 인증 인터셉터
    ├── exception/
    │   ├── CustomException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── initData/BaseInitData.java        ← 캐릭터 + 기본 보드 자동 생성
    ├── jpa/entity/BaseEntity.java
    ├── redis/
    │   ├── RedisGameStateService.java    ← 세션 CRUD (TTL 2h)
    │   └── RedisLockService.java         ← SETNX 분산 락
    ├── rq/Rq.java
    ├── security/
    │   ├── CustomAuthenticationFilter.java
    │   ├── SecurityConfig.java
    │   └── SecurityUser.java
    └── websocket/
        ├── GameMessage.java
        ├── MessageType.java
        └── StompAuthChannelInterceptor.java  ← WebSocket JWT 인증
```

---

## 레이어 구조

```
HTTP / WebSocket Client
        │
[SecurityFilter / StompAuthInterceptor]  ← JWT 검증
        │
  [Controller Layer]   ← 요청 수신, 응답 반환
        │
  [Service Layer]
    ├── GameService          (게임 상태 머신)
    ├── TurnTimeoutService   (30초 턴 / 20초 분기 타임아웃)
    ├── MemberService        (회원 관리)
    └── CharacterService     (캐릭터 목록)
        │
    ┌───┴────┐
    │        │
[JPA / DB]  [Redis]
Game        GameSession (TTL 2h)
Player      PlayerSession
Member
World/Node
GameCharacter
```

---

## 데이터 분리 전략

| 저장소 | 데이터 | 이유 |
|--------|--------|------|
| Redis | GameSession, PlayerSession | 밀리초 단위 읽기/쓰기, TTL 2시간 |
| JPA/DB | Game, Player | 게임 종료 후 영구 결과 보존 |
| JPA/DB | World, Node, Edge | 보드는 정적 데이터 |
| JPA/DB | Member | 회원 정보 영구 보존, SoftDelete |
| JPA/DB | GameCharacter | 캐릭터 마스터 데이터 |

---

## Redis 키 구조

| 키 패턴 | 값 | TTL |
|---------|-----|-----|
| `game:session:{gameId}` | JSON (GameSession) | 2시간 |
| `game:lock:{gameId}` | UUID 문자열 | 30초 |

---

## 상태 머신 (GameState)

```
WAITING
  │  POST /start (방장 + 전원 레디)
  ▼
IN_PROGRESS
  │
  ▼
TURN_START ◄─────────────────────────────────┐
  │  POST /roll                               │
  ▼                                           │
DICE_ROLL → PLAYER_MOVED                      │
  │                                           │
  ├─ [분기점 없음]                             │
  │    ▼                                      │
  │  TILE_EVENT                               │
  │    ▼                                      │
  │  TURN_END ──► 게임 계속 ──────────────────┘
  │    └────────► 게임 종료 ──► GAME_END
  │
  └─ [분기점 있음]
       ▼
     BRANCH_SELECT  ← POST /branch (20초 타임아웃)
       │  선택 완료
       └──► TILE_EVENT → TURN_END → ...
```

---

## 기본 보드 구조

타일 20개 / 순환 링 + 단축로 (타일 5 → 타일 10)

```
0(S)→1→2(R)→3→4(T)→5(P)─────────────────→10(R)
                     │                      ↑
                     └→6→7→8(T)→9──────────┘

10→11→12→13(R)→14→15(P)→16→17(T)→18(R)→19→0

S=시작  R=랜덤보상(+5~14코인)  T=함정(-3~7코인)  P=텔레포트
```

| 타일 타입 | 효과 |
|----------|------|
| NORMAL | 없음 |
| RANDOM_REWARD | +5~14 코인 |
| TRAP | -3~7 코인 (최소 0) |
| TELEPORT | 무작위 타일로 이동 |
