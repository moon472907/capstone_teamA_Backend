package com.back.domain.mission.dto.response;

import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse { //미션 응답 목록, 상세, 생성

    private Integer missionId;
    private String title;
    private MissionCategory category;
    private MissionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalWeeks;
    private Integer currentWeek;
    private boolean isCompleted;
    private boolean isEditable;
    private Integer progressRate;

    // 상세 조회시에만 포함 (목록 조회시에는 null)
    private List<SubGoalResponse> subGoals;


}
