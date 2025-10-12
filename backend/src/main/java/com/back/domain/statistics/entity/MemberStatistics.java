package com.back.domain.statistics.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "member_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatistics extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Integer memberId;

    // 데일리 통계
    @Column(nullable = false)
    @Builder.Default
    private Integer dailyTotalCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer dailyCurrentStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer dailyMaxStreak = 0;

    private LocalDate dailyLastCompletedDate;

    // 주차별 통계
    @Column(nullable = false)
    @Builder.Default
    private Integer weeklyTotalCount = 0;

    // 미션 통계
    @Column(nullable = false)
    @Builder.Default
    private Integer missionTotalCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer soloMissionCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer partyMissionCount = 0;

    // 하루 최대 기록
    @Column(nullable = false)
    @Builder.Default
    private Integer maxDailyTaskCount = 0;

    private LocalDate maxDailyTaskDate;

}