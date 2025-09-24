package com.back.domain.mission.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGoalCreateRequest { // 세부 목표 생성 요청 DTO

    @NotBlank(message = "세부목표 제목은 필수입니다")
    private String title;

    @NotNull(message = "주차 번호는 필수입니다")
    @Min(value = 1, message = "1주차부터 시작됩니다")
    private Integer weekNum;
    // 몇 주차인지만 (날짜는 Service에서 계산)
}
