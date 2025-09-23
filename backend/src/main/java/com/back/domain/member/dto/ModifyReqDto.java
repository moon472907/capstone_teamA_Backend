package com.back.domain.member.dto;

import com.back.domain.member.entity.MemberGender;

import java.time.LocalDate;

public record ModifyReqDto(
        String name,
        LocalDate birth,
        MemberGender gender
) {

}