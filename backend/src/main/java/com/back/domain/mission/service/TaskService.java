package com.back.domain.mission.service;

import com.back.domain.mission.dto.request.TaskCompleteRequest;
import com.back.domain.mission.dto.response.TaskCompleteResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.entity.*;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final MissionRepository missionRepository;
    private final MissionCalculateService calculateService;

    public TaskCompleteResponse completeTask(Integer memberId, TaskCompleteRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

        LocalDate completedDate = request.getDate() != null ? request.getDate() : LocalDate.now();

        if (taskLogRepository.existsByTaskIdAndMemberIdAndDate(
                request.getTaskId(), memberId, completedDate)) {
            throw new MissionException(MissionErrorCode.TASK_ALREADY_COMPLETED);
        }

        Mission mission = task.getSubGoal().getMission();

        TaskLog taskLog = TaskLog.builder()
                .task(task)
                .memberId(memberId)
                .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                .date(completedDate)
                .status(request.getStatus())
                .build();

        taskLogRepository.save(taskLog);

        return TaskCompleteResponse.builder()
                .taskId(task.getId())
                .status(request.getStatus())
                .completedDate(completedDate)
                .earnedPoints(request.getStatus() == TaskStatus.COMPLETED ? 10 : 0)
                .earnedExp(request.getStatus() == TaskStatus.COMPLETED ? 5 : 0)
                .dailyProgressRate(calculateService.calculateDailyProgress(memberId, completedDate))
                .weeklyProgressRate(calculateService.calculateWeeklyProgress(memberId, mission, completedDate))
                .missionProgressRate(calculateService.calculateMissionProgress(mission))
                .build();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(Integer memberId) {
        LocalDate today = LocalDate.now();
        int todayDayNum = today.getDayOfWeek().getValue();

        List<Task> tasks = taskRepository.findTodayTasks(memberId, today, todayDayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDate(Integer memberId, LocalDate date) {
        int dayNum = date.getDayOfWeek().getValue();

        List<Task> tasks = taskRepository.findTasksByDate(memberId, date, dayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, date))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getWeekTasks(Integer memberId, Integer missionId, Integer weekNum) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        SubGoal subGoal = mission.getSubGoals().stream()
                .filter(sg -> sg.getOrderNum().equals(weekNum))
                .findFirst()
                .orElseThrow(() -> new MissionException(MissionErrorCode.SUBGOAL_NOT_FOUND));

        return subGoal.getTasks().stream()
                .map(task -> convertToTaskResponse(task, memberId, LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse toTaskResponse(Task task, Integer memberId, LocalDate date) {
        return convertToTaskResponse(task, memberId, date);
    }


    private TaskResponse convertToTaskResponse(Task task, Integer memberId, LocalDate date) {
        Optional<TaskLog> taskLog = taskLogRepository.findByTaskIdAndMemberIdAndDate(
                task.getId(), memberId, date);

        TaskStatus status = taskLog.map(TaskLog::getStatus).orElse(TaskStatus.PENDING);

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
                .isToday(calculateService.isToday(task))
                .build();
    }
}