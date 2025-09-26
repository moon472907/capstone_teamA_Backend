package com.back.domain.mission.dto.response;

import com.back.domain.mission.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Integer taskId;
    private String title;
    private Integer dayNum;
    private TaskStatus status;
    private LocalDate lastCompletedDate;
    private boolean isToday;
}