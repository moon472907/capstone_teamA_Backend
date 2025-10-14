package com.back.domain.member.dto;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;

import java.time.LocalDate;

public record MemberDto(
        Integer id,
        String email,
        String name,
        String code,
        LocalDate birth,
        MemberGender gender,
        Integer level,
        Integer xp,
        Integer xpReq,
        Integer money,
        String title,
        String item
) {
    public MemberDto(Member member) {
        this(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getCode(),
                member.getBirth(),
                member.getGender(),
                member.getLevel(),
                member.getXp(),
                member.getXpReq(),
                member.getMoney(),
                member.getTitle() != null ? member.getTitle().getContent() : null,
                member.getItem() != null ? member.getItem().getImg() : null
        );
    }
}