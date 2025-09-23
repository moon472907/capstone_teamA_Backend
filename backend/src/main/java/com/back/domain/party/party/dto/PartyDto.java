package com.back.domain.party.party.dto;

import com.back.domain.party.party.entity.Party;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class PartyDto {
    private Integer id;
    private String name;
    private Integer leaderId;
    private Integer currentMembers;
    private Integer maxMembers;
    private Boolean isPublic;
    private List<PartyMemberDto> members;

    // 미션 도메인 완성 시 사용
    // private Integer missionId;

    public PartyDto(Party party) {
        this.id = party.getId();
        this.name = party.getName();
        this.leaderId = party.getLeader().getId();
        this.currentMembers = party.getPartyMembers().size();
        this.maxMembers = party.getMaxMembers();
        this.isPublic = party.isPublic();
        this.members = party.getPartyMembers().stream()
                .map(partyMember -> new PartyMemberDto(partyMember.getMember()))
                .collect(Collectors.toList());
        // missionId는 미션 도메인 완성 후 추가
        // this.missionId = party.getMission().getId();
    }
}