# 강대마블 API 명세서

> 프론트엔드 연동 기준 문서  
> Base URL: `http://localhost:8080` (개발) / `https://api.everyknu.com` (운영)  
> 최종 업데이트: 2026-05-25 (스타 기반 점수 + 이벤트 카드/두리버스/스타/미니게임 추가)

---

## 목차

1. [공통 규칙](#1-공통-규칙)
2. [회원 API](#2-회원-api)
3. [캐릭터 API](#3-캐릭터-api)
4. [게임 API](#4-게임-api)
5. [WebSocket](#5-websocket)
6. [게임 플로우](#6-게임-플로우)
7. [미결 항목](#7-미결-항목)

---

## 1. 공통 규칙

### 인증

로그인 성공 시 서버가 `accessToken` 쿠키를 자동 발급합니다.

**REST API — 쿠키 자동 포함 설정 필수**
```javascript
axios.defaults.withCredentials = true;
```

**WebSocket — STOMP CONNECT 헤더에 토큰 포함**
```javascript
const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`
  }
});
```

### 공통 응답 형식

**성공**
```json
{ "code": "200", "message": "성공 메시지", "data": { ... } }
```

**실패**
```json
{ "code": "404", "message": "에러 메시지", "data": null }
```

### 에러 코드 목록

| HTTP | 코드 | 상황 |
|------|------|------|
| 400 | GAME-400-01 | 게임이 대기 상태가 아님 |
| 400 | GAME-400-05 | 4명이 모두 참여하지 않음 |
| 400 | GAME-400-07 | 모든 플레이어가 레디하지 않음 |
| 400 | GAME-400-08 | 선택할 수 없는 분기점 |
| 400 | GAME-400-09 | 지정할 수 없는 카드 대상 |
| 400 | GAME-400-10 | 이동할 수 없는 정류장 |
| 400 | GAME-400-11 | 보유한 방어 카드가 없음 |
| 401 | AUTH-401 | 로그인 필요 |
| 403 | GAME-403-01 | 본인 턴이 아님 |
| 403 | GAME-403-02 | 방장이 아님 |
| 403 | GAME-403-03 | 방어 대상 플레이어가 아님 |
| 404 | GAME-404 | 게임 없음 |
| 404 | CHAR-404 | 존재하지 않는 캐릭터 |
| 409 | GAME-409-01 | 이미 해당 게임에 참가 중 |
| 409 | GAME-409-02 | 동시 요청 충돌 (재시도 필요) |
| 409 | CHAR-409 | 이미 다른 플레이어가 선택한 캐릭터 |

---

## 2. 회원 API

### 2-1. 회원가입

```
POST /api/v1/members/signup
```

**Request Body**
```json
{
  "email": "test@example.com",
  "password": "password123",
  "name": "홍길동"
}
```

**Response** `201`
```json
{
  "code": "201",
  "message": "[Member] Success: 회원 가입",
  "data": { "id": 1, "email": "test@example.com", "name": "홍길동" }
}
```

---

### 2-2. 로그인

```
POST /api/v1/members/login
```

**Request Body**
```json
{ "email": "test@example.com", "password": "password123" }
```

**Response** `200`
```json
{
  "code": "200",
  "message": "[Member] Success: 로그인",
  "data": {
    "member": { "id": 1, "email": "test@example.com", "name": "홍길동" },
    "accessToken": "eyJhbGci..."
  }
}
```

> 응답과 동시에 `accessToken` 쿠키가 자동 설정됩니다. WebSocket 연결 시 이 토큰을 CONNECT 헤더에 포함하세요.

---

### 2-3. 로그아웃

```
DELETE /api/v1/members/logout
```

**Response** `200` — data: null

---

### 2-4. 내 정보 조회

```
GET /api/v1/members/me
```

> 로그인 필요

**Response** `200`
```json
{
  "code": "200",
  "data": { "id": 1, "email": "test@example.com", "name": "홍길동" }
}
```

---

## 3. 캐릭터 API

### 3-1. 캐릭터 목록 조회

```
GET /api/v1/characters
```

> 인증 불필요. 서버 최초 실행 시 6개 캐릭터가 자동 등록됩니다.

**Response** `200`
```json
{
  "code": "200",
  "data": [
    { "id": 1, "characterKey": "gomduri", "name": "곰두리", "icon": "🐻‍❄️", "description": "강원대 대표 마스코트" },
    { "id": 2, "characterKey": "narae",   "name": "나래",   "icon": "🕊️",   "description": "하늘을 나는 비둘기" },
    { "id": 3, "characterKey": "daramji", "name": "다람쥐", "icon": "🐿️",   "description": "캠퍼스 다람쥐" },
    { "id": 4, "characterKey": "bunny",   "name": "토끼",   "icon": "🐰",   "description": "춘천 옥토끼" },
    { "id": 5, "characterKey": "fox",     "name": "여우",   "icon": "🦊",   "description": "영리한 산여우" },
    { "id": 6, "characterKey": "cat",     "name": "고양이", "icon": "🐱",   "description": "캠퍼스 길고양이" }
  ]
}
```

---

## 4. 게임 API

### 4-1. 방 목록 조회

```
GET /api/v1/games
```

> 로그인 필요. `WAITING` 상태인 방만 반환합니다.

**Response** `200`
```json
{
  "code": "200",
  "data": [
    {
      "gameId": 1,
      "title": "승준이의 테스트 방",
      "currentPlayers": 2,
      "maxPlayers": 4,
      "state": "WAITING",
      "hostMemberId": 1
    }
  ]
}
```

---

### 4-2. 방 생성

```
POST /api/v1/games
```

> 로그인 필요. 생성자가 자동으로 방장이 되며 첫 번째 플레이어로 등록됩니다.

**Request Body**
```json
{
  "title": "승준이의 테스트 방",
  "hostNickname": "홍길동",
  "characterKey": "gomduri",
  "boardId": null
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| `title` | String | Y | 방 제목 |
| `hostNickname` | String | Y | 게임 내 닉네임 |
| `characterKey` | String | Y | 선택한 캐릭터 키 (`GET /api/v1/characters` 참고) |
| `boardId` | Integer | N | 보드 ID (null이면 기본 보드) |

**Response** `201`
```json
{ "code": "201", "message": "게임이 생성되었습니다.", "data": 1 }
```

> `data`가 생성된 `gameId`입니다.

---

### 4-3. 방 입장

```
POST /api/v1/games/{gameId}/join
```

> 로그인 필요. `WAITING` 상태 + 4명 미만인 방에만 입장 가능합니다.

**Request Body**
```json
{
  "nickname": "플레이어2",
  "characterKey": "narae"
}
```

**Response** `200`
```json
{ "code": "200", "message": "게임에 참여했습니다.", "data": 3 }
```

> `data`가 생성된 `playerId`입니다.

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| 방 없음 | 404 | GAME-404 |
| 이미 시작된 방 | 400 | GAME-400-01 |
| 정원 초과 | 400 | GAME-400-02 |
| 이미 참가 중 | 409 | GAME-409-01 |
| 캐릭터 없음 | 404 | CHAR-404 |
| 캐릭터 중복 | 409 | CHAR-409 |

> WebSocket으로 `PLAYER_JOINED` 이벤트가 브로드캐스트됩니다.

---

### 4-4. 방 나가기

```
POST /api/v1/games/{gameId}/leave
```

> 로그인 필요. `WAITING` 상태에서만 가능합니다 (게임 중 나가기 불가).

**Response** `200` — data: null

**동작**
- 일반 플레이어 나가면 → `PLAYER_LEFT` 브로드캐스트
- 방장이 나가면 → 남은 플레이어 중 가장 먼저 입장한 사람이 방장 위임 → `PLAYER_LEFT` + `newHostMemberId` 브로드캐스트
- 마지막 플레이어가 나가면 → 방 삭제

---

### 4-5. 레디 토글

```
POST /api/v1/games/{gameId}/ready
```

> 로그인 필요. 호출할 때마다 레디/레디취소가 토글됩니다.

**Response** `200` — data: null

> WebSocket으로 `PLAYER_READY` 이벤트가 브로드캐스트됩니다.  
> `readyCount == 4`일 때 방장에게 시작 버튼을 활성화하세요.

---

### 4-6. 게임 시작

```
POST /api/v1/games/{gameId}/start
```

> 로그인 필요. **방장만** 호출 가능. **4명 전원이 레디**해야 시작 가능합니다.

**Response** `200`
```json
{
  "code": "200",
  "data": {
    "gameId": 1,
    "state": "TURN_START",
    "round": 1,
    "maxRounds": 8,
    "currentPlayerIndex": 0,
    "currentPlayerNickname": "홍길동",
    "players": [
      { "playerId": 2, "nickname": "홍길동",   "characterKey": "gomduri", "tileId": 10, "coins": 10, "gpa": 0, "connected": true },
      { "playerId": 3, "nickname": "플레이어2", "characterKey": "narae",   "tileId": 10, "coins": 10, "gpa": 0, "connected": true }
    ]
  }
}
```

> WebSocket으로 `GAME_STARTED`, `TURN_CHANGED` 이벤트가 브로드캐스트됩니다.

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| 방장 아님 | 403 | GAME-403-02 |
| 4명 미만 | 400 | GAME-400-05 |
| 전원 레디 아님 | 400 | GAME-400-07 |

---

### 4-7. 주사위 굴리기

```
POST /api/v1/games/{gameId}/roll
```

> 로그인 필요. 본인 턴 (`TURN_START` 상태)에만 호출 가능합니다.

**Response** `200` — 분기점 없는 경우
```json
{
  "code": "200",
  "data": {
    "diceValue": 4,
    "fromTileId": 10,
    "toTileId": 14,
    "tileIndex": 4,
    "tileType": "RANDOM_REWARD",
    "coinsChange": 9,
    "totalCoins": 19,
    "tileEventDescription": "행운 칸! +9 코인을 획득했습니다.",
    "nextState": "TURN_START",
    "gameEnded": false,
    "branchOptions": null
  }
}
```

**Response** `200` — 분기점 도달한 경우
```json
{
  "code": "200",
  "data": {
    "diceValue": 3,
    "fromTileId": 10,
    "toTileId": 13,
    "nextState": "BRANCH_SELECT",
    "gameEnded": false,
    "branchOptions": [14, 20]
  }
}
```

> `nextState`가 `BRANCH_SELECT`이면 `POST /branch`를 호출해야 턴이 계속됩니다.  
> **20초** 안에 선택하지 않으면 서버가 자동으로 랜덤 선택합니다.

---

### 4-8. 분기점 선택

```
POST /api/v1/games/{gameId}/branch
```

> 로그인 필요. `BRANCH_SELECT` 상태 + 본인 턴에만 호출 가능합니다.

**Request Body**
```json
{ "selectedNodeId": 14 }
```

**Response** `200` — 분기점 없이 이동 완료
```json
{
  "code": "200",
  "data": {
    "fromTileId": 13,
    "toTileId": 17,
    "tileType": "TRAP",
    "coinsChange": -5,
    "totalCoins": 5,
    "tileEventDescription": "함정 칸! -5 코인을 잃었습니다.",
    "nextState": "TURN_START",
    "gameEnded": false,
    "branchOptions": null
  }
}
```

**Response** `200` — 또 다른 분기점 도달
```json
{
  "code": "200",
  "data": {
    "nextState": "BRANCH_SELECT",
    "branchOptions": [18, 25],
    "branchOptions": null
  }
}
```

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| BRANCH_SELECT 상태 아님 | 400 | GAME-400-06 |
| 선택 불가 노드 | 400 | GAME-400-08 |
| 본인 턴 아님 | 403 | GAME-403-01 |

> ⚠️ **noteId 매핑**: 모든 이동 관련 응답/이벤트는 `nodeNumber`(1~53)를 보냅니다.
> 프론트는 `"node" + nodeNumber`로 매핑하고, 분기/버스 선택 요청에도 `nodeNumber`를 그대로 보냅니다.
> (`toTileId`/`tileId`는 서버 내부 DB id이며 프론트는 사용하지 않습니다.)

---

### 4-9. 공격 카드 대상 지정

```
POST /api/v1/games/{gameId}/card/target
```

> 로그인 필요. `CARD_TARGET_SELECT` 상태 + 본인 턴(카드를 뽑은 플레이어)에만 호출.
> 상대 지정 공격 카드(`police`/`drinking`/`skipper`)를 뽑았을 때만 발생합니다.

**Request Body**
```json
{ "targetPlayerId": 3 }
```

**Response** `200` — 대상이 방어 카드 보유 시 `nextState`가 `CARD_DEFENSE`, 아니면 즉시 적용 후 `TURN_START`/`GAME_END`.

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| CARD_TARGET_SELECT 상태 아님 | 400 | GAME-400-06 |
| 본인 턴 아님 | 403 | GAME-403-01 |
| 대상이 자기 자신/존재하지 않음 | 400 | GAME-400-09 |

> 20초 내 미선택 시 서버가 랜덤 상대를 자동 지정합니다.

---

### 4-10. 방어 카드 사용 여부 선택

```
POST /api/v1/games/{gameId}/card/defense
```

> 로그인 필요. `CARD_DEFENSE` 상태 + **피격 대상 본인**만 호출. (현재 턴 플레이어가 아니어도 호출)

**Request Body**
```json
{ "useDefense": true }
```

- `true`: 방어 카드 1장 소모, 스타 차감 무효화
- `false`: 방어 안 함, 스타 차감 적용

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| CARD_DEFENSE 상태 아님 | 400 | GAME-400-06 |
| 방어 대상이 아님 | 403 | GAME-403-03 |
| 방어 카드 없음(`useDefense:true`인데 보유 0) | 400 | GAME-400-11 |

> 20초 내 미선택 시 서버가 **방어 미사용**(카드 보존, 차감 적용)으로 자동 처리합니다.

---

### 4-11. 두리버스 도착 정류장 선택

```
POST /api/v1/games/{gameId}/bus
```

> 로그인 필요. `BUS_SELECT` 상태 + 본인 턴에만 호출. 버스 칸 도착 시 발생합니다.

**Request Body**
```json
{ "destinationTileId": 22 }
```

> `destinationTileId`는 도착 정류장의 `nodeNumber`. `BUS_RIDE_REQUIRED`의 `busOptions` 중 하나.

**오류**
| 상황 | HTTP | 코드 |
|------|------|------|
| BUS_SELECT 상태 아님 | 400 | GAME-400-06 |
| 본인 턴 아님 | 403 | GAME-403-01 |
| 이동 불가 정류장 | 400 | GAME-400-10 |

> 20초 내 미선택 시 서버가 랜덤 정류장으로 자동 이동합니다.

---

### 4-12. 게임 상태 조회 (재접속용)

```
GET /api/v1/games/{gameId}/state
```

> 로그인 필요. 재접속 시 현재 상태 스냅샷을 받아 화면을 복원합니다.

**Response** `200`
```json
{
  "code": "200",
  "data": {
    "gameId": 1,
    "state": "TURN_START",
    "round": 3,
    "maxRounds": 8,
    "currentPlayerIndex": 1,
    "currentPlayerNickname": "플레이어2",
    "players": [
      { "playerId": 2, "nickname": "홍길동",   "characterKey": "gomduri", "tileId": 14, "tileNumber": 5,  "stars": 4, "defenseCards": 1, "coins": 10, "gpa": 0, "connected": true },
      { "playerId": 3, "nickname": "플레이어2", "characterKey": "narae",   "tileId": 10, "tileNumber": 1,  "stars": 2, "defenseCards": 0, "coins": 10, "gpa": 0, "connected": false }
    ]
  }
}
```

> **플레이어 필드**: `stars`=점수(승패 기준), `defenseCards`=보유 방어 카드 수, `tileNumber`=현재 칸(`"node"+tileNumber`로 매핑). `coins`/`gpa`는 현재 미사용(코인 잔재, gpa는 보류).

---

## 5. WebSocket

### 연결

```
엔드포인트: ws://localhost:8080/ws (SockJS)
구독 주소:  /topic/game/{gameId}
```

```javascript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`  // 필수
  },
  onConnect: () => {
    client.subscribe(`/topic/game/${gameId}`, (msg) => {
      const { type, payload } = JSON.parse(msg.body);
      // type으로 분기 처리
    });
  }
});
client.activate();
```

### 공통 메시지 구조

```json
{ "type": "이벤트_타입", "gameId": 1, "payload": { ... }, "timestamp": 1735000000000 }
```

---

### 이벤트 목록

#### PLAYER_JOINED
> 트리거: `POST /join`
```json
{
  "type": "PLAYER_JOINED",
  "payload": { "playerId": 3, "nickname": "플레이어2", "characterKey": "narae", "playerIndex": 1, "playerCount": 2 }
}
```

---

#### PLAYER_LEFT
> 트리거: `POST /leave`
```json
// 일반 플레이어
{ "type": "PLAYER_LEFT", "payload": { "memberId": 2, "nickname": "홍길동", "remainingCount": 3 } }

// 방장이 나갔을 때 (newHost 필드 추가)
{ "type": "PLAYER_LEFT", "payload": { "memberId": 2, "nickname": "홍길동", "remainingCount": 3, "newHostMemberId": 3, "newHostNickname": "플레이어2" } }
```

---

#### PLAYER_READY
> 트리거: `POST /ready`
```json
{
  "type": "PLAYER_READY",
  "payload": { "memberId": 1, "nickname": "홍길동", "ready": true, "readyCount": 3, "totalCount": 4 }
}
```

> `readyCount == totalCount == 4`이면 방장에게 시작 버튼을 활성화하세요.

---

#### GAME_STARTED
> 트리거: `POST /start`
```json
{
  "type": "GAME_STARTED",
  "payload": {
    "gameId": 1, "state": "TURN_START", "round": 1, "maxRounds": 8,
    "currentPlayerIndex": 0, "currentPlayerNickname": "홍길동",
    "players": [
      { "playerId": 2, "nickname": "홍길동",   "characterKey": "gomduri", "tileId": 10, "coins": 10, "gpa": 0, "connected": true },
      { "playerId": 3, "nickname": "플레이어2", "characterKey": "narae",   "tileId": 10, "coins": 10, "gpa": 0, "connected": true }
    ]
  }
}
```

---

#### TURN_CHANGED
> 트리거: 이전 턴 종료 직후
```json
{
  "type": "TURN_CHANGED",
  "payload": { "currentPlayerIndex": 1, "currentPlayerId": 3, "currentPlayerNickname": "플레이어2", "round": 2, "maxRounds": 8 }
}
```

---

#### DICE_ROLLED
> 트리거: `POST /roll` 처리 중 (1번째)
```json
{
  "type": "DICE_ROLLED",
  "payload": { "playerId": 2, "nickname": "홍길동", "diceValue": 4 }
}
```

---

#### PLAYER_MOVED
> 트리거: 이동 처리 중. **`nodeNumber`(1~53)로 말을 이동하세요.**
```json
{
  "type": "PLAYER_MOVED",
  "payload": { "playerId": 2, "fromTileId": 10, "toTileId": 14, "tileIndex": 4, "nodeNumber": 5 }
}
```

---

#### BRANCH_REQUIRED
> 트리거: 분기점 도달 시 (`/roll` 또는 `/branch` 처리 중). `branchOptions`는 **nodeNumber** 배열.
```json
{
  "type": "BRANCH_REQUIRED",
  "payload": { "playerId": 2, "branchOptions": [15, 17], "timeoutSeconds": 20 }
}
```

> 현재 턴 플레이어만 `POST /branch`(body `selectedNodeId` = nodeNumber)를 호출합니다.  
> 20초 타임아웃 시 서버가 자동으로 랜덤 선택합니다.

---

#### TILE_TRIGGERED
> 트리거: 이동/카드/버스 처리 중. **점수는 `stars`**(`starsChange`/`totalStars`).
```json
// STAR (스타 칸)
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "STAR", "starsChange": 1, "totalStars": 5, "description": "⭐ 스타 칸! 스타 +1" } }

