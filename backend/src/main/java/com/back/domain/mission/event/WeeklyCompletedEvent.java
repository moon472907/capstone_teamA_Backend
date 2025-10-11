package com.back.domain.mission.event;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class WeeklyCompletedEvent {
    private Integer memberId;
    private Integer subGoalId;
    private Integer missionId;
    private Integer weekNum;
    private LocalDate completedDate;
}