package com.back.domain.mission.listener;

import com.back.domain.mission.event.MissionCompletedEvent;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@lombok.extern.slf4j.Slf4j
@Component
@Slf4j
public class MissionCompletedEventListener {

    @TransactionalEventListener
    public void handleMissionCompleted(MissionCompletedEvent event) {
        log.info("═══════════════════════════════════════");
        log.info("🎉 미션 완료 이벤트 발행됨!");
        log.info("Mission ID: {}", event.getMissionId());
        log.info("Member ID: {}", event.getMemberId());
        log.info("완료일: {}", event.getCompletedDate());
        log.info("═══════════════════════════════════════");

    }
}
