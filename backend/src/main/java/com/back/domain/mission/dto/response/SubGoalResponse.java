package com.back.domain.mission.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGoalResponse {
    private Integer subGoalId;
    private String title;
    private Integer weekNum; // 몇번쨰 주차인지
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean hasBeenEdited; //사용자가 한번이라도 수정했는지
    private LocalDate editableUntil; // 수정 가능 기한
    private String editStatus; // 수정 상태 표시
    private Integer weekProgressRate; //해당주차 진행률
    private List<TaskResponse> tasks;
}
