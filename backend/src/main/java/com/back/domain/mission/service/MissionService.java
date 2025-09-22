package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.dto.ai.DailyTask;
import com.back.domain.mission.dto.ai.WeeklyPlan;
import com.back.domain.mission.dto.request.MissionCreateRequest;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.entitiy.SubGoal;
import com.back.domain.mission.entitiy.Task;
import com.back.domain.mission.enums.MissionType;
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



    private void validateMissionLimit(Integer memberId) {
        Long activeMissionCount = missionRepository.countByMemberIdAndIsCompleted(memberId, false);
        if (activeMissionCount >= MAX_MISSIONS_PER_USER) {
            throw new MissionException(MissionErrorCode.MISSION_LIMIT_EXCEEDED);
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

    }

    //mission 엔티티 -> missionResponse DTO 변환
    private MissionResponse convertToMissionResponse(Mission savedMission, boolean b) {
        return null;

    }



}
