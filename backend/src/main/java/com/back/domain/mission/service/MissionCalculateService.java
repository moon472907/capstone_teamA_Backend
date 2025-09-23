package com.back.domain.mission.service;

import com.back.domain.mission.entitiy.Mission;
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
            return null;
        }

        long daysPassed = ChronoUnit.DAYS.between(mission.getStartDate(), today);
        return (int) (daysPassed / 7) + 1;
    }
}
