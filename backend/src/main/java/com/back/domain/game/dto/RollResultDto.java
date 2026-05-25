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
    private Integer toNodeNumber;  // 프론트 매핑용: "node{toNodeNumber}" (1~53)
    private int tileIndex;
    private String tileType;
    private int coinsChange;
    private int totalCoins;
    private int starsChange;
    private int totalStars;
    private String tileEventDescription;
    private GameState nextState;
    private boolean gameEnded;
    // BRANCH_SELECT 상태일 때만 값이 채워짐
    private List<Integer> branchOptions;

    // CARD 타일 — 뽑은 카드 정보
    private String cardKey;
    private String cardType;
    private String cardTitle;
    private String cardDescription;
    // CARD_TARGET_SELECT 상태일 때만 — 지정 가능한 상대 playerId 목록
    private List<Integer> targetOptions;
    // BUS_SELECT 상태일 때만 — 이동 가능한 정류장 tileId 목록
    private List<Integer> busOptions;
}
