package com.back.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;

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


}
