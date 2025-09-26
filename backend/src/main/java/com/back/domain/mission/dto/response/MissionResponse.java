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
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWeeks;
    private Integer currentWeek;
    private boolean isCompleted;
    private boolean isPartyMission;
    private Integer partyId;
    private Integer progressRate;
    private List<SubGoalResponse> subGoals;
}