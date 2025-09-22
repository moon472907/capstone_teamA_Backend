package com.back.domain.mission.dto.request;


import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionCreateRequest { // 미션 생성 요청 DTO
    @NotBlank(message="미션 목표는 필수입니다.")
    private String title;

    @NotNull(message = "기간은 필수입니다.")
    @Min(value = 1, message = "최소 1주 이상이어야 합니다. ")
    @Max(value = 4, message = "최대 4주까지 가능합니다.")
    private Integer periodWeeks;

    @NotNull(message = "미션 유형을 선택해주세요")
    private MissionType type; // AI 또는 CUSTOM

    private MissionCategory category;

    @AssertTrue(message = "커스텀 미션일 경우 카테고리가 필요합니다")
    public boolean isCategoryRequiredForCustom() {
        return type != MissionType.CUSTOM || category != null;
    }
}
