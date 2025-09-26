package com.back.domain.mission.dto.ai;

import com.back.domain.mission.enums.MissionCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiMissionResult {
    private String goal;
    private MissionCategory category;
    private List<WeeklyPlan> weeklyPlans;
}