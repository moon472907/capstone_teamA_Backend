package com.back.domain.party.party.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationDto {
    @NotBlank(message = "초대 코드는 필수 입력 항목입니다.")
    private String invitedMemberCode;
}