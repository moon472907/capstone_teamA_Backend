package com.back.domain.party.party.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberIdRequest {
    @NotNull(message = "멤버 ID는 필수 입력 항목입니다.")
    private Integer memberId;
}