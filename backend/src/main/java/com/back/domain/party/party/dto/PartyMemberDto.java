package com.back.domain.party.party.dto;

import com.back.domain.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartyMemberDto {
    private Integer id;
    private String email;
    private String name;

    public PartyMemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
    }
}