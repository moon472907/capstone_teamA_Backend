package com.back.domain.mission.dto.response;

import com.back.domain.mission.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompleteResponse {
    private Integer taskId;
    private TaskStatus status;
    private LocalDate completedDate;

    // 보상 정보
    private Integer earnedPoints;
    private Integer earnedExp;

    // 진행률 정보
    private Integer dailyProgressRate;   // 0~100%
    private Integer weeklyProgressRate;  // 0~100%
    private Integer missionProgressRate; // 0~100%
}
