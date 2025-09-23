package com.back.domain.party.party.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartyUpdateRequestDto {
    @Size(min = 2, max = 20, message = "파티 이름은 2자 이상 20자 이하로 입력해주세요.")
    private String name;

    @NotNull(message = "최대 멤버 수는 필수 입력 항목입니다.")
    @Min(value = 2, message = "최소 멤버 수는 2명(파티장 포함)입니다.")
    @Max(value = 5, message = "최대 멤버 수는 5명입니다.")
    private Integer maxMembers;

    private Boolean isPublicStatus;
}