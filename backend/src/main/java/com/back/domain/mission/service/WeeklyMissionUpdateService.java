package com.back.domain.mission.service;

import com.back.domain.mission.dto.request.WeeklyUpdateRequest;
import com.back.domain.mission.dto.response.SubGoalResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.SubGoal;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.SubGoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WeeklyMissionUpdateService {

    private final MissionRepository missionRepository;
    private final SubGoalRepository subGoalRepository;
    private final MissionCalculateService missionCalculateService;

    // 현재 수정 가능한 주차 목록 조회
    @Transactional(readOnly = true)
    public List<SubGoalResponse> getEditableWeeks(Integer memberId, Integer missionId) {
        Mission mission = validateMissionAccess(memberId, missionId);
        LocalDate today = LocalDate.now();

        // 현재 주차와 다음 주차만 수정 가능
        List<SubGoal> editableSubGoals = mission.getSubGoals().stream()
                .filter(sg -> isEditableWeek(sg, today))
                .collect(Collectors.toList());

        return editableSubGoals.stream()
                .map(this::convertToSubGoalResponse)
                .collect(Collectors.toList());
    }

    // 특정 주차 수정ㄴ
    public SubGoalResponse updateWeekly(Integer memberId, WeeklyUpdateRequest request) {
        // 1. 권한 확인
        Mission mission = validateMissionAccess(memberId, request.getMissionId());

        // 2. SubGoal 조회
        SubGoal subGoal = subGoalRepository.findById(request.getSubGoalId())
                .orElseThrow(() -> new MissionException(MissionErrorCode.SUBGOAL_NOT_FOUND));

        // 3. 수정 가능 여부 확인
        validateEditability(subGoal);

        // 4. 수정 사항 적용
        applyWeeklyUpdates(subGoal, request);

        // 5. 수정 완료 처리
        subGoal.setHasBeenEdited(true);

        log.info("주차 수정 완료 - subGoalId: {}, weekNum: {}",
                subGoal.getId(), subGoal.getOrderNum());

        return convertToSubGoalResponse(subGoal);
    }

    // 미션 시작 시 수정 가능 기한 설정
    public void initializeEditablePeriods(Integer missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        LocalDate today = LocalDate.now();

        mission.getSubGoals().forEach(subGoal -> {
            // 주차 시작 후 3일까지만 수정 가능
            if (subGoal.getEditableUntil() == null) {
                subGoal.setEditableUntil(subGoal.getStartDate().plusDays(2)); // 3일까지 (0,1,2)
            }
        });

        log.info("미션 수정 기한 초기화 완료 - missionId: {}", missionId);
    }


    private Mission validateMissionAccess(Integer memberId, Integer missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        // 개인 미션이면 멤버 체크, 파티 미션이면 파티 멤버 체크
        if (mission.isPartyMission()) {
            boolean isMember = mission.getParty().getPartyMembers().stream()
                    .anyMatch(pm -> pm.getMember().getId().equals(memberId) &&
                            pm.getStatus().name().equals("ACCEPTED")); // 또는 ACTIVE
            if (!isMember) {
                throw new MissionException(MissionErrorCode.NOT_PARTY_MEMBER);
            }
        } else {
            if (!mission.getMember().getId().equals(memberId)) {
                throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
            }
        }

        return mission;
    }

    private boolean isEditableWeek(SubGoal subGoal, LocalDate today) {
        // 이미 수정한 적이 있으면 수정 불가
        if (Boolean.TRUE.equals(subGoal.getHasBeenEdited())) {
            return false;
        }

        // 시작 전 주차는 수정 가능 (다음 주차)
        if (today.isBefore(subGoal.getStartDate())) {
            // 하지만 너무 먼 미래는 수정 불가 (다음주까지만)
            return today.plusWeeks(2).isAfter(subGoal.getStartDate());
        }

        // 현재 진행중인 주차
        if (!today.isBefore(subGoal.getStartDate()) && !today.isAfter(subGoal.getEndDate())) {
            // 수정 기한 내이면 가능
            return subGoal.getEditableUntil() != null && !today.isAfter(subGoal.getEditableUntil());
        }

        return false;
    }

    private void validateEditability(SubGoal subGoal) {
        LocalDate today = LocalDate.now();

        // 수정 기한 체크
        if (subGoal.getEditableUntil() != null && today.isAfter(subGoal.getEditableUntil())) {
            throw new MissionException(MissionErrorCode.NOT_EDITABLE);
        }

        // 이미 수정했는지 체크
        if (Boolean.TRUE.equals(subGoal.getHasBeenEdited())) {
            throw new MissionException(MissionErrorCode.ALREADY_EDITED);
        }

        // 수정 가능한 주차인지 체크
        if (!isEditableWeek(subGoal, today)) {
            throw new MissionException(MissionErrorCode.NOT_EDITABLE);
        }
    }

    private void applyWeeklyUpdates(SubGoal subGoal, WeeklyUpdateRequest request) {
        // Tasks 수정만 처리 (weekTitle 필드가 없으므로)
        if (request.getTaskUpdates() != null) {
            request.getTaskUpdates().forEach(taskUpdate -> {
                Task task = subGoal.getTasks().stream()
                        .filter(t -> t.getId().equals(taskUpdate.getTaskId()))
                        .findFirst()
                        .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

                task.setTitle(taskUpdate.getTitle());
            });
        }
    }

    private SubGoalResponse convertToSubGoalResponse(SubGoal subGoal) {
        List<TaskResponse> taskResponses = subGoal.getTasks().stream()
                .map(task -> TaskResponse.builder()
                        .taskId(task.getId())
                        .title(task.getTitle())
                        .dayNum(task.getDayNum())
                        .status(TaskStatus.PENDING)
                        .isToday(missionCalculateService.isToday(task))
                        .build())
                .collect(Collectors.toList());

        return SubGoalResponse.builder()
                .subGoalId(subGoal.getId())
                .title(subGoal.getTitle())
                .weekNum(subGoal.getOrderNum())
                .startDate(subGoal.getStartDate())
                .endDate(subGoal.getEndDate())
                .hasBeenEdited(subGoal.getHasBeenEdited())
                .editableUntil(subGoal.getEditableUntil())
                .weekProgressRate(missionCalculateService.calculateWeekProgress(subGoal))
                .tasks(taskResponses)
                .build();
    }

    private boolean isCurrentWeek(SubGoal subGoal) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(subGoal.getStartDate()) && !today.isAfter(subGoal.getEndDate());
    }
}