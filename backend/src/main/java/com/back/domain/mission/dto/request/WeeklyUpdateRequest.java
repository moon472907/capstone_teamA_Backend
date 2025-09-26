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
public class WeeklyUpdateRequest {

    @NotNull(message = "미션 ID는 필수입니다")
    private Integer missionId;

    @NotNull(message = "서브골 ID는 필수입니다")
    private Integer subGoalId;

    @NotEmpty(message = "수정할 태스크가 필요합니다")
    private List<TaskUpdateDto> taskUpdates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskUpdateDto {
        private Integer taskId;
        private String title;
    }
}