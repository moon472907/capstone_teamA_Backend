package com.back.domain.level.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "level_xp")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class LevelXP {

    @Id
    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "xp_to_next", nullable = false)
    private Integer xpToNext;

    // Level 30 이후 고정 요구량 처리를 위한 상수 정의
    public static final int FIXED_XP_REQUIREMENT = 30000;

}