package com.back.domain.mission.service;

import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.entitiy.SubGoal;
import com.back.domain.mission.entitiy.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MissionCalculateService {

    //시작일 계산
    public LocalDate calculateStartDate() {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        if(todayDayOfWeek == DayOfWeek.MONDAY){
            return today;
        }

        int daysUntilMonday = DayOfWeek.MONDAY.getValue() - todayDayOfWeek.getValue();
        if(daysUntilMonday <0 ) {
            daysUntilMonday += 7;
        }

        return today.plusDays(daysUntilMonday);
    }


    // 종료일 계산
    public LocalDate calculateEndDate(LocalDate startDate, Integer weeks){
        return startDate.plusWeeks(weeks).minusDays(1);
    }

    //현재 주차 계산
    public Integer calculateCurrentWeek(Mission mission){
        LocalDate today = LocalDate.now();
        if(today.isBefore(mission.getStartDate())){
            return 0; //시작전
        }
        if(today.isAfter(mission.getEndDate())){
            return null; //시작후
        }

        long daysPassed = ChronoUnit.DAYS.between(mission.getStartDate(), today);
        return (int) (daysPassed / 7) + 1;
    }

    public boolean isCurrentWeek(SubGoal subGoal) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(subGoal.getStartDate()) && !today.isAfter(subGoal.getEndDate());
    }

    public boolean isToday(Task task){
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        int todayDayNum = todayDayOfWeek.getValue();

        return task.getDayNum() == todayDayNum && isCurrentWeek(task.getSubGoal());
    }


    //일일 진행률 계산
    public Integer calculateDailyProgress(Integer memberId, LocalDate Date){
        return 0;
    }

    //주간 질행률 계신
    public Integer calculateWeeklyProgress(Integer memberId, Mission mission, LocalDate date){
        return 0;
    }

    //미션 전체 진행률 계산
    public Integer calculateMissionProgress(Mission mission){
        return 0;
    }

    //주차별 진행률 계산
    public Integer calculateWeekProgress(SubGoal subGoal){
        return 0;
    }
}
