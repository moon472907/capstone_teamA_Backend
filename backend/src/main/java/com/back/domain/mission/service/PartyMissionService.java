package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.dto.ai.DailyTask;
import com.back.domain.mission.dto.ai.WeeklyPlan;
import com.back.domain.mission.dto.request.PartyMissionCreateRequest;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.dto.response.SubGoalResponse;
import com.back.domain.mission.entity.*;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.party.party.dto.PartyRequestDto;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.service.PartyService;
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
public class PartyMissionService {

    private final MissionRepository missionRepository;
    private final MemberRepository memberRepository;
    private final PartyService partyService;
    private final MissionCalculateService calculateService;
    private final AiMissionGeneratorService aiGeneratorService;
    private final TaskService taskService;
    private static final int MAX_MISSIONS_PER_USER = 5;

    // 파티 미션 생성
    public MissionResponse createPartyMission(Integer memberId, PartyMissionCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MEMBER_NOT_FOUND));

        // 미션 개수 제한 체크
        Long count = missionRepository.countByMemberIdAndIsCompleted(memberId, false);
        if (count >= MAX_MISSIONS_PER_USER) {
            throw new MissionException(MissionErrorCode.MISSION_LIMIT_EXCEEDED);
        }

        // 파티 생성 (필요시)
        Party party = null;
        if (request.getMaxMembers() > 1) {
            PartyRequestDto partyRequest = new PartyRequestDto();
            partyRequest.setName(request.getTitle());
            partyRequest.setMaxMembers(request.getMaxMembers());
            partyRequest.setIsPublicStatus(request.isPublic());
            party = partyService.createParty(partyRequest, memberId);
        }

        // 미션 생성
        LocalDate startDate = calculateService.calculateStartDate();
        LocalDate endDate = calculateService.calculateEndDate(startDate, request.getPeriodWeeks());

        Mission mission = Mission.builder()
                .member(member)
                .party(party)
                .title(request.getTitle())
                .type(request.getType())
                .category(request.getCategory())
                .startDate(startDate)
                .endDate(endDate)
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();


        if (request.getType() == MissionType.AI) {
            generateAiSubGoals(mission, request);
        } else {
            generateBasicSubGoals(mission, request.getPeriodWeeks());
        }

        Mission savedMission = missionRepository.save(mission);
        return convertToResponse(savedMission, true);
    }

    @Transactional(readOnly = true)
    public List<MissionResponse> getPartyMissions(Integer partyId) {
        List<Mission> missions = missionRepository.findByPartyId(partyId);

        return missions.stream()
                .map(mission -> convertToResponse(mission, false)) // 목록 조회니까 false
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MissionResponse getPartyMissionDetail(Integer partyId, Integer missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (mission.getParty() == null || !mission.getParty().getId().equals(partyId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN); // 파티 불일치 시 예외
        }

        return convertToResponse(mission, true); // 상세니까 subGoals까지 포함
    }


    // AI 기반으로 주차별  subgoal + task 생성
    private void generateAiSubGoals(Mission mission, PartyMissionCreateRequest request) {
        AiMissionResult aiResult = aiGeneratorService.generateMission(
                request.getTitle(),
                request.getPeriodWeeks(),
                mission.getMember().getId()
        );

        mission.setCategory(aiResult.getCategory());

        for (WeeklyPlan weekPlan : aiResult.getWeeklyPlans()) {
            LocalDate weekStart = mission.getStartDate().plusWeeks(weekPlan.getWeekNum() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            SubGoal subGoal = SubGoal.builder()
                    .mission(mission)
                    .title(weekPlan.getTitle())
                    .orderNum(weekPlan.getWeekNum())
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .hasBeenEdited(false)
                    .editableUntil(weekStart.plusDays(2))
                    .tasks(new ArrayList<>())
                    .build();

            for (DailyTask dailyTask : weekPlan.getDailyTasks()) {
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

    // 단순 기본 생성 로직 ( AI 아닐 경우)
    private void generateBasicSubGoals(Mission mission, Integer weeks) {
        String[] dayNames = {"", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일"};

        for (int week = 1; week <= weeks; week++) {
            LocalDate weekStart = mission.getStartDate().plusWeeks(week - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            SubGoal subGoal = SubGoal.builder()
                    .mission(mission)
                    .title(week + "주차 목표")
                    .orderNum(week)
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .hasBeenEdited(false)
                    .editableUntil(weekStart.plusDays(2))
                    .tasks(new ArrayList<>())
                    .build();

            for (int day = 1; day <= 7; day++) {
                Task task = Task.builder()
                        .subGoal(subGoal)
                        .title(dayNames[day] + " 활동")
                        .dayNum(day)
                        .taskLogs(new ArrayList<>())
                        .build();
                subGoal.getTasks().add(task);
            }

            mission.getSubGoals().add(subGoal);
        }
    }

    // Mission 엔티티 -> MissionResponse  변환
    //includeSubGoals=true면 주차별(SubGoal, Task)까지 전부 포함
    public MissionResponse convertToResponse(Mission mission, boolean includeSubGoals) {
        Integer totalWeeks = (int) ChronoUnit.WEEKS.between(
                mission.getStartDate(),
                mission.getEndDate().plusDays(1)
        );

        List<SubGoalResponse> subGoalResponses = null;
        if (includeSubGoals && mission.getSubGoals() != null) {
            subGoalResponses = mission.getSubGoals().stream()
                    .map(sg -> SubGoalResponse.builder()
                            .subGoalId(sg.getId())
                            .title(sg.getTitle())
                            .weekNum(sg.getOrderNum())
                            .startDate(sg.getStartDate())
                            .endDate(sg.getEndDate())
                            .hasBeenEdited(sg.getHasBeenEdited())  // Boolean 타입이므로 바로 접근
                            .editableUntil(sg.getEditableUntil())
                            .weekProgressRate(calculateService.calculateWeekProgress(sg))
                            .tasks(
                                    sg.getTasks().stream()
                                            .map(task -> taskService.toTaskResponse(task, mission.getMember().getId(), LocalDate.now()))
                                            .collect(Collectors.toList())
                            )
                            .build())
                    .collect(Collectors.toList());
        }

        MissionResponse.MissionResponseBuilder builder = MissionResponse.builder();
        builder.missionId(mission.getId());
        builder.title(mission.getTitle());
        builder.category(mission.getCategory());
        builder.type(mission.getType());
        builder.startDate(mission.getStartDate());
        builder.endDate(mission.getEndDate());
        builder.totalWeeks(totalWeeks);
        builder.currentWeek(calculateService.calculateCurrentWeek(mission));
        builder.isCompleted(mission.isCompleted());
        builder.isPartyMission(mission.isPartyMission());
        builder.partyId(mission.getParty() != null ? mission.getParty().getId() : null);
        builder.progressRate(calculateService.calculateMissionProgress(mission));
        builder.subGoals(subGoalResponses);
        MissionResponse build = builder
                .build();
        return build;
    }
}