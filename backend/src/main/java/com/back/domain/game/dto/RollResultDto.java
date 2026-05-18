package com.back.domain.game.dto;

import com.back.domain.game.entity.GameState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollResultDto {
    private int diceValue;
    private int fromTileId;
    private int toTileId;
    private int tileIndex;
    private String tileType;
    private int coinsChange;
    private int totalCoins;
    private String tileEventDescription;
    private GameState nextState;
    private boolean gameEnded;
    // BRANCH_SELECT 상태일 때만 값이 채워짐
    private List<Integer> branchOptions;
}
