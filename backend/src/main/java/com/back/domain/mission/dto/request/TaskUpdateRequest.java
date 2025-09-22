package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TaskUpdateRequest { //Task 수정 요청 DTO
    @NotNull(message = "테스크 ID는 필수입니다.")
    private Integer taskId;

    @NotNull(message = "테스크 제목은 필수입니다.")
    private String title;
}
