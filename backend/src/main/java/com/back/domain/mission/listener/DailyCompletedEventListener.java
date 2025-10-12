package com.back.domain.mission.listener;

import com.back.domain.mission.event.DailyCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyCompletedEventListener {

    // TODO: 보상 서비스 주입
    // private final RewardService rewardService;

    @TransactionalEventListener
    public void handleDailyCompleted(DailyCompletedEvent event) {
        log.info("═══════════════════════════════════════");
        log.info("🎉 데일리 완료 이벤트!");
        log.info("Member ID: {}", event.getMemberId());
        log.info("완료일: {}", event.getCompletedDate());
        log.info("═══════════════════════════════════════");

        // TODO: 보상 지급
        // rewardService.grantDailyReward(event.getMemberId(), event.getCompletedDate());

        // TODO: 업적 체크
        // achievementService.checkAndGrantAchievements(event.getMemberId());
    }
}