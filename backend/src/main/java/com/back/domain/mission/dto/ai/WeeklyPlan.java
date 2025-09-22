package com.back.domain.mission.dto.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyPlan {
    private Integer weekNum;
    private String title;
    private List<DailyTask> dailyTasks;
}
