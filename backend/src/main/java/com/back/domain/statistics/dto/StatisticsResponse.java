package com.back.domain.statistics.dto;

import com.back.domain.statistics.entity.MemberStatistics;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StatisticsResponse {

    // 데일리 통계
    private Integer dailyTotalCount;   // 총 데일리 완료 횟수
    private Integer dailyCurrentStreak; // 현재 연속 달성 일수
    private Integer dailyMaxStreak; // 최대 연속 달성 일수
    private LocalDate dailyLastCompletedDate;  // 마지막 완료 날짜

    // 주차별 통계
    private Integer weeklyTotalCount; // 총 주차 완료 횟수

    // 미션 통계
    private Integer missionTotalCount; // 총 미션 완료 횟수
    private Integer soloMissionCount; // 혼자 미션 완료 횟수
    private Integer partyMissionCount; // 파티 미션 완료 횟수

    // 추가 정보
    private Integer maxDailyTaskCount; // 하루 최대 Task 개수
    private LocalDate maxDailyTaskDate; // 하루 최대 Task 날짜

    public static StatisticsResponse from(MemberStatistics stats) {
        if (stats == null) {
            return StatisticsResponse.builder()
                    .dailyTotalCount(0)
                    .dailyCurrentStreak(0)
                    .dailyMaxStreak(0)
                    .weeklyTotalCount(0)
                    .missionTotalCount(0)
                    .soloMissionCount(0)
                    .partyMissionCount(0)
                    .maxDailyTaskCount(0)
                    .build();
        }

        return StatisticsResponse.builder()
                .dailyTotalCount(stats.getDailyTotalCount())
                .dailyCurrentStreak(stats.getDailyCurrentStreak())
                .dailyMaxStreak(stats.getDailyMaxStreak())
                .dailyLastCompletedDate(stats.getDailyLastCompletedDate())
                .weeklyTotalCount(stats.getWeeklyTotalCount())
                .missionTotalCount(stats.getMissionTotalCount())
                .soloMissionCount(stats.getSoloMissionCount())
                .partyMissionCount(stats.getPartyMissionCount())
                .maxDailyTaskCount(stats.getMaxDailyTaskCount())
                .maxDailyTaskDate(stats.getMaxDailyTaskDate())
                .build();
    }
}