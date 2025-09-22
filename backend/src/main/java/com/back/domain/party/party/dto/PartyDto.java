package com.back.domain.party.party.dto;

import com.back.domain.party.party.entity.Party;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartyDto {
    private Integer id;
    private String name;
    private Integer maxMembers;
    private Boolean isPublic;
    private Integer leaderId;
    // 미션 도메인 완성 시 사용
    // private Integer missionId;

    public PartyDto(Party party) {
        this.id = party.getId();
        this.name = party.getName();
        this.maxMembers = party.getMaxMembers();
        this.isPublic = party.isPublic();
        this.leaderId = party.getLeader().getId();
        // missionId는 미션 도메인 완성 후 추가
        // this.missionId = party.getMission().getId();
    }
}