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

    public LocalDate calculateEndDate(LocalDate startDate, Integer weeks) {
        return startDate.plusWeeks(weeks).minusDays(1);
    }

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

    public boolean isToday(Task task) {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        int todayDayNum = todayDayOfWeek.getValue();

        SubGoal subGoal = task.getSubGoal();
        return task.getDayNum() == todayDayNum &&
                !today.isBefore(subGoal.getStartDate()) &&
                !today.isAfter(subGoal.getEndDate());
    }

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

    public Integer calculateWeekProgress(SubGoal subGoal) {
        if (subGoal.getTasks().isEmpty()) return 0;

        long totalTasks = subGoal.getTasks().size();
        long completedTasks = taskLogRepository.countCompletedTasksBySubGoal(
                subGoal.getId(), TaskStatus.COMPLETED);

        return (int) (completedTasks * 100 / totalTasks);
    }
}