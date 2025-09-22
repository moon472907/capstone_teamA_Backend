package com.back.domain.mission.service;

import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.dto.ai.DailyTask;
import com.back.domain.mission.dto.ai.WeeklyPlan;
import com.back.domain.mission.enums.MissionCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
// AI 목업 서비스
public class AiMissionGeneratorService {
   // 미션 자동 생성 진입 메서드
    public AiMissionResult generateMission(String title, Integer weeks, Integer memberId) {
        MissionCategory category = determineCategoryByTitle(title);

        List<WeeklyPlan> weeklyPlans = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            WeeklyPlan weekPlan = WeeklyPlan.builder()
                    .weekNum(week)
                    .title(week + "주차: " + title)
                    .dailyTasks(generateBasicDailyTasks(title, week))
                    .build();
            weeklyPlans.add(weekPlan);
        }

        return AiMissionResult.builder()
                .category(category)
                .weeklyPlans(weeklyPlans)
                .build();
    }

    //키워드 분석
    private MissionCategory determineCategoryByTitle(String title) {
        String titleLower = title.toLowerCase();

        if (titleLower.contains("운동") || titleLower.contains("헬스") || titleLower.contains("달리기")) {
            return MissionCategory.EXERCISE;
        } else if (titleLower.contains("공부") || titleLower.contains("토익") || titleLower.contains("학습")) {
            return MissionCategory.LEARNING;
        } else if (titleLower.contains("습관") || titleLower.contains("금연") || titleLower.contains("다이어트")) {
            return MissionCategory.HABIT;
        } else if (titleLower.contains("명상") || titleLower.contains("스트레스") || titleLower.contains("멘탈")) {
            return MissionCategory.MENTAL;
        }

        return MissionCategory.CUSTOM;
    }

    //기본 task 생성
    private List<DailyTask> generateBasicDailyTasks(String title, int week) {
        List<DailyTask> dailyTasks = new ArrayList<>();

        for (int day = 1; day <= 7; day++) {
            DailyTask task = DailyTask.builder()
                    .dayNum(day)
                    .title(title + " - " + getDayName(day) + " 활동")
                    .build();
            dailyTasks.add(task);
        }

        return dailyTasks;
    }

    private String getDayName(int dayNum) {
        String[] days = {"", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};
        return days[dayNum];
    }
}