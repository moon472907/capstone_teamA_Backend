package com.back.domain.mission.dto.response;

import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse {
    private Integer missionId;
    private String title;
    private MissionCategory category;
    private MissionType type;
    private LocalDate startDate; // 시작일 (항상 월요일)
    private LocalDate endDate;
    private Integer totalWeeks; // 전체 기간 주단위
    private Integer currentWeek; // 현재 몇주차인지
    private boolean isCompleted; //미션 완료 여부
    private boolean isPartyMission; //파티 미션 여부 (개인인지 / 파티인지)
    private Integer partyId; // 파티일경우
    private Integer progressRate; //미션 전체 진행률 (개인)
    private List<SubGoalResponse> subGoals;
}