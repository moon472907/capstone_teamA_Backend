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
import com.back.domain.mission.event.TaskCompletedEvent;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.SubGoalRepository;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.mission.repository.TaskRepository;
import com.back.domain.party.party.entity.PartyMemberStatus;
import com.back.global.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TimeProvider timeProvider;
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final MissionCalculateService calculateService;
    private final SubGoalRepository subGoalRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CompletionCheckService completionCheckService;

    // 태스크 완료/취소 처리 (체크박스 토글)
    // 당일만 처리 가능
    // 개인 미션: 무제한 토글 가능
    // 파티 미션: 완료 후 취소 불가
    public TaskCompleteResponse completeTask(Integer memberId, TaskCompleteRequest request) {
        // 1. Task 조회
        Task task = findTaskById(request.getTaskId());
        LocalDate today = timeProvider.today();
        Mission mission = task.getSubGoal().getMission();
        SubGoal subGoal = task.getSubGoal();

        // 2. 검증 (요일, 날짜 범위, 권한 등)
        validateTaskCompletion(task, mission, subGoal, today, memberId);

        // 3. 기존 기록 확인
        Optional<TaskLog> existingLog = taskLogRepository
                .findByTaskIdAndMemberIdAndDate(task.getId(), memberId, today);

        TaskLog taskLog;
        TaskStatus finalStatus;

        if (existingLog.isPresent()) {
            // 기존 기록이 있으면 토글
            taskLog = existingLog.get();
            TaskStatus currentStatus = taskLog.getStatus();

            // 토글 로직
            if (currentStatus == TaskStatus.COMPLETED) {
                finalStatus = TaskStatus.CANCELLED;  // 완료 → 취소
            } else {
                finalStatus = TaskStatus.COMPLETED;  // 취소/대기 → 완료
            }

            taskLog.setStatus(finalStatus);
        } else {
            // 새 기록 생성 (첫 체크)
            taskLog = TaskLog.builder()
                    .task(task)
                    .memberId(memberId)
                    .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                    .date(today)
                    .status(TaskStatus.COMPLETED)
                    .build();
            finalStatus = TaskStatus.COMPLETED;
        }

        // 4. 저장
        taskLogRepository.save(taskLog);

        // 5. 이벤트 발행 (COMPLETED일 때만 - 보상 처리용)
        if (finalStatus == TaskStatus.COMPLETED) {
            completionCheckService.checkAllCompletions(memberId, task, today);
        } else if (finalStatus == TaskStatus.CANCELLED) {
            completionCheckService.recheckAfterCancellation(memberId, task, today);
        }

        // 7. 응답 생성
        return buildTaskCompleteResponse(task, mission, memberId, today, finalStatus);
    }

    // 오늘의 태스크 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getTodayTasks(Integer memberId) {
        LocalDate today = timeProvider.today();
        int todayDayNum = today.getDayOfWeek().getValue();

        List<Task> tasks = taskRepository.findTodayTasks(memberId, today, todayDayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponseForToday(task, memberId, today))
                .collect(Collectors.toList());
    }

    // 특정 날짜의 태스크 조회
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByDate(Integer memberId, LocalDate date) {
        int dayNum = date.getDayOfWeek().getValue();
        List<Task> tasks = taskRepository.findTasksByDate(memberId, date, dayNum);

        return tasks.stream()
                .map(task -> convertToTaskResponse(task, memberId, date))
                .collect(Collectors.toList());
    }

    // 태스크 제목 수정
    public TaskResponse updateTask(Integer memberId, Integer taskId, String newTitle) {
        Task task = findTaskById(taskId);
        validateTaskOwnership(task, memberId);

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new MissionException(MissionErrorCode.TASK_TITLE_REQUIRED);
        }

        // Task 엔티티 내부에서 수정 가능 여부 검증
        task.updateContent(newTitle);

        return toTaskResponse(task, memberId, timeProvider.today());
    }

    // 주차별 태스크 일괄 수정
    public List<TaskResponse> updateWeekTasks(Integer memberId, WeekTaskUpdateRequest request) {
        SubGoal subGoal = findSubGoalById(request.getSubGoalId());
        validateTaskOwnership(subGoal.getMission(), memberId);

        List<TaskResponse> responses = new ArrayList<>();

        for (WeekTaskUpdateRequest.TaskUpdateDto taskDto : request.getTasks()) {
            Task task = findTaskById(taskDto.getTaskId());

            // SubGoal 일치 확인
            if (!task.getSubGoal().getId().equals(request.getSubGoalId())) {
                throw new MissionException(MissionErrorCode.TASK_NOT_IN_SUBGOAL);
            }

            task.updateContent(taskDto.getTitle());
            responses.add(toTaskResponse(task, memberId, timeProvider.today()));
        }

        return responses;
    }

    private Task findTaskById(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));
    }

    private SubGoal findSubGoalById(Integer subGoalId) {
        return subGoalRepository.findById(subGoalId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.SUBGOAL_NOT_FOUND));
    }


    private void validateTaskCompletion(Task task, Mission mission, SubGoal subGoal,
                                        LocalDate completedDate, Integer memberId) {
        // 요일 체크
        int completedDayOfWeek = completedDate.getDayOfWeek().getValue();
        if (task.getDayNum() != completedDayOfWeek) {
            throw new MissionException(MissionErrorCode.TASK_WRONG_DAY);
        }

        // 미션 시작일 체크
        if (completedDate.isBefore(mission.getStartDate())) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_STARTED);
        }

        // 미션 종료일 체크
        if (completedDate.isAfter(mission.getEndDate())) {
            throw new MissionException(MissionErrorCode.MISSION_ALREADY_ENDED);
        }

        // 주차 범위 체크
        if (completedDate.isBefore(subGoal.getStartDate()) ||
                completedDate.isAfter(subGoal.getEndDate())) {
            throw new MissionException(MissionErrorCode.TASK_NOT_IN_DATE_RANGE);
        }

        // 권한 체크
        validateTaskOwnership(mission, memberId);
    }

    // 태스크 소유권 검증+
    private void validateTaskOwnership(Task task, Integer memberId) {
        validateTaskOwnership(task.getSubGoal().getMission(), memberId);
    }

    // 미션 접근 권한 검증
    private void validateTaskOwnership(Mission mission, Integer memberId) {
        // 개인 미션
        if (!mission.isPartyMission()) {
            if (!mission.getMember().getId().equals(memberId)) {
                throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
            }
            return;
        }

        // 파티 미션: ACCEPTED 상태 확인
        boolean isAcceptedMember = mission.getParty().getPartyMembers().stream()
                .anyMatch(pm -> pm.getMember().getId().equals(memberId)
                        && pm.getStatus() == PartyMemberStatus.ACCEPTED);

        if (!isAcceptedMember) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }
    }

    // 태스크 완료 이벤트 발행 (보상 처리용)
    private void publishTaskCompletedEvent(Integer memberId, Task task, Mission mission,
                                           SubGoal subGoal, LocalDate completedDate, TaskStatus status) {
        eventPublisher.publishEvent(TaskCompletedEvent.builder()
                .memberId(memberId)
                .taskId(task.getId())
                .missionId(mission.getId())
                .subGoalId(subGoal.getId())
                .completedDate(completedDate)
                .status(status)
                .build());
    }

    // 태스크 완료 응답 생성
    private TaskCompleteResponse buildTaskCompleteResponse(Task task, Mission mission,
                                                           Integer memberId, LocalDate completedDate,
                                                           TaskStatus status) {
        return TaskCompleteResponse.builder()
                .taskId(task.getId())
                .status(status)
                .completedDate(completedDate)
                .dailyProgressRate(calculateService.calculateDailyProgress(memberId, completedDate))
                .weeklyProgressRate(calculateService.calculateWeeklyProgress(memberId, mission, completedDate))
                .missionProgressRate(calculateService.calculateMissionProgressForMember(mission, memberId))
                .build();
    }

    // 오늘의 태스크 전용 변환 (추가 정보 포함)
    private TaskResponse convertToTaskResponseForToday(Task task, Integer memberId, LocalDate date) {
        TaskResponse response = convertToTaskResponse(task, memberId, date);

        Mission mission = task.getSubGoal().getMission();
        SubGoal subGoal = task.getSubGoal();

        // 미션 제목, 주차 제목 추가
        response.setMissionTitle(mission.getTitle());
        response.setSubGoalTitle(subGoal.getTitle());

        // 파티 미션이면 완료 정보 추가
        if (mission.isPartyMission()) {
            response.setPartyCompletion(calculateService.calculateTaskCompletion(task, date));
        }

        return response;
    }

    // TaskResponse 변환 (기본)
    @Transactional(readOnly = true)
    public TaskResponse toTaskResponse(Task task, Integer memberId, LocalDate date) {
        return convertToTaskResponse(task, memberId, date);
    }

    // Task → TaskResponse 변환
    private TaskResponse convertToTaskResponse(Task task, Integer memberId, LocalDate date) {
        // 해당 날짜의 TaskLog 조회
        Optional<TaskLog> taskLog = taskLogRepository.findByTaskIdAndMemberIdAndDate(
                task.getId(), memberId, date);

        // TaskLog가 없으면 PENDING, 있으면 실제 status
        TaskStatus status = taskLog.map(TaskLog::getStatus).orElse(TaskStatus.PENDING);

        // 마지막 완료 날짜 (COMPLETED 상태만)
        LocalDate lastCompletedDate = taskLogRepository
                .findTopByTaskIdAndMemberIdOrderByDateDesc(task.getId(), memberId)
                .filter(log -> log.getStatus() == TaskStatus.COMPLETED)
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

    // 배치 변환 (N+1 방지)
    @Transactional(readOnly = true)
    public List<TaskResponse> toTaskResponsesBatch(List<Task> tasks, Integer memberId, LocalDate date) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        // Task ID 목록
        List<Integer> taskIds = tasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        // 한 번에 조회 (N+1 방지)
        List<TaskLog> currentLogs = taskLogRepository
                .findByTaskIdsAndMemberIdAndDate(taskIds, memberId, date);
        List<TaskLog> lastLogs = taskLogRepository
                .findLastCompletedByTaskIds(taskIds, memberId);

        // Map으로 변환
        Map<Integer, TaskLog> currentLogMap = currentLogs.stream()
                .collect(Collectors.toMap(
                        tl -> tl.getTask().getId(),
                        tl -> tl,
                        (existing, replacement) -> existing
                ));

        Map<Integer, TaskLog> lastLogMap = lastLogs.stream()
                .filter(tl -> tl.getStatus() == TaskStatus.COMPLETED)
                .collect(Collectors.toMap(
                        tl -> tl.getTask().getId(),
                        tl -> tl,
                        (existing, replacement) -> existing
                ));

        // 변환
        return tasks.stream()
                .map(task -> convertToTaskResponseWithMaps(
                        task, memberId, date, currentLogMap, lastLogMap
                ))
                .collect(Collectors.toList());
    }

    // Map을 사용한 TaskResponse 변환 (N+1 방지)
    private TaskResponse convertToTaskResponseWithMaps(
            Task task, Integer memberId, LocalDate date,
            Map<Integer, TaskLog> currentLogMap,
            Map<Integer, TaskLog> lastLogMap) {

        TaskLog currentLog = currentLogMap.get(task.getId());
        TaskLog lastLog = lastLogMap.get(task.getId());

        TaskStatus status = currentLog != null ?
                currentLog.getStatus() : TaskStatus.PENDING;
        LocalDate lastCompletedDate = lastLog != null ?
                lastLog.getDate() : null;

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