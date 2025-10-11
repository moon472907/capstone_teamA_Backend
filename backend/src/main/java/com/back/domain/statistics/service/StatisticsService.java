package com.back.domain.statistics.service;

import com.back.domain.mission.entity.DailyCompletionLog;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.DailyCompletionLogRepository;
import com.back.domain.mission.repository.MissionCompletionLogRepository;
import com.back.domain.mission.repository.SubGoalCompletionLogRepository;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.statistics.entity.MemberStatistics;
import com.back.domain.statistics.repository.MemberStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatisticsService {

    private final MemberStatisticsRepository memberStatisticsRepository;
    private final DailyCompletionLogRepository dailyCompletionLogRepository;
    private final SubGoalCompletionLogRepository subGoalCompletionLogRepository;
    private final MissionCompletionLogRepository missionCompletionLogRepository;
    private final TaskLogRepository taskLogRepository;

    @Transactional(readOnly = true)
    public MemberStatistics getStatistics(Integer memberId) {
        return memberStatisticsRepository.findByMemberId(memberId)
                .orElseGet(() -> MemberStatistics.builder()
                        .memberId(memberId)
                        .build());
    }

    // 데일리 완료 시
    public void onDailyCompleted(Integer memberId, LocalDate date) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = dailyCompletionLogRepository.countByMemberId(memberId);
        stats.setDailyTotalCount(totalCount.intValue());

        updateStreak(stats, date);
        updateMaxDailyTaskCount(stats, memberId, date);

        memberStatisticsRepository.save(stats);
    }

    public void onDailyCancelled(Integer memberId, LocalDate date) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = dailyCompletionLogRepository.countByMemberId(memberId);
        stats.setDailyTotalCount(totalCount.intValue());

        recalculateStreak(stats, memberId);

        memberStatisticsRepository.save(stats);
    }

    // 주차 완료 시
    public void onWeeklyCompleted(Integer memberId) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = subGoalCompletionLogRepository.countByMemberId(memberId);
        stats.setWeeklyTotalCount(totalCount.intValue());

        memberStatisticsRepository.save(stats);
    }

    public void onWeeklyCancelled(Integer memberId) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = subGoalCompletionLogRepository.countByMemberId(memberId);
        stats.setWeeklyTotalCount(totalCount.intValue());

        memberStatisticsRepository.save(stats);
    }

    // 미션 완료 시
    public void onMissionCompleted(Integer memberId, boolean isParty) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = missionCompletionLogRepository.countByMemberId(memberId);
        stats.setMissionTotalCount(totalCount.intValue());

        if (isParty) {
            stats.setPartyMissionCount(stats.getPartyMissionCount() + 1);
        } else {
            stats.setSoloMissionCount(stats.getSoloMissionCount() + 1);
        }

        memberStatisticsRepository.save(stats);
    }

    public void onMissionCancelled(Integer memberId, boolean isParty) {
        MemberStatistics stats = getOrCreate(memberId);

        Long totalCount = missionCompletionLogRepository.countByMemberId(memberId);
        stats.setMissionTotalCount(totalCount.intValue());

        if (isParty) {
            stats.setPartyMissionCount(Math.max(0, stats.getPartyMissionCount() - 1));
        } else {
            stats.setSoloMissionCount(Math.max(0, stats.getSoloMissionCount() - 1));
        }

        memberStatisticsRepository.save(stats);
    }

    // 🆕 하루 최대 Task 개수 업데이트
    private void updateMaxDailyTaskCount(MemberStatistics stats, Integer memberId, LocalDate date) {
        // 오늘 완료한 Task 개수
        Long todayCount = taskLogRepository.countByMemberIdAndDateAndStatus(
                memberId, date, TaskStatus.COMPLETED
        );

        int count = todayCount.intValue();

        // 최대 기록 갱신
        if (count > stats.getMaxDailyTaskCount()) {
            stats.setMaxDailyTaskCount(count);
            stats.setMaxDailyTaskDate(date);
        }
    }

    // 연속 달성 계산
    private void updateStreak(MemberStatistics stats, LocalDate date) {
        LocalDate last = stats.getDailyLastCompletedDate();

        if (last == null) {
            stats.setDailyCurrentStreak(1);
        } else if (last.equals(date.minusDays(1))) {
            stats.setDailyCurrentStreak(stats.getDailyCurrentStreak() + 1);
        } else if (!last.equals(date)) {
            stats.setDailyCurrentStreak(1);
        }

        if (stats.getDailyCurrentStreak() > stats.getDailyMaxStreak()) {
            stats.setDailyMaxStreak(stats.getDailyCurrentStreak());
        }

        stats.setDailyLastCompletedDate(date);
    }

    private void recalculateStreak(MemberStatistics stats, Integer memberId) {
        List<DailyCompletionLog> recentLogs = dailyCompletionLogRepository
                .findByMemberIdOrderByCompletedDateDesc(memberId);

        if (recentLogs.isEmpty()) {
            stats.setDailyCurrentStreak(0);
            stats.setDailyLastCompletedDate(null);
            return;
        }

        LocalDate lastDate = recentLogs.get(0).getCompletedDate();
        int streak = 1;

        for (int i = 1; i < recentLogs.size(); i++) {
            LocalDate prevDate = recentLogs.get(i).getCompletedDate();
            if (lastDate.equals(prevDate.plusDays(1))) {
                streak++;
                lastDate = prevDate;
            } else {
                break;
            }
        }

        stats.setDailyCurrentStreak(streak);
        stats.setDailyLastCompletedDate(recentLogs.get(0).getCompletedDate());
    }

    private MemberStatistics getOrCreate(Integer memberId) {
        return memberStatisticsRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    MemberStatistics newStats = MemberStatistics.builder()
                            .memberId(memberId)
                            .build();
                    return memberStatisticsRepository.save(newStats);
                });
    }
}