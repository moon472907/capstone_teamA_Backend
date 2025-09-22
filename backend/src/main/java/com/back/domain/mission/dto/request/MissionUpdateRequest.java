package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class MissionUpdateRequest { // 미션 수정 요청 DTO (Task title만 수정)

    @NotNull(message = "미션 ID는 필수입니다")
    private Integer missionId;

    // 태스크 제목만 수정 가능
    @NotEmpty(message = "수정할 태스크가 최소 1개는 있어야 합니다")
    private List<TaskUpdateRequest> taskUpdates;

    @NotNull(message = "수정 확인은 필수입니다")
    private Boolean confirmUpdate;
}
