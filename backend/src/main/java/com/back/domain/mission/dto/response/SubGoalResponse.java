package com.back.domain.mission.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubGoalResponse {
    private Integer subGoalId;
    private String title;
    private Integer weekNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean editable;
    private boolean currentWeek; // 현재 진행 주차인지
    private Integer weekProgressRate; // 0~100%

    // 상세 조회시에만 포함
    private List<TaskResponse> tasks;

}
