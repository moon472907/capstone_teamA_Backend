# 강대마블 API 명세서

> 프론트엔드 연동 기준 문서  
> Base URL: `http://localhost:8080` (개발) / `https://api.everyknu.com` (운영)  
> 최종 업데이트: 2026-05-18

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
| 401 | AUTH-401 | 로그인 필요 |
| 403 | GAME-403-01 | 본인 턴이 아님 |
| 403 | GAME-403-02 | 방장이 아님 |
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

---

### 4-9. 게임 상태 조회 (재접속용)

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
      { "playerId": 2, "nickname": "홍길동",   "characterKey": "gomduri", "tileId": 14, "coins": 19, "gpa": 0, "connected": true },
      { "playerId": 3, "nickname": "플레이어2", "characterKey": "narae",   "tileId": 10, "coins": 10, "gpa": 0, "connected": false }
    ]
  }
}
```

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
> 트리거: `POST /roll` 처리 중 (2번째)
```json
{
  "type": "PLAYER_MOVED",
  "payload": { "playerId": 2, "fromTileId": 10, "toTileId": 14, "tileIndex": 4 }
}
```

---

#### BRANCH_REQUIRED
> 트리거: 분기점 도달 시 (`/roll` 또는 `/branch` 처리 중)
```json
{
  "type": "BRANCH_REQUIRED",
  "payload": { "playerId": 2, "branchOptions": [14, 20], "timeoutSeconds": 20 }
}
```

> 현재 턴 플레이어만 `POST /branch`를 호출합니다.  
> 20초 타임아웃 시 서버가 자동으로 랜덤 선택합니다.

---

#### TILE_TRIGGERED
> 트리거: `POST /roll` 또는 `/branch` 처리 중 (이동 완료 후)
```json
// RANDOM_REWARD
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "RANDOM_REWARD", "coinsChange": 9, "totalCoins": 19, "description": "행운 칸! +9 코인을 획득했습니다." } }

// TRAP
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "TRAP", "coinsChange": -5, "totalCoins": 5, "description": "함정 칸! -5 코인을 잃었습니다." } }

// TELEPORT
{ "type": "TILE_TRIGGERED", "payload": { "playerId": 2, "tileType": "TELEPORT", "coinsChange": 0, "totalCoins": 10, "description": "텔레포트!", "teleportTileId": 22 } }
```

---

#### GAME_ENDED
> 트리거: 8라운드 마지막 턴 완료
```json
{
  "type": "GAME_ENDED",
  "payload": {
    "results": [
      { "playerId": 3, "nickname": "플레이어2", "coins": 42, "rank": 1 },
      { "playerId": 2, "nickname": "홍길동",   "coins": 35, "rank": 2 }
    ]
  }
}
```

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
  ├─ POST /roll
  │    ├─ WS: DICE_ROLLED
  │    ├─ WS: PLAYER_MOVED
  │    │
  │    ├─ [분기점 없음]
  │    │    ├─ WS: TILE_TRIGGERED
  │    │    └─ WS: TURN_CHANGED (또는 GAME_ENDED)
  │    │
  │    └─ [분기점 있음] → WS: BRANCH_REQUIRED
  │         │
  │         ├─ POST /branch (20초 이내, 아니면 서버 자동 선택)
  │         │    ├─ [또 분기점] → WS: BRANCH_REQUIRED (반복)
  │         │    └─ [완료]
  │         │         ├─ WS: TILE_TRIGGERED
  │         │         └─ WS: TURN_CHANGED (또는 GAME_ENDED)
  │         │
  │         └─ [20초 타임아웃] → 서버 랜덤 선택 후 위와 동일
```

### 타임아웃

| 종류 | 시간 | 동작 |
|------|------|------|
| 턴 타임아웃 | 30초 | 서버가 자동으로 주사위 굴림 |
| 분기점 타임아웃 | 20초 | 서버가 랜덤으로 경로 선택 |

### 재접속

```
1. WS   CONNECT → SUBSCRIBE /topic/game/{id}
2. GET  /api/v1/games/{id}/state   현재 스냅샷으로 화면 복원
3. 이후 WebSocket 이벤트 수신
```

---

## 7. 미결 항목

| 항목 | 내용 |
|------|------|
| **학점(gpa) 누적 로직** | 현재 `gpa` 필드는 항상 0. 타일 이벤트/미니게임 상세 기획 후 구현 필요 |
| **타일 ID ↔ 노드 이름 변환** | 백엔드 `tileId`(정수)를 프론트 Phaser의 노드 이름(`"node14"`)으로 변환하는 로직 프론트에서 처리 필요 (`"node" + tileIndex`로 단순 변환 가능한지 확인 필요) |
