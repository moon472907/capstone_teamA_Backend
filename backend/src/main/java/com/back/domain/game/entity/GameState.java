package com.back.domain.game.entity;

public enum GameState {
    WAITING,
    IN_PROGRESS,
    TURN_START,
    DICE_ROLL,
    MOVE,
    BRANCH_SELECT,
    TILE_EVENT,
    TURN_END,
    GAME_END
}