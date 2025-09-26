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
            // setPublic 대신 setIsPublic 사용 (또는 PartyRequestDto 확인 필요)
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
                            // getHasBeenEdited() 대신 isHasBeenEdited() 또는 getHasBeenEdited() 직접 접근
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

        MissionResponse build = MissionResponse.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .category(mission.getCategory())
                .type(mission.getType())
                .startDate(mission.getStartDate())
                .endDate(mission.getEndDate())
                .totalWeeks(totalWeeks)
                .currentWeek(calculateService.calculateCurrentWeek(mission))
                .isCompleted(mission.isCompleted())
                .isPartyMission(mission.isPartyMission())
                .partyId(mission.getParty() != null ? mission.getParty().getId() : null)
                .progressRate(calculateService.calculateMissionProgress(mission))
                .subGoals(subGoalResponses)
                .build();
        return build;
    }
}