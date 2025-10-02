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
    private Integer dayNum; // 요일
    private TaskStatus status; // 상태
    private LocalDate lastCompletedDate; // 마지막 완료 일자
    private boolean isToday; // 오늘 해야하는 task인지 여부

    // 🔹 수정 관련 필드
    private boolean hasBeenEdited;  // 이미 수정됐는지 여부
    private boolean canEdit;        // 지금 시점에 수정 가능한지 여부
    private LocalDate editDeadline;  // 수정 마감일
}
