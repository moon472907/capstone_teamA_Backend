package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateRequest { // 테스크 생성 요청 DTO
    @NotNull(message = "태스크 제목은 필수입니다.")
    private String title;

    @NotNull(message = "요일은 필수입니다")
    @Min(value = 1, message = "1(월요일)부터 7(일요일)까지")
    @Max(value = 7, message = "1(월요일)부터 7(일요일)까지")
    private Integer dayNum; // 요일만 (1=월요일, 7=일요일)
}
