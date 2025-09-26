package com.back.domain.mission.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionOverviewResponse {
    private List<MissionResponse> activeMissions; //진행 중인 미션들을 담는 리스트
    private List<MissionResponse> completedMissions; //완료된 미션들을 담는 리스트
    private Integer activeMissionCount; // 현재 진행 중인 미션 개수
    private Integer remainingSlots; // 사용자가 추가로 만들 수 있는 미션 수
}