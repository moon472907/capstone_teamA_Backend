package com.back.domain.mission.event;

import com.back.domain.mission.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;


@Getter
@Builder
public class TaskCompletedEvent {
    private Integer memberId;
    private Integer taskId;
    private Integer missionId;
    private Integer subGoalId;
    private LocalDate completedDate;
    private TaskStatus status;
}
