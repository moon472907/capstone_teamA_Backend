package com.back.domain.party.party.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartyRequestDto {
    @NotBlank(message = "파티 이름은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "파티 이름은 2자 이상 20자 이하로 입력해주세요.")
    private String name;

    @NotNull(message = "최대 멤버 수는 필수 입력 항목입니다.")
    private Integer maxMembers;

    @NotNull(message = "공개 여부는 필수 입력 항목입니다.")
    private Boolean isPublicStatus;

    // 미션 도메인 완성 시 사용
    // private Integer missionId;
}