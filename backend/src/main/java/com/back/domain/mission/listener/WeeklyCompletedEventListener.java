package com.back.domain.mission.listener;

import com.back.domain.mission.event.WeeklyCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyCompletedEventListener {

    @TransactionalEventListener
    public void handleWeeklyCompleted(WeeklyCompletedEvent event) {
        log.info("═══════════════════════════════════════");
        log.info("📅 주차 완료 이벤트!");
        log.info("Member ID: {}", event.getMemberId());
        log.info("Mission ID: {}", event.getMissionId());
        log.info("Week Number: {}", event.getWeekNum());
        log.info("완료일: {}", event.getCompletedDate());
        log.info("═══════════════════════════════════════");

        // TODO: 보상 지급
        // rewardService.grantWeeklyReward(event.getMemberId(), event.getSubGoalId());

        // TODO: 업적 체크
        // achievementService.checkAndGrantAchievements(event.getMemberId());
    }
}