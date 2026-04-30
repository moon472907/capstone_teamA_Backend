package com.back.global.websocket;

public enum MessageType {
    GAME_STARTED,
    TURN_CHANGED,
    DICE_ROLLED,
    PLAYER_MOVED,
    TILE_TRIGGERED,
    GAME_ENDED,
    PLAYER_JOINED,
    STATE_SNAPSHOT,
    ERROR
}