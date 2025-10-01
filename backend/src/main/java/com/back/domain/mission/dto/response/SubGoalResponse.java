package com.back.domain.mission.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGoalResponse {
    private Integer subGoalId;
    private String title;
    private Integer weekNum; // 몇번쨰 주차인지
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer weekProgressRate; // 개인 미션 진행률
    private PartyWeekProgressDto partyWeekProgress; // 파티 미션 진행률

    private boolean visible;
    private List<TaskResponse> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartyWeekProgressDto {
        private Integer myProgress;
        private Integer averageProgress;
    }
}
