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
    private Integer weekNum;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean hasBeenEdited;
    private LocalDate editableUntil;
    private String editStatus;
    private Integer weekProgressRate;
    private List<TaskResponse> tasks;
}
