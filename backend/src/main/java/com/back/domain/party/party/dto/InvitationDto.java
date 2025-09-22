package com.back.domain.party.party.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvitationDto {
    @NotNull(message = "초대할 멤버의 이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String invitedMemberEmail;
}