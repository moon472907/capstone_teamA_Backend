package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;

import java.time.LocalDate;

public record MemberDto(
        Integer id,
        String email,
        String name,
        String code,
        LocalDate birth
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getCode(),
                member.getBirth()
        );
    }
}