// MINIGAME (v1 임시: 랜덤 등수)
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "MINIGAME", "starsChange": 3, "totalStars": 8, "description": "🎮 미니게임 1등! 스타 +3" } }

// CARD (장학금/자기차감 즉시 적용 — 카드 메타 포함)
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "CARD", "cardKey": "top", "cardType": "SCHOLARSHIP", "title": "성적우수 장학금", "starsChange": 3, "totalStars": 8, "defenseCards": 0, "skipNextTurn": false, "description": "..." } }

// CARD (공격 적용 결과 — 방어 여부 포함)
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 3, "tileType": "CARD", "cardKey": "skipper", "cardType": "ATTACK", "blocked": false, "starsChange": -3, "totalStars": 1, "defenseCards": 0, "description": "출튀한 사람 — 플레이어2님 스타 -3" } }

// BUS (두리버스 이동 완료)
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "BUS", "teleportTileId": 31, "nodeNumber": 22, "starsChange": 0, "totalStars": 5, "description": "🚌 두리버스로 정류장을 이동했습니다." } }
```

---

#### CARD_DRAWN
> 트리거: 카드 칸 도착 시 (전체에게 뽑힌 카드 공개)
```json
{ "type": "CARD_DRAWN", "payload": { "playerId": 2, "nickname": "홍길동", "cardKey": "skipper", "cardType": "ATTACK", "title": "출튀한 사람", "description": "..." } }
```

---

#### CARD_TARGET_REQUIRED
> 트리거: 상대 지정 공격 카드를 뽑았을 때. **카드 뽑은 본인**이 `POST /card/target` 호출.
```json
{ "type": "CARD_TARGET_REQUIRED", "payload": { "playerId": 2, "cardKey": "skipper", "title": "출튀한 사람", "targetOptions": [3, 4, 5], "timeoutSeconds": 20 } }
```
> `targetOptions`는 지정 가능한 상대 **playerId** 목록 (nodeNumber 아님).

---

#### DEFENSE_PROMPT
> 트리거: 지정된 대상이 방어 카드 보유 시. **피격 대상 본인**이 `POST /card/defense` 호출.
```json
{ "type": "DEFENSE_PROMPT", "payload": { "targetPlayerId": 3, "attackerPlayerId": 2, "cardKey": "skipper", "title": "출튀한 사람", "starsChange": -3, "defenseCards": 1, "timeoutSeconds": 20 } }
```

---

#### BUS_RIDE_REQUIRED
> 트리거: 버스 칸 도착 시. **현재 플레이어**가 `POST /bus` 호출. `busOptions`는 **nodeNumber** 목록.
```json
{ "type": "BUS_RIDE_REQUIRED", "payload": { "playerId": 2, "busOptions": [22, 31, 36], "timeoutSeconds": 20 } }
```

---

#### TURN_SKIPPED
> 트리거: "그렇게 과CC를..." 카드로 다음 턴을 건너뛸 때
```json
{ "type": "TURN_SKIPPED", "payload": { "playerId": 3, "nickname": "플레이어2" } }
```

---

#### GAME_ENDED
> 트리거: 8라운드 마지막 턴 완료
```json
{
  "type": "GAME_ENDED",
  "payload": {
    "results": [
      { "playerId": 3, "nickname": "플레이어2", "stars": 12, "coins": 10, "rank": 1 },
      { "playerId": 5, "nickname": "플레이어3", "stars": 12, "coins": 10, "rank": 1 },
      { "playerId": 2, "nickname": "홍길동",   "stars": 9,  "coins": 10, "rank": 3 }
    ]
  }
}
```

> **순위 = `stars` 내림차순. 동점은 공동 순위**(위 예시: 1등 2명 → 다음은 3등). `coins`는 표시용 잔재.

---

## 6. 게임 플로우

### 로비 → 게임 시작

```
1. GET  /api/v1/characters          캐릭터 목록 조회
2. POST /api/v1/members/login       로그인 → accessToken 쿠키 발급
3. WS   CONNECT /ws                 WebSocket 연결 (토큰 헤더 포함)
4. GET  /api/v1/games               대기 중인 방 목록 조회
5. POST /api/v1/games               방 생성 → gameId 반환  (또는)
   POST /api/v1/games/{id}/join     방 입장
6. WS   SUBSCRIBE /topic/game/{id}  게임 구독
7. POST /api/v1/games/{id}/ready    레디 (PLAYER_READY 브로드캐스트)
   → readyCount == 4이면 방장에게 시작 버튼 활성화
8. POST /api/v1/games/{id}/start    게임 시작 (방장만)
   → GAME_STARTED, TURN_CHANGED 브로드캐스트
```

### 한 턴의 흐름

```
현재 턴 플레이어
  │
  ├─ POST /roll → WS: DICE_ROLLED → WS: PLAYER_MOVED
  │
  ├─ [분기점] → WS: BRANCH_REQUIRED → POST /branch (반복 가능)
  │
  └─ 도착 칸 종류별 처리:
       ├─ NORMAL                : 효과 없음
       ├─ STAR                  : 스타 +1 → WS: TILE_TRIGGERED
       ├─ MINIGAME              : 랜덤 등수 → 스타 지급 → WS: TILE_TRIGGERED
       ├─ BUS  → WS: BUS_RIDE_REQUIRED → POST /bus → 순간이동
       └─ CARD → WS: CARD_DRAWN
                 ├─ 장학금/자기차감/스킵 : 즉시 적용 → WS: TILE_TRIGGERED
                 └─ 상대 지정 공격 → WS: CARD_TARGET_REQUIRED → POST /card/target
                          ├─ 대상 방어카드 보유 → WS: DEFENSE_PROMPT → POST /card/defense
                          └─ 적용 → WS: TILE_TRIGGERED
  │
  └─ WS: TURN_CHANGED (스킵 대상은 TURN_SKIPPED 후 자동 넘김) 또는 GAME_ENDED
