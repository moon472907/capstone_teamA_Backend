package com.back.domain.mission.dto.ai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTask {
    private Integer dayNum;
    private String title;
}