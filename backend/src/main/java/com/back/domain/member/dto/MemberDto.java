package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;

import java.time.LocalDate;

public record MemberDto(
        String name,
        String code,
        LocalDate birth,
        MemberGender gender,
        int level,
        int xp,
        int money
) {
    public MemberDto(Member member) {
        this(
                member.getName(),
                member.getCode(),
                member.getBirth(),
                member.getGender(),
                member.getLevel(),
                member.getXp(),
                member.getMoney()
        );
    }
}