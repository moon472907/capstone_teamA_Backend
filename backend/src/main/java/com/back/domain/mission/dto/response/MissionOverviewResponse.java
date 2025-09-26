package com.back.domain.mission.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionOverviewResponse {
    private List<MissionResponse> activeMissions;
    private List<MissionResponse> completedMissions;
    private Integer activeMissionCount;
    private Integer remainingSlots;
}