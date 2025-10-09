package com.back.domain.mission.event;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MissionCompletedEvent {
    private Integer missionId;
    private Integer memberId;
    private boolean isPartyMission;
    private Integer partyId;
    private LocalDate completedDate;
}