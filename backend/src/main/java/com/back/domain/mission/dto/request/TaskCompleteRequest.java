package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCompleteRequest {
    @NotNull(message = "테스크 ID는 필수입니다.")
    private  Integer taskId;

}
