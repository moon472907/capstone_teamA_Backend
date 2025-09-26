package com.back.domain.mission.dto.request;

import com.back.domain.mission.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCompleteRequest {
    @NotNull(message = "테스크 ID는 필수입니다.")
    private  Integer taskId;

    @NotNull(message = "완료 상태는 필수입니다.")
    private TaskStatus status;

    private LocalDate date; //  null이면 오늘
}
