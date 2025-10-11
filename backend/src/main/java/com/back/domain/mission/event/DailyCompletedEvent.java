package com.back.domain.mission.event;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class DailyCompletedEvent {
    private Integer memberId;
    private LocalDate completedDate;
}