package com.back.domain.mission.service;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.SubGoal;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionCalculateService {

    private final TaskLogRepository taskLogRepository;

    // 날짜 계산
    // 시작 날짜 계산
    public LocalDate calculateStartDate() {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        if (todayDayOfWeek == DayOfWeek.MONDAY) {
            return today;
        }

        int daysUntilMonday = DayOfWeek.MONDAY.getValue() - todayDayOfWeek.getValue();
        if (daysUntilMonday < 0) {
            daysUntilMonday += 7;
        }

        return today.plusDays(daysUntilMonday);
    }

    // 종료일 계산(금)
    public LocalDate calculateEndDate(LocalDate startDate, Integer weeks) {
        return startDate.plusWeeks(weeks).minusDays(1);
    }

    //현재 몇 주차인지 계산( 시작 전 0, 후 null)
    public Integer calculateCurrentWeek(Mission mission) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(mission.getStartDate())) {
            return 0;
        }
        if (today.isAfter(mission.getEndDate())) {
            return null;
        }

        long daysPassed = ChronoUnit.DAYS.between(mission.getStartDate(), today);
        return (int) (daysPassed / 7) + 1;
    }

    // 특정 날짜 기준 몇주차인지 계산
    public int getCurrentWeekNumber(Mission mission, LocalDate date) {
        if (date.isBefore(mission.getStartDate())) return 0;
        if (date.isAfter(mission.getEndDate())) return mission.getSubGoals().size();

        long daysPassed = ChronoUnit.DAYS.between(mission.getStartDate(), date);
        return (int) (daysPassed / 7) + 1;
    }

    // 오늘 할 일인지 여부
    public boolean isToday(Task task) {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        int todayDayNum = todayDayOfWeek.getValue();

        SubGoal subGoal = task.getSubGoal();
        return task.getDayNum() == todayDayNum &&
                !today.isBefore(subGoal.getStartDate()) &&
                !today.isAfter(subGoal.getEndDate());
    }

    // 개인 미션 진행률
    // 특정 멤버가 특정 날짜에 해야할 테스크 중 완료 비율 계산
    public Integer calculateDailyProgress(Integer memberId, LocalDate date) {
        int dayOfWeek = date.getDayOfWeek().getValue();

        Long totalTasks = taskLogRepository.countDailyTasks(memberId, date, dayOfWeek);
        if (totalTasks == 0) {
            return 0;
        }

        Long completedTasks = taskLogRepository.countByMemberIdAndDateAndStatus(
                memberId, date, TaskStatus.COMPLETED);

        return (int) (completedTasks * 100 / totalTasks);
    }

    // 특정 날짜가 속한 주차 찾아서 그 주차에서 멤버의 진행률 계산
    public Integer calculateWeeklyProgress(Integer memberId, Mission mission, LocalDate date) {
        SubGoal currentSubGoal = mission.getSubGoals().stream()
                .filter(sg -> !date.isBefore(sg.getStartDate()) && !date.isAfter(sg.getEndDate()))
                .findFirst()
                .orElse(null);

        if (currentSubGoal == null) {
            return 0;
        }

        return calculateWeekProgressForMember(currentSubGoal, memberId);
    }

    // 미션 전체 테스크 기준으로 멤버의 진행률 계산
    public Integer calculateMissionProgressForMember(Mission mission, Integer memberId) {
        if (mission.getSubGoals().isEmpty()) {
            return 0;
        }

        long totalTasks = mission.getSubGoals().stream()
                .mapToLong(sg -> sg.getTasks().size())
                .sum();

        if (totalTasks == 0) return 0;

        long completedTasks = taskLogRepository.countCompletedTasksByMissionAndMember(
                mission.getId(), memberId, TaskStatus.COMPLETED);

        return (int) Math.min(completedTasks * 100 / totalTasks, 100);
    }

    // 특정 주차에서 멤버의 진행률 계산
    public Integer calculateWeekProgressForMember(SubGoal subGoal, Integer memberId) {
        if (subGoal.getTasks().isEmpty()) return 0;

        long totalTasks = subGoal.getTasks().size();
        long completedTasks = taskLogRepository.countCompletedTasksBySubGoalAndMember(
                subGoal.getId(), memberId, TaskStatus.COMPLETED);

        return (int) (completedTasks * 100 / totalTasks);
    }

    //  파티 미션 진행률
    // 속한 파티 미션에서 내 개인 진행률 계산
    public Integer calculatePartyMissionProgressForMe(Mission mission, Integer memberId) {
        return calculateMissionProgressForMember(mission, memberId);
    }

    // 파티에 참여한 모든 멤버들의 평균 진행률 계산 ((ACCEPTED 상태의 멤버만)
    public Integer calculatePartyMissionProgressAverage(Mission mission) {
        if (mission.getParty() == null) return 0;

        List<PartyMember> activeMembers = mission.getParty().getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .toList();

        if (activeMembers.isEmpty()) return 0;

        long totalTasks = mission.getSubGoals().stream()
                .mapToLong(sg -> sg.getTasks().size())
                .sum();

        if (totalTasks == 0) return 0;

        double totalProgress = 0;
        for (PartyMember pm : activeMembers) {
            long completedTasks = taskLogRepository.countCompletedTasksByMissionAndMember(
                    mission.getId(), pm.getMember().getId(), TaskStatus.COMPLETED);
            double memberProgress = (double) completedTasks / totalTasks;
            totalProgress += memberProgress;
        }

        return (int) Math.min((totalProgress / activeMembers.size()) * 100, 100);
    }

    // 파티 전체 테스크 기준으로 완료된 비율 계산
    public Integer calculatePartyMissionProgressTotal(Mission mission) {
        if (mission.getSubGoals().isEmpty()) return 0;

        long totalTasks = mission.getSubGoals().stream()
                .mapToLong(sg -> sg.getTasks().size())
                .sum();

        if (totalTasks == 0) return 0;

        long completedTasksCount = mission.getSubGoals().stream()
                .flatMap(sg -> sg.getTasks().stream())
                .filter(task -> taskLogRepository.existsByTaskIdAndStatus(
                        task.getId(), TaskStatus.COMPLETED))
                .count();

        return (int) Math.min(completedTasksCount * 100 / totalTasks, 100);
    }

    public boolean calculateVisible(Mission mission, int weekNum, int currentWeek, LocalDate today) {
        if (today.isBefore(mission.getStartDate())) {
            return weekNum <= 2;
        }
        return weekNum <= currentWeek + 1;
    }

    public Integer calculatePartyWeekAverage(SubGoal subGoal) {
        Mission mission = subGoal.getMission();
        if (!mission.isPartyMission()) return 0;

        List<PartyMember> activeMembers = mission.getParty().getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .toList();

        if (activeMembers.isEmpty()) return 0;

        double totalProgress = activeMembers.stream()
                .mapToDouble(pm -> calculateWeekProgressForMember(subGoal, pm.getMember().getId()))
                .sum();

        return (int) (totalProgress / activeMembers.size());
    }
}