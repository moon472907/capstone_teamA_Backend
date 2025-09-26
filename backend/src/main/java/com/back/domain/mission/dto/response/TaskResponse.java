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

    //private boolean hasBeenEdited;// 이미 수정된 Task인지 여부 (true면 1회 수정 끝)
    //private LocalDate editableUntil;// 수정 가능 기한 (ex: 이번 주 종료일까지)
    //private String editStatus;   // 수정 상태 표시 ("가능", "불가", "기간만료")

}