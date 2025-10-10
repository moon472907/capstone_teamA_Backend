package com.back.domain.member.dto;

import com.back.domain.member.entity.MemberStatistic;

public record StatisticDto(
        int countDaily,
        int countWeekly,
        int countChallenge
) {
    public StatisticDto(MemberStatistic statistic) {
        this(
                statistic.getCountDaily(),
                statistic.getCountWeekly(),
                statistic.getCountChallenge()
        );
    }
}