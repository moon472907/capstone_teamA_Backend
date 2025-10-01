package com.back.domain.mission.service;

import com.back.domain.mission.dto.request.TaskCompleteRequest;
import com.back.domain.mission.dto.request.WeekTaskUpdateRequest;
import com.back.domain.mission.dto.response.TaskCompleteResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.SubGoal;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.entity.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.SubGoalRepository;
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
    private final MissionRepository missionRepository;
    private final MissionCalculateService calculateService;
    private final SubGoalRepository subGoalRepository;

    // 특정 task 완료 처리
    public TaskCompleteResponse completeTask(Integer memberId, TaskCompleteRequest request) {
        //task 찾기
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

        // 완료 날짜 ( 없을 시 오늘 )
        LocalDate completedDate = request.getDate() != null ? request.getDate() : LocalDate.now();

        // 이미 완료한 기록이 있다면 예외처리
        if (taskLogRepository.existsByTaskIdAndMemberIdAndDate(
                request.getTaskId(), memberId, completedDate)) {
            throw new MissionException(MissionErrorCode.TASK_ALREADY_COMPLETED);
        }

        Mission mission = task.getSubGoal().getMission();

        //tasklog에 기록처리
        TaskLog taskLog = TaskLog.builder()
                .task(task)
                .memberId(memberId)
                .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                .date(completedDate)
                .status(request.getStatus())
                .build();

        taskLogRepository.save(taskLog);

        // 완료 응답 반환 ( 포인트/경험치 + 진행률 포함 ---> TODO : reward 완료되면 수정해야함 !!  )
        return TaskCompleteResponse.builder()
                .taskId(task.getId())
                .status(request.getStatus())
                .completedDate(completedDate)
                .earnedPoints(request.getStatus() == TaskStatus.COMPLETED ? 10 : 0)
                .earnedExp(request.getStatus() == TaskStatus.COMPLETED ? 5 : 0)
                .dailyProgressRate(calculateService.calculateDailyProgress(memberId, completedDate))
                .weeklyProgressRate(calculateService.calculateWeeklyProgress(memberId, mission, completedDate))
                .missionProgressRate(calculateService.calculateMissionProgressForMember(mission, memberId))
                .build();
    }

    // 멤버 id 기준 오늘 할 일 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(Integer memberId) {
        LocalDate today = LocalDate.now();
        int todayDayNum = today.getDayOfWeek().getValue();

        List<Task> tasks = taskRepository.findTodayTasks(memberId, today, todayDayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, today))
                .collect(Collectors.toList());
    }

    //특정 날짜에 해당하는 task
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDate(Integer memberId, LocalDate date) {
        int dayNum = date.getDayOfWeek().getValue();

        List<Task> tasks = taskRepository.findTasksByDate(memberId, date, dayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, date))
                .collect(Collectors.toList());
    }

    // 특정 미션의 특정 주차에 해당하는 task 조회
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

    // task 수정
    public TaskResponse updateTask(Integer memberId, Integer taskId, String newTitle) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

        Mission mission = task.getSubGoal().getMission();
        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        // Task 내부 로직 사용
        task.updateContent(newTitle);

        return toTaskResponse(task, memberId, LocalDate.now());
    }

    public List<TaskResponse> updateWeekTasks(Integer memberId, WeekTaskUpdateRequest request) {
        // SubGoal 조회 및 권한 확인
        SubGoal subGoal = subGoalRepository.findById(request.getSubGoalId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.SUBGOAL_NOT_FOUND));

        Mission mission = subGoal.getMission();
        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        List<TaskResponse> responses = new ArrayList<>();

        for (WeekTaskUpdateRequest.TaskUpdateDto taskDto : request.getTasks()) {
            Task task = taskRepository.findById(taskDto.getTaskId())
                    .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

            // SubGoal 일치 확인
            if (!task.getSubGoal().getId().equals(request.getSubGoalId())) {
                throw new MissionException(MissionErrorCode.TASK_NOT_IN_SUBGOAL);
            }

            // Task 수정 (canEdit 체크 포함)
            task.updateContent(taskDto.getTitle());

            responses.add(toTaskResponse(task, memberId, LocalDate.now()));
        }

        return responses;
    }


    // task 엔티티 -> taskResponse DTO 변환

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
                .hasBeenEdited(task.getHasBeenEdited())
                .canEdit(task.canEdit())
                .editDeadline(task.getEditDeadline())
                .build();
    }
}