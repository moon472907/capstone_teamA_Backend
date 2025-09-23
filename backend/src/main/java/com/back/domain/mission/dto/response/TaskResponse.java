package com.back.domain.mission.dto.response;


import com.back.domain.mission.enums.TaskStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse  {
    private Integer taskId;
    private String title;
    private Integer dayNum;   // 1=월요일, 2=화요일 ...

    // 완료 상태 정보 (상세 조회시에만)
    private TaskStatus status;
    private LocalDate lastCompletedDate;
    private boolean isToday; // 오늘의 태스크인지
}
