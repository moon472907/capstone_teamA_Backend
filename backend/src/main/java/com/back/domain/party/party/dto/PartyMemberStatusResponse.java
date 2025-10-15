package com.back.domain.party.party.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PartyMemberStatusResponse {
    private Integer memberId;
    private String status;
}
