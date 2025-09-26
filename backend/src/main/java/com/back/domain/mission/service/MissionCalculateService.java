package com.back.domain.mission.service;

import com.back.domain.mission.entity.*;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.TaskLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MissionCalculateService {

    private final TaskLogRepository taskLogRepository;
    //시작일 계산 ( 오늘이 월요일 = 당일 시작 ) 아니면 무조건 다음주 월
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

    // 종료일 계산
    public LocalDate calculateEndDate(LocalDate startDate, Integer weeks) {
        return startDate.plusWeeks(weeks).minusDays(1);
    }

    // 특정 날짜 기준으로 현재 주차 계산 ( 기준일이 시작 전이면 0, 종료일 이후면 null)
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

    // 주어진 task가 오늘 해야 하는 일인지 판단
    public boolean isToday(Task task) {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        int todayDayNum = todayDayOfWeek.getValue();

        SubGoal subGoal = task.getSubGoal();
        return task.getDayNum() == todayDayNum &&
                !today.isBefore(subGoal.getStartDate()) &&
                !today.isAfter(subGoal.getEndDate());
    }

    // 하루 진행률(개인)
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

    // 주간 진행률(개인)
    public Integer calculateWeeklyProgress(Integer memberId, Mission mission, LocalDate date) {
        SubGoal currentSubGoal = mission.getSubGoals().stream()
                .filter(sg -> !date.isBefore(sg.getStartDate()) && !date.isAfter(sg.getEndDate()))
                .findFirst()
                .orElse(null);

        if (currentSubGoal == null) {
            return 0;
        }

        return calculateWeekProgress(currentSubGoal);
    }

    //미션 전체 진행률
    public Integer calculateMissionProgress(Mission mission) {
        if (mission.getSubGoals().isEmpty()) {
            return 0;
        }

        long totalTasks = mission.getSubGoals().stream()
                .mapToLong(sg -> sg.getTasks().size())
                .sum();

        if (totalTasks == 0) return 0;

        long completedTasks = taskLogRepository.countCompletedTasksByMission(
                mission.getId(), TaskStatus.COMPLETED);

        return (int) Math.min(completedTasks * 100 / totalTasks, 100);
    }

    // 특정 주차 진행률
    public Integer calculateWeekProgress(SubGoal subGoal) {
        if (subGoal.getTasks().isEmpty()) return 0;

        long totalTasks = subGoal.getTasks().size();
        long completedTasks = taskLogRepository.countCompletedTasksBySubGoal(
                subGoal.getId(), TaskStatus.COMPLETED);

        return (int) (completedTasks * 100 / totalTasks);
    }


}