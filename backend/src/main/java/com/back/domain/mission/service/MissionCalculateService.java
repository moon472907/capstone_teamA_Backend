package com.back.domain.mission.service;

import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.entitiy.SubGoal;
import com.back.domain.mission.entitiy.Task;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.mission.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class MissionCalculateService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;

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

    // 현재 진행 주차인지 확인
    public boolean isCurrentWeek(SubGoal subGoal) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(subGoal.getStartDate()) && !today.isAfter(subGoal.getEndDate());
    }


    //오늘의 task인지 확인
    public boolean isToday(Task task){
        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        int todayDayNum = todayDayOfWeek.getValue();

        return task.getDayNum() == todayDayNum && isCurrentWeek(task.getSubGoal());
    }


    //일일 진행률 계산 - 특정 날짜의 완료율
    public Integer calculateDailyProgress(Integer memberId, LocalDate date) {
        //해당 날짜의 요일 확인
        int dayOfWweek = date.getDayOfWeek().getValue();

        //해당 날짜에 수행해야 할 테스크 개수
        Long totalTasks = taskLogRepository.countDailyTasks(memberId, date, dayOfWweek);

        if(totalTasks == 0){
            return 0;
        }

        //완료된 테스크 수
       Long completedTasks = taskLogRepository.countByMemberIdAndDateAndStatus(memberId, date, TaskStatus.COMPLETED);
        return (int) (completedTasks * 100 / totalTasks);
    }


    //주간 질행률 계신
    public Integer calculateWeeklyProgress(Integer memberId, Mission mission, LocalDate date){
       // 해당 날ㅉ짜가 속한 주차의 SubGoal 찾기
        SubGoal currentSubGoal = mission.getSubGoals().stream()
                .filter(sg-> !date.isBefore(sg.getStartDate()) && !date.isAfter(sg.getEndDate()))
                .findFirst()
                .orElse(null);

        if (currentSubGoal == null){
            return 0;
        }

        return calculateWeekProgress(currentSubGoal);
    }

    //미션 전체 진행률 계산
    public Integer calculateMissionProgress(Mission mission){
        if (mission.getSubGoals().isEmpty()){
            return 0;
        }

        //전체 테크수 계산
        long totalTasks = mission.getSubGoals().stream()
                .mapToLong(sg -> sg.getTasks().size())
                .sum();

        if(totalTasks == 0) return 0;

        //완료된 테스크 수 계산
        long completedTasks = taskLogRepository.countCompletedTasksByMission(mission.getId(), TaskStatus.COMPLETED);
        int progress = (int) (completedTasks * 100 / totalTasks);

        // 100% 완료 시 미션 완료 처리
        if (progress >= 100 && !mission.isCompleted()) mission.setCompleted(true);

        return Math.min(progress, 100); //100을 않도록
    }

    //주차별 진행률 계산
    public Integer calculateWeekProgress(SubGoal subGoal){
        if (subGoal.getTasks().isEmpty()) return 0;

        long totalTasks = subGoal.getTasks().size();

        //현재 주차의 와뇰된 테스크 수
        long completedTasks = taskLogRepository.countCompletedTasksBySubGoal(subGoal.getId(), TaskStatus.COMPLETED);

        return (int) (completedTasks * 100 / totalTasks);
    }
}
