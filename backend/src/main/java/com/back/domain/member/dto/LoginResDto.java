package com.back.domain.member.dto;

public record LoginResDto(
        MemberDto item,
        String accessToken
) {
}
