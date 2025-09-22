package com.back.domain.mission.dto.response;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionOverviewResponse { //미션 현황 응답
    private List<MissionResponse> activeMissions;
    private List<MissionResponse> completedMissions;
    private Integer activeMissionCount;
    private Integer remainingSlots; // 5 - 활성 미션 수
}
