package com.back.domain.mission.dto.request;

import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyMissionCreateRequest {

    @NotBlank(message = "미션 제목을 입력해주세요")
    private String title;

    @NotNull(message = "미션 타입을 선택해주세요")
    private MissionType type;  // AI or CUSTOM

    private MissionCategory category;  // CUSTOM일 때만 필수

    @NotNull(message = "미션 기간을 선택해주세요")
    @Min(value = 1, message = "최소 1주")
    @Max(value = 4, message = "최대 4주")
    private Integer periodWeeks;

    @NotNull(message = "최대 인원을 설정해주세요")
    @Min(value = 1, message = "최소 1명")
    @Max(value = 5, message = "최대 5명")
    private Integer maxMembers;  // 1 = 개인미션, 2~5 = 파티미션

    @JsonProperty("isPublic")
    private boolean isPublic = false;  // 파티 공개 여부 (파티모집 게시판)


    //검증 어노테이션

    @JsonIgnore
    @AssertTrue(message = "커스텀 미션은 카테고리가 필수입니다")
    public boolean validateCategory() {
        return type != MissionType.CUSTOM || category != null;
    }

}