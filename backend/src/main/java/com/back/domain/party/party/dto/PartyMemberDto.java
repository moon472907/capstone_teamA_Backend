package com.back.domain.party.party.dto;

import com.back.domain.member.entity.Member;
import com.back.domain.title.entity.Title;
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
    private String status;

    private String title;
    private ItemDecorationDto item;

    public PartyMemberDto(Member member, String missionStatus) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.status = missionStatus;

        // Member Entity에서 장착된 칭호 정보를 가져와 매핑
        Title equippedTitle = member.getTitle(); // Member.title (장착된 칭호)
        this.title = (equippedTitle != null) ? equippedTitle.getContent() : null;

        this.item = (member.getItem() != null)
                ? new ItemDecorationDto(member.getItem())
                : null;
    }

    public PartyMemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.status = null;

        // Member Entity에서 장착된 칭호 정보를 가져와 매핑
        Title equippedTitle = member.getTitle();
        this.title = (equippedTitle != null) ? equippedTitle.getContent() : null;

        this.item = (member.getItem() != null)
                ? new ItemDecorationDto(member.getItem())
                : null;
    }
}