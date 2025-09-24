package com.back.domain.mission.service;

import com.back.domain.mission.dto.request.TaskCompleteRequest;
import com.back.domain.mission.dto.response.TaskCompleteResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.entitiy.Task;
import com.back.domain.mission.entitiy.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.mission.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final MissionCalculateService missionCalculateService;
    private final MissionRepository missionRepository;

    // 테스트 완료 처리
    public TaskCompleteResponse completeTask(Integer memberId, TaskCompleteRequest request){
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

        LocalDate completedDate = request.getDate() != null ? request.getDate() : LocalDate.now();

        //중복 완료 방지
        validateTaskNotCompleted(request.getTaskId(), memberId, completedDate);

        // tasklog 생성
        TaskLog taskLog = createTaskLog(task, memberId, completedDate, request.getStatus());
        taskLogRepository.save(taskLog);

        //진행률 계산
        Mission mission = task.getSubGoal().getMission();

        return TaskCompleteResponse.builder()
                .taskId(task.getId())
                .status(request.getStatus())
                .completedDate(completedDate)
                .earnedPoints(calculateEarnedPoints(request.getStatus()))
                .earnedExp(calculateEarnedExp(request.getStatus()))
                .dailyProgressRate(missionCalculateService.calculateDailyProgress(memberId, completedDate))
                .weeklyProgressRate(missionCalculateService.calculateWeeklyProgress(memberId, mission, completedDate))
                .missionProgressRate(missionCalculateService.calculateMissionProgress(mission))
                .build();

    }


    //사용자의 오늘 테스크 목록 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(Integer memberId) {
        LocalDate today = LocalDate.now();
        int todayDayNum = today.getDayOfWeek().getValue();

        // 오늘에 해당하는 모든 태스크 조회
        List<Task> todayTasks = taskRepository.findTodayTasks(memberId, today, todayDayNum);

        return todayTasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, today))
                .collect(Collectors.toList());
    }

    //특정 날짜의 태스크 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDate(Integer memberId, LocalDate date) {
        int dayNum = date.getDayOfWeek().getValue();

        // 해당 날짜에 속하는 태스크 조회
        List<Task> tasks = taskRepository.findTasksByDate(memberId, date, dayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, date))
                .collect(Collectors.toList());
    }

    //특정 미션의 특정 주차 태스크 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getWeekTasks(Integer memberId, Integer missionId, Integer weekNum) {
        // 미션 권한 체크
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        // 해당 주차의 SubGoal 찾기
        return mission.getSubGoals().stream()
                .filter(sg -> sg.getOrderNum().equals(weekNum))
                .findFirst()
                .map(subGoal -> subGoal.getTasks().stream()
                        .map(task -> convertToTaskResponse(task, memberId, LocalDate.now()))
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());
    }




    private void validateTaskNotCompleted(Integer taskId, Integer memberId, LocalDate date) {
        boolean exists = taskLogRepository.existsByTaskIdAndMemberIdAndDate(taskId, memberId, date);
        if (exists) {
            throw new MissionException(MissionErrorCode.TASK_ALREADY_COMPLETED);
        }
    }

    private TaskLog createTaskLog(Task task, Integer memberId, LocalDate date, TaskStatus status) {
        return TaskLog.builder()
                .task(task)
                .memberId(memberId)
                .date(date)
                .status(status)
                .build();
    }


    private TaskResponse convertToTaskResponse(Task task, Integer memberId, LocalDate date) {
        // 해당 날짜의 TaskLog 조회
        Optional<TaskLog> taskLog = taskLogRepository.findByTaskIdAndMemberIdAndDate(
                task.getId(), memberId, date);

        TaskStatus status = taskLog
                .map(TaskLog::getStatus)
                .orElse(TaskStatus.PENDING);

        LocalDate lastCompletedDate = taskLogRepository
                .findTopByTaskIdAndMemberIdOrderByDateDesc(task.getId(), memberId)
                .map(TaskLog::getDate)
                .orElse(null);

        return TaskResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .dayNum(task.getDayNum())
                .status(status)
                .lastCompletedDate(lastCompletedDate)
                .isToday(missionCalculateService.isToday(task))
                .build();
    }

    // 보상관련 로직 - > 나중에 다른 곳으로 옮겨야할듯.
    private Integer calculateEarnedPoints(TaskStatus status) {
        return switch (status) {
            case COMPLETED -> 10;
            case SKIPPED -> 0;
            default -> 0;
        };
    }

    private Integer calculateEarnedExp(TaskStatus status) {
        return switch (status) {
            case COMPLETED -> 5;
            case SKIPPED -> 0;
            default -> 0;
        };
    }
}
