package com.back.domain.party.party.dto;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
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
    private Integer missionId;
    private MissionCategory category;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate createDate;
    private Integer views;


    public PartyDto(Party party, Mission mission) {
        this.id = party.getId();
        this.name = party.getName();
        this.leaderId = party.getLeader().getId();

        this.currentMembers = (int) party.getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .count();

        this.maxMembers = party.getMaxMembers();
        this.isPublic = party.isPublic();

        this.createDate = party.getCreateDate().toLocalDate();

        this.views = party.getViews();

        this.missionId = mission != null ? mission.getId() : null;

        if (mission != null) {
            this.category = mission.getCategory();
            this.startDate = mission.getStartDate();
            this.endDate = mission.getEndDate();
        }

        this.members = party.getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .map(partyMember -> new PartyMemberDto(partyMember.getMember()))
                .collect(Collectors.toList());
    }

    public PartyDto(Party party) {
        this.id = party.getId();
        this.name = party.getName();
        this.leaderId = party.getLeader().getId();

        this.currentMembers = (int) party.getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .count();

        this.maxMembers = party.getMaxMembers();
        this.isPublic = party.isPublic();

        this.createDate = party.getCreateDate().toLocalDate();
        this.views = party.getViews();

        this.category = null;
        this.startDate = null;
        this.endDate = null;

        this.members = party.getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .map(partyMember -> new PartyMemberDto(partyMember.getMember()))
                .collect(Collectors.toList());
    }

}