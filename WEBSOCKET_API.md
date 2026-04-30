# WebSocket API 문서

## 연결 정보

| 항목 | 값 |
|---|---|
| 엔드포인트 | `ws://localhost:8080/ws` |
| 프로토콜 | STOMP over SockJS |
| 구독 주소 | `/topic/game/{gameId}` |

---

## 연결 방법 (클라이언트 예시)

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  onConnect: () => {
    // 게임 구독
    client.subscribe(`/topic/game/${gameId}`, (message) => {
      const data = JSON.parse(message.body);
      handleGameMessage(data);
    });
  }
});

client.activate();
```

---

## 공통 메시지 구조

서버가 보내는 모든 메시지는 아래 형태입니다.

```json
{
  "type": "메시지_타입",
  "gameId": 1,
  "payload": { ... },
  "timestamp": 1735000000000
}
```

---

## 메시지 타입 목록

### 1. PLAYER_JOINED
> 새 플레이어가 게임에 참여했을 때 발생  
> 트리거: `POST /api/v1/games/{gameId}/join`

```json
{
  "type": "PLAYER_JOINED",
  "gameId": 1,
  "payload": {
    "playerId": 3,
    "nickname": "Alice",
    "playerIndex": 1,
    "playerCount": 2
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `playerId` | Integer | 참여한 플레이어 DB ID |
| `nickname` | String | 플레이어 닉네임 |
| `playerIndex` | int | 턴 순서 (0부터 시작) |
| `playerCount` | int | 현재 참여 인원 수 |

---

### 2. GAME_STARTED
> 게임이 시작됐을 때 발생. 전체 초기 상태 포함  
> 트리거: `POST /api/v1/games/{gameId}/start`

```json
{
  "type": "GAME_STARTED",
  "gameId": 1,
  "payload": {
    "gameId": 1,
    "state": "TURN_START",
    "round": 1,
    "maxRounds": 8,
    "currentPlayerIndex": 0,
    "currentPlayerNickname": "Alice",
    "players": [
      { "playerId": 2, "nickname": "Alice", "tileId": 10, "coins": 10, "connected": true },
      { "playerId": 3, "nickname": "Bob",   "tileId": 10, "coins": 10, "connected": true }
    ]
  },
  "timestamp": 1735000000000
}
```

---

### 3. TURN_CHANGED
> 다음 플레이어의 턴이 시작됐을 때 발생  
> 트리거: 이전 플레이어의 턴 종료 직후 / 게임 시작 직후

```json
{
  "type": "TURN_CHANGED",
  "gameId": 1,
  "payload": {
    "currentPlayerIndex": 1,
    "currentPlayerId": 3,
    "currentPlayerNickname": "Bob",
    "round": 2,
    "maxRounds": 8
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `currentPlayerIndex` | int | 현재 턴인 플레이어의 인덱스 |
| `currentPlayerId` | Integer | 현재 턴인 플레이어 DB ID |
| `currentPlayerNickname` | String | 현재 턴인 플레이어 닉네임 |
| `round` | int | 현재 라운드 번호 (1~8) |
| `maxRounds` | int | 최대 라운드 수 (항상 8) |

---

### 4. DICE_ROLLED
> 주사위를 굴린 결과  
> 트리거: `POST /api/v1/games/{gameId}/roll` 처리 중 (첫 번째 이벤트)

```json
{
  "type": "DICE_ROLLED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "nickname": "Alice",
    "diceValue": 4
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `playerId` | Integer | 주사위를 굴린 플레이어 ID |
| `nickname` | String | 플레이어 닉네임 |
| `diceValue` | int | 주사위 결과 (1~6) |

---

### 5. PLAYER_MOVED
> 플레이어가 이동을 완료했을 때  
> 트리거: `POST /api/v1/games/{gameId}/roll` 처리 중 (두 번째 이벤트)

```json
{
  "type": "PLAYER_MOVED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "fromTileId": 10,
    "toTileId": 14,
    "tileIndex": 4
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `playerId` | Integer | 이동한 플레이어 ID |
| `fromTileId` | int | 이동 전 타일 DB ID |
| `toTileId` | int | 이동 후 타일 DB ID |
| `tileIndex` | int | 도착한 타일의 보드 상 인덱스 (0~19) |

---

### 6. TILE_TRIGGERED
> 도착 타일의 이벤트가 처리됐을 때  
> 트리거: `POST /api/v1/games/{gameId}/roll` 처리 중 (세 번째 이벤트)

**NORMAL 타일**
```json
{
  "type": "TILE_TRIGGERED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "tileType": "NORMAL",
    "coinsChange": 0,
    "totalCoins": 10,
    "description": "보통 칸입니다. 아무 일도 일어나지 않습니다."
  },
  "timestamp": 1735000000000
}
```

**RANDOM_REWARD 타일**
```json
{
  "type": "TILE_TRIGGERED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "tileType": "RANDOM_REWARD",
    "coinsChange": 9,
    "totalCoins": 19,
    "description": "행운 칸! +9 코인을 획득했습니다."
  },
  "timestamp": 1735000000000
}
```

**TRAP 타일**
```json
{
  "type": "TILE_TRIGGERED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "tileType": "TRAP",
    "coinsChange": -5,
    "totalCoins": 5,
    "description": "함정 칸! -5 코인을 잃었습니다."
  },
  "timestamp": 1735000000000
}
```

**TELEPORT 타일**
```json
{
  "type": "TILE_TRIGGERED",
  "gameId": 1,
  "payload": {
    "playerId": 2,
    "tileType": "TELEPORT",
    "coinsChange": 0,
    "totalCoins": 10,
    "description": "텔레포트 칸! 12번 칸으로 이동합니다.",
    "teleportTileId": 22
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `playerId` | Integer | 이벤트가 적용된 플레이어 ID |
| `tileType` | String | `NORMAL` / `RANDOM_REWARD` / `TRAP` / `TELEPORT` |
| `coinsChange` | int | 코인 변화량 (음수 가능, 0 가능) |
| `totalCoins` | int | 이벤트 적용 후 총 코인 |
| `description` | String | 사용자에게 표시할 설명 문자열 |
| `teleportTileId` | Integer | TELEPORT 타입일 때만 존재. 이동한 타일 DB ID |

---

### 7. GAME_ENDED
> 8라운드가 모두 끝났을 때 발생  
> 트리거: 마지막 플레이어의 마지막 턴 완료 후

```json
{
  "type": "GAME_ENDED",
  "gameId": 1,
  "payload": {
    "results": [
      { "playerId": 3, "nickname": "Bob",   "coins": 42, "rank": 1 },
      { "playerId": 2, "nickname": "Alice", "coins": 35, "rank": 2 },
      { "playerId": 4, "nickname": "Carol", "coins": 28, "rank": 3 },
      { "playerId": 5, "nickname": "Dave",  "coins": 15, "rank": 4 }
    ]
  },
  "timestamp": 1735000000000
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `results` | Array | 코인 내림차순으로 정렬된 최종 순위 목록 |
| `results[].playerId` | Integer | 플레이어 DB ID |
| `results[].nickname` | String | 닉네임 |
| `results[].coins` | int | 최종 코인 수 |
| `results[].rank` | int | 최종 순위 (1 = 1등) |

---

## 한 번의 Roll 호출 시 이벤트 발생 순서

```
클라이언트                          서버
    │                                │
    │── POST /api/v1/games/1/roll ──►│
    │                                │ (1) DICE_ROLLED   broadcast
    │◄── WS: DICE_ROLLED ───────────│
    │                                │ (2) PLAYER_MOVED  broadcast
    │◄── WS: PLAYER_MOVED ──────────│
    │                                │ (3) TILE_TRIGGERED broadcast
    │◄── WS: TILE_TRIGGERED ────────│
    │                                │ (4a) 게임 계속: TURN_CHANGED broadcast
    │◄── WS: TURN_CHANGED ──────────│
    │        또는                    │ (4b) 게임 종료: GAME_ENDED broadcast
    │◄── WS: GAME_ENDED ────────────│
    │                                │
    │◄── HTTP 200 RollResultDto ─────│  ← REST 응답 (WebSocket과 동일한 결과값)
```

---

## 턴 타임아웃

플레이어가 **30초** 안에 `/roll`을 호출하지 않으면 서버가 자동으로 주사위를 굴립니다.  
이때도 동일한 순서로 WebSocket 이벤트가 발생합니다.

---

## 재접속 처리

접속이 끊겼다가 다시 연결할 경우:

1. `/topic/game/{gameId}` 재구독
2. `GET /api/v1/games/{gameId}/state` 호출 → 현재 전체 상태 스냅샷 수신
3. 스냅샷으로 화면 복원 후 이후 WebSocket 이벤트 수신
