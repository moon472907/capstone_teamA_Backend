package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekTaskUpdateRequest {

    @NotNull(message = "SubGoal ID는 필수입니다")
    private Integer subGoalId;

    @NotEmpty(message = "수정할 Task 목록이 필요합니다")
    private List<TaskUpdateDto> tasks;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskUpdateDto {
        @NotNull(message = "Task ID는 필수입니다")
        private Integer taskId;

        @NotNull(message = "제목은 필수입니다")
        private String title;
    }

}
