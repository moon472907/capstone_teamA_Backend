package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.dto.ai.DailyTask;
import com.back.domain.mission.dto.ai.WeeklyPlan;
import com.back.domain.mission.dto.request.MissionCreateRequest;
import com.back.domain.mission.dto.request.MissionUpdateRequest;
import com.back.domain.mission.dto.request.TaskUpdateRequest;
import com.back.domain.mission.dto.response.MissionOverviewResponse;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.dto.response.SubGoalResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.entitiy.SubGoal;
import com.back.domain.mission.entitiy.Task;
import com.back.domain.mission.enums.MissionType;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {
    private final MissionRepository missionRepository;
    private final SubGoalRepository subGoalRepository;
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final MemberRepository memberRepository;
    private final MissionCalculateService missionCalculateService;
    private final AiMissionGeneratorService aiMissionGeneratorService;


    private static final int MAX_MISSIONS_PER_USER = 5;

    //미션 생성
    public MissionResponse createMission(Integer memberId, MissionCreateRequest request){
        // 사용자 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MEMBER_NOT_FOUND));

        // 5개 제한 체크
        validateMissionLimit(memberId);

        //시작일 종료일 계산
        LocalDate startDate = missionCalculateService.calculateStartDate();
        LocalDate endDate = missionCalculateService.calculateEndDate(startDate, request.getPeriodWeeks());

        // 미션 생성
        Mission mission = switch (request.getType()){
            case AI -> createAiMission(member, request, startDate, endDate);
            case CUSTOM -> createCustomMission(member, request, startDate, endDate);
        };

        //저장
        Mission savedMission = missionRepository.save(mission);


        // 6. 응답 생성
        return convertToMissionResponse(savedMission, true); // 생성시 subGoals 포함

    }

    //미션 목록 조회
    @Transactional(readOnly = true)
    public MissionOverviewResponse getMissions(Integer memberId){
        List<Mission> activceMissions = missionRepository.findByMemberIdAndIsCompleted(memberId, false);
        List<MissionResponse> activeSummaries = activceMissions.stream()
                .map(mission -> convertToMissionResponse(mission, false))
                .collect(Collectors.toList());

        List<Mission> completeMissions = missionRepository.findByMemberIdAndIsCompleted(memberId,false);
        List<MissionResponse> completedSummaries = completeMissions.stream()
                .map(mission -> convertToMissionResponse(mission, false))
                .collect(Collectors.toList());

        return MissionOverviewResponse.builder()
                .activeMissions(activeSummaries) // 진행중인 미션 목록
                .completedMissions(completedSummaries) // 완료된 미션 목록
                .activeMissionCount(activeSummaries.size()) // 진행중인 미션 개수
                .remainingSlots(MAX_MISSIONS_PER_USER - activeSummaries.size()) //남은 슬롯 계싼
                .build();
    }


    // 미션 상세 조회
    @Transactional(readOnly = true)
    public MissionResponse getMissionDetail(Integer memberId, Integer missionId){
        Mission mission = findMissionWithPermissionCheck(memberId, missionId);
        return convertToMissionResponse(mission, true);

    }


    //미선 수정
    public MissionResponse updateMission(Integer memberId, MissionUpdateRequest request){
        Mission mission = findMissionWithPermissionCheck(memberId, request.getMissionId());
        if(!mission.isEditable()) {
            throw new MissionException(MissionErrorCode.MISSION_NOT_EDITABLE);
        }

        if(!request.getConfirmUpdate()) {
            throw new MissionException(MissionErrorCode.UPDATE_CONFIRMATION_REQUIRED);
        }

        updateTasks(mission, request.getTaskUpdates());

        mission.setEditable(false);

        return convertToMissionResponse(mission,true);
    }



    private void validateMissionLimit(Integer memberId) {
        Long activeMissionCount = missionRepository.countByMemberIdAndIsCompleted(memberId, false);
        if (activeMissionCount >= MAX_MISSIONS_PER_USER) {
            throw new MissionException(MissionErrorCode.MISSION_LIMIT_EXCEEDED);
        }
    }


    // 사용자가 자신의 미션만 접근 가능
    private Mission findMissionWithPermissionCheck(Integer memberId, Integer missionId){
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        return mission;
    }


    private void updateTasks(Mission mission, List<TaskUpdateRequest> taskUpdates) {
        for (TaskUpdateRequest taskUpdate : taskUpdates) {
            Task task = taskRepository.findById(taskUpdate.getTaskId())
                    .orElseThrow(() -> new MissionException(MissionErrorCode.TASK_NOT_FOUND));

            if (!task.getSubGoal().getMission().getId().equals(mission.getId())) {
                throw new MissionException(MissionErrorCode.TASK_NOT_BELONGS_TO_MISSION);
            }

            task.setTitle(taskUpdate.getTitle());
        }
    }


    //createAiMission
    private Mission createAiMission(Member member, MissionCreateRequest request, LocalDate startDate, LocalDate endDate){
        // 나중에 가능하다면 log 달기

        //Ai 서비스 호출
        AiMissionResult aiResult = aiMissionGeneratorService.generateMission(
                request.getTitle(), request.getPeriodWeeks(), member.getId());

        Mission mission = Mission.builder()
                .member(member)
                .title(request.getTitle())
                .category(aiResult.getCategory())
                .type(MissionType.AI)
                .isEditable(true)
                .startDate(startDate)
                .endDate(endDate)
                .subGoals(new ArrayList<>())
                .build();


        //  AI가 생성한 주차별 계획으로 SubGoal과 Task 생성
        createSubGoalsAndTasks(mission, aiResult.getWeeklyPlans(), startDate);

        return mission ;
    }

    //사용자가 직접 만든 미션 생성
    private Mission createCustomMission(Member member, MissionCreateRequest request, LocalDate startDate, LocalDate endDate){
        //로그
        Mission mission = Mission.builder()
                .member(member)
                .title(request.getTitle())
                .category(request.getCategory())
                .type(MissionType.CUSTOM)
                .isCompleted(false)
                .isEditable(true)
                .startDate(startDate)
                .endDate(endDate)
                .subGoals(new ArrayList<>())
                .build();

        createBasicSubGoalsForCustom(mission, request.getPeriodWeeks(), startDate);
        return mission;
    }


    //ai 미션 생성 시 주차별/일별 계획 받아와서 엔티티 만들기
    private void createSubGoalsAndTasks(Mission mission, List<WeeklyPlan>weeklyPlans, LocalDate startDate){
        for (WeeklyPlan weeklyPlan : weeklyPlans){
            LocalDate weekStartDate = startDate.plusWeeks(weeklyPlan.getWeekNum() -1 ); // -1 안하면 2주차 시작함
            LocalDate weekEndDate  = weekStartDate.plusDays(6);

            SubGoal subGoal = SubGoal.builder()
                    .mission(mission)
                    .title(weeklyPlan.getTitle())
                    .orderNum(weeklyPlan.getWeekNum()) // 1주차, 2주차
                    .startDate(weekStartDate)
                    .endDate(weekEndDate)
                    .isEditable(true)
                    .tasks(new ArrayList<>())
                    .build();

            for(DailyTask dailyTask : weeklyPlan.getDailyTasks()){
                Task task = Task.builder()
                        .subGoal(subGoal)
                        .title(dailyTask.getTitle())
                        .dayNum(dailyTask.getDayNum())
                        .taskLogs(new ArrayList<>())
                        .build();
                subGoal.getTasks().add(task);
            }

            mission.getSubGoals().add(subGoal);
        }


    }

    // 커스텀 시 subgoal
    private void createBasicSubGoalsForCustom(Mission mission, Integer weeks, LocalDate startDate){
        for (int week = 1; week <= weeks; week ++ ){
            LocalDate weekStartDate = startDate.plusWeeks(week -1);
            LocalDate weekEndDate = weekStartDate.plusDays(6);

            SubGoal subGoal = SubGoal.builder()
                    .mission(mission)
                    .title(week + "주차")
                    .orderNum(week)
                    .startDate(weekStartDate)
                    .endDate(weekEndDate)
                    .isEditable(true)
                    .tasks(new ArrayList<>())
                    .build();
            mission.getSubGoals().add(subGoal);
        }
    }

    //mission 엔티티 -> missionResponse DTO 변환
    private MissionResponse convertToMissionResponse(Mission mission, boolean includeSubGoals) {
        Integer totalWeeks = (int) ChronoUnit.WEEKS.between(
                mission.getStartDate(), mission.getEndDate().plusDays(1));

        List<SubGoalResponse> subGoalResponses = null;
        if (includeSubGoals) {
            subGoalResponses = mission.getSubGoals().stream()
                    .map(subGoal -> convertToSubGoalResponse(subGoal, includeSubGoals))
                    .collect(Collectors.toList());
        }

        return MissionResponse.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .category(mission.getCategory())
                .type(mission.getType())
                .startDate(mission.getStartDate())
                .endDate(mission.getEndDate())
                .totalWeeks(totalWeeks)
                .currentWeek(missionCalculateService.calculateCurrentWeek(mission))
                .isCompleted(mission.isCompleted())
                .isEditable(mission.isEditable())
                .progressRate(missionCalculateService.calculateMissionProgress(mission))
                .subGoals(subGoalResponses)
                .build();
    }

    private SubGoalResponse convertToSubGoalResponse(SubGoal subGoal, boolean includeTasks) {
        List<TaskResponse> taskResponses = null;
        if (includeTasks) {
            taskResponses = subGoal.getTasks().stream()
                    .map(this::convertToTaskResponse)
                    .collect(Collectors.toList());
        }

        return SubGoalResponse.builder()
                .subGoalId(subGoal.getId())
                .title(subGoal.getTitle())
                .weekNum(subGoal.getOrderNum())
                .startDate(subGoal.getStartDate())
                .endDate(subGoal.getEndDate())
                .isEditable(subGoal.isEditable())
                .isCurrentWeek(missionCalculateService.isCurrentWeek(subGoal))
                .weekProgressRate(missionCalculateService.calculateWeekProgress(subGoal))
                .tasks(taskResponses)
                .build();
    }

    private TaskResponse convertToTaskResponse(Task task) {
        return TaskResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .dayNum(task.getDayNum())
                .status(TaskStatus.PENDING) // TODO: 실제 상태 조회
                .lastCompletedDate(null) // TODO: 실제 완료일 조회
                .isToday(missionCalculateService.isToday(task))
                .build();
    }

}
