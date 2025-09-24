package com.back.domain.mission.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressResponse {

    private String type;  // DAILY, WEEKLY, MISSION
    private Integer progressRate;  // 진행률 (0~100)

    // Optional fields
    private LocalDate date;  // 일일 진행률용
    private Integer missionId;  // 미션 진행률용
    private Integer weekNum;  // 주간 진행률용
    private Integer currentWeek;  // 현재 주차
}