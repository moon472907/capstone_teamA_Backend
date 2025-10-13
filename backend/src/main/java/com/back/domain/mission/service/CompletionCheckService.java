package com.back.domain.mission.service;

import com.back.domain.mission.entity.*;
import com.back.domain.mission.event.DailyCompletedEvent;
import com.back.domain.mission.event.MissionCompletedEvent;
import com.back.domain.mission.event.WeeklyCompletedEvent;
import com.back.domain.mission.repository.DailyCompletionLogRepository;
import com.back.domain.mission.repository.MissionCompletionLogRepository;
import com.back.domain.mission.repository.SubGoalCompletionLogRepository;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.service.RewardService;
import com.back.domain.statistics.service.StatisticsService;
import com.back.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CompletionCheckService {

    private final TimeProvider timeProvider;
    private final MissionCalculateService calculateService;

    private final DailyCompletionLogRepository dailyCompletionLogRepository;
    private final SubGoalCompletionLogRepository subGoalCompletionLogRepository;
    private final MissionCompletionLogRepository missionCompletionLogRepository;

    private final StatisticsService statisticsService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RewardService rewardService;

    public void checkAllCompletions(Integer memberId, Task task, LocalDate date) {
        SubGoal subGoal = task.getSubGoal();
        Mission mission = subGoal.getMission();

        checkDailyCompletion(memberId, date);
        checkWeeklyCompletion(memberId, subGoal);
        checkMissionCompletion(memberId, mission);
    }

    public void recheckAfterCancellation(Integer memberId, Task task, LocalDate date) {
        SubGoal subGoal = task.getSubGoal();
        Mission mission = subGoal.getMission();

        recheckDailyCompletion(memberId, date);
        recheckWeeklyCompletion(memberId, subGoal);
        recheckMissionCompletion(memberId, mission);
    }

    // ========== 데일리 ==========

    private void checkDailyCompletion(Integer memberId, LocalDate date) {
        if (dailyCompletionLogRepository.existsByMemberIdAndCompletedDate(memberId, date)) {
            return;
        }

        Integer progress = calculateService.calculateDailyProgress(memberId, date);

        if (progress >= 80) {
            dailyCompletionLogRepository.save(DailyCompletionLog.builder()
                    .memberId(memberId)
                    .completedDate(date)
                    .build());

            statisticsService.onDailyCompleted(memberId, date);

            try {
                rewardService.giveRewardByType(memberId, RewardType.DAILYCLEAR);
            } catch (Exception ignored) {
            }

            applicationEventPublisher.publishEvent(DailyCompletedEvent.builder()
                    .memberId(memberId)
                    .completedDate(date)
                    .build());
        }
    }

    private void recheckDailyCompletion(Integer memberId, LocalDate date) {
        Optional<DailyCompletionLog> logOpt = dailyCompletionLogRepository
                .findByMemberIdAndCompletedDate(memberId, date);

        if (logOpt.isEmpty()) return;

        Integer progress = calculateService.calculateDailyProgress(memberId, date);

        if (progress < 80) {
            dailyCompletionLogRepository.delete(logOpt.get());
            statisticsService.onDailyCancelled(memberId, date);
            try {
                rewardService.revokeRewardByType(memberId, RewardType.DAILYCLEAR);
            } catch (Exception ignored) {
            }
        }
    }

    // ========== 주차 ==========

    private void checkWeeklyCompletion(Integer memberId, SubGoal subGoal) {
        if (subGoalCompletionLogRepository.existsBySubGoalIdAndMemberId(subGoal.getId(), memberId)) {
            return;
        }

        Integer progress = calculateService.calculateWeekProgressForMember(subGoal, memberId);

        if (progress >= 80) {
            subGoalCompletionLogRepository.save(SubGoalCompletionLog.builder()
                    .subGoalId(subGoal.getId())
                    .memberId(memberId)
                    .completedDate(timeProvider.today())
                    .weekNum(subGoal.getOrderNum())
                    .build());

            statisticsService.onWeeklyCompleted(memberId);

            try {
                rewardService.giveRewardByType(memberId, RewardType.WEEKLYCLEAR);
            } catch (Exception ignored) {
            }

            applicationEventPublisher.publishEvent(WeeklyCompletedEvent.builder()
                    .memberId(memberId)
                    .subGoalId(subGoal.getId())
                    .missionId(subGoal.getMission().getId())
                    .weekNum(subGoal.getOrderNum())
                    .completedDate(timeProvider.today())
                    .build());
        }
    }

    private void recheckWeeklyCompletion(Integer memberId, SubGoal subGoal) {
        Optional<SubGoalCompletionLog> logOpt = subGoalCompletionLogRepository
                .findBySubGoalIdAndMemberId(subGoal.getId(), memberId);

        if (logOpt.isEmpty()) return;

        Integer progress = calculateService.calculateWeekProgressForMember(subGoal, memberId);

        if (progress < 80) {
            subGoalCompletionLogRepository.delete(logOpt.get());
            statisticsService.onWeeklyCancelled(memberId);
        }
    }

    // ========== 미션 ==========

    private void checkMissionCompletion(Integer memberId, Mission mission) {
        if (missionCompletionLogRepository.existsByMissionIdAndMemberId(mission.getId(), memberId)) {
            return;
        }

        Integer progress = calculateService.calculateMissionProgressForMember(mission, memberId);

        if (progress >= 80) {
            LocalDate today = timeProvider.today();

            missionCompletionLogRepository.save(MissionCompletionLog.builder()
                    .missionId(mission.getId())
                    .memberId(memberId)
                    .completedDate(today)
                    .build());

            statisticsService.onMissionCompleted(memberId, mission.isPartyMission());

            try {
                rewardService.giveRewardByType(memberId, RewardType.CHALLENGECLEAR);
            } catch (Exception ignored) {
            }

            applicationEventPublisher.publishEvent(MissionCompletedEvent.builder()
                    .missionId(mission.getId())
                    .memberId(memberId)
                    .PartyMission(mission.isPartyMission())
                    .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                    .completedDate(today)
                    .build());


            if (!mission.isPartyMission() && !mission.isCompleted()) {
                mission.setCompleted(true);
            }
        }
    }

    private void recheckMissionCompletion(Integer memberId, Mission mission) {
        Optional<MissionCompletionLog> logOpt = missionCompletionLogRepository
                .findByMissionIdAndMemberId(mission.getId(), memberId);

        if (logOpt.isEmpty()) return;

        Integer progress = calculateService.calculateMissionProgressForMember(mission, memberId);

        if (progress < 80) {
            missionCompletionLogRepository.delete(logOpt.get());
            statisticsService.onMissionCancelled(memberId, mission.isPartyMission());


            if (!mission.isPartyMission()) {
                mission.setCompleted(false);
            }
        }
    }
}