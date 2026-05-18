package com.back.global.websocket;

public enum MessageType {
    GAME_STARTED,
    TURN_CHANGED,
    DICE_ROLLED,
    PLAYER_MOVED,
    BRANCH_REQUIRED,
    TILE_TRIGGERED,
    GAME_ENDED,
    PLAYER_JOINED,
    PLAYER_LEFT,
    PLAYER_READY,
    STATE_SNAPSHOT,
    ERROR
}