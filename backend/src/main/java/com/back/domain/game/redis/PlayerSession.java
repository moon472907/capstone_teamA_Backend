package com.back.domain.game.redis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSession {
    private Integer playerId;
    private Integer memberId;
    private String nickname;
    private String characterKey;
    private Integer tileId;
    private int coins;
    private int gpa;
    private boolean connected;
}
