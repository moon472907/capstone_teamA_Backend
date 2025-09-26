package com.back.domain.mission.dto.response;

import com.back.domain.mission.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCompleteResponse {
    private Integer taskId;
    private TaskStatus status;
    private LocalDate completedDate;
    private Integer earnedPoints; //task로 획득한 포인트
    private Integer earnedExp; //task로 획득한 경험치
    private Integer dailyProgressRate; //일일 진행률
    private Integer weeklyProgressRate; //주차별 진행률
    private Integer missionProgressRate; //전체 미션 진행률
}