```

### 점수/승리 조건

- **점수 = 스타 개수.** 8라운드 종료 후 스타가 가장 많은 플레이어가 승리. **동점은 공동 순위.**
- 코인(`coins`)/학점(`gpa`)은 현재 미사용 (코인은 잔재, gpa는 보류).

### 타임아웃

| 종류 | 시간 | 동작 |
|------|------|------|
| 턴 타임아웃 | 30초 | 서버가 자동으로 주사위 굴림 |
| 분기점 타임아웃 | 20초 | 서버가 랜덤으로 경로 선택 |
| 카드 대상 지정 | 20초 | 서버가 랜덤 상대 자동 지정 |
| 방어 선택 | 20초 | 방어 미사용(차감 적용)으로 자동 처리 |
| 두리버스 선택 | 20초 | 서버가 랜덤 정류장 자동 이동 |

### 재접속

```
1. WS   CONNECT → SUBSCRIBE /topic/game/{id}
2. GET  /api/v1/games/{id}/state   현재 스냅샷으로 화면 복원
3. 이후 WebSocket 이벤트 수신
```

---

## 7. 타일 & 이벤트 카드 메커닉 (2026-05-25 확정)

### 7-1. 보드 타일 종류 (53노드, `map_data.json` 기준)

| 타일 | 노드 번호 | 효과 |
|------|-----------|------|
| `CARD` (이벤트 카드) | 3, 9, 13, 34, 44, 47, 48, 49 | 카드 1장 뽑아 효과 적용 (아래 7-2) |
| `STAR` | 4, 11, 19, 23, 28, 30, 42, 52 | 스타 +1 |
| `BUS` (두리버스) | 15, 22, 31, 36 | 다른 정류장으로 순간이동 (선택) |
| `MINIGAME` | 6, 21, 26, 39, 45, 46, 50, 53 | **v1 임시**: 랜덤 등수(1~4) → 스타 +3/+2/+1/0. 실제 미니게임 추후 |
| `NORMAL`/start | 나머지 | 효과 없음 |

> 스타는 0 미만으로 내려가지 않음.

### 7-2. 이벤트 카드 덱 (총 30장)

| 분류 | 카드 (키) | 장수 | 대상 | 스타 |
|------|-----------|:---:|------|:---:|
| 공격 | 출튀한 사람 (`skipper`) | 1 | 상대 지정 | **−3** |
| 공격 | 캠퍼스 폴리스에 적발! (`police`) | 3 | 상대 지정 | −2 |
| 공격 | 회식의 저주 (`drinking`) | 2 | 상대 지정 | −2 |
| 공격 | 수강신청 대 실패 (`course_fail`) | 2 | 자신 | −2 |
| 공격 | 팀프로젝트 무임승차 빌런 (`freeloader`) | 2 | 자신 | −1 |
| 공격 | 그렇게 과CC를... (`breakup`) | 2 | 자신 | 0 + **다음 턴 스킵** |
| 방어 | 백령 곰두리의 수호 (`guardian`) | 8 | 보관(무제한) | 상대 지정 공격 1회 무효 |
| 장학 | 성적우수 장학금 (`top`) | 2 | 자신 | +3 |
| 장학 | 캡스톤 디자인 A+ (`capstone`) | 2 | 자신 | +3 |
| 장학 | KNU미래글로벌인재 장학금 (`global`) | 2 | 자신 | +2 |
| 장학 | 국가유공자 및 자녀장학금 (`veteran`) | 2 | 자신 | +2 |
| 장학 | 학과사랑 근로장학금 (`work`) | 2 | 자신 | +1 |

**규칙**
- 매 뽑기는 위 장수로 가중된 무한 덱에서 독립 추출(소진/셔플 없음).
- **상대 지정 공격**: 카드 뽑은 사람이 상대 1명 선택 → 대상이 방어 카드 보유 시 사용 여부 선택.
- **자기 차감 공격**: 즉시 본인 스타 차감 (방어 불가).
- **방어 카드**: 무제한 보유, 상대 지정 공격을 받을 때 1장 소모해 무효화. 자기 차감/스킵은 방어 불가.
- **`breakup`**: 스타 변화 없이 다음 본인 턴 1회 스킵 (`TURN_SKIPPED` 브로드캐스트).

---

## 8. 미결 / 보류 항목

| 항목 | 상태 |
|------|------|
| **학점(gpa) 시스템** | **보류**. `gpa` 항상 0. 추후 기획. |
| **실제 미니게임** | 보류. 현재는 랜덤 등수 플레이스홀더. |
| **타일 ID ↔ 노드 이름 변환** | ✅ 해결. 서버가 모든 이벤트/응답에 `nodeNumber`(1~53) 포함, 프론트는 `"node"+nodeNumber`로 매핑. 분기/버스 선택 요청도 nodeNumber. |
| **dev DB 재시드** | 기존 `db_dev.mv.db`에 구버전 46노드 보드가 남아 있으면 새 보드가 시드되지 않음(`worldRepository.count()==0`일 때만 시드). **로컬에서 `db_dev.mv.db` 삭제 후 재기동 필요.** |
