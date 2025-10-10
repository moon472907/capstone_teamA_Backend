package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.ai.AiMissionResult;
import com.back.domain.mission.dto.ai.DailyTask;
import com.back.domain.mission.dto.ai.WeeklyPlan;
import com.back.domain.mission.dto.request.PartyMissionCreateRequest;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.dto.response.SubGoalResponse;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.SubGoal;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.party.party.dto.PartyRequestDto;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.service.PartyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
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
    private static final int MAX_MISSIONS_PER_USER = 5;

    @Lazy
    private final TaskService taskService;

    // 파티/개인 미션 생성
    public MissionResponse createPartyMission(Integer memberId, PartyMissionCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MEMBER_NOT_FOUND));

        // 미션 개수 제한 체크
        Long count = missionRepository.countByMemberIdAndIsCompleted(memberId, false);
        if (count >= MAX_MISSIONS_PER_USER) {
            throw new MissionException(MissionErrorCode.MISSION_LIMIT_EXCEEDED);
        }

        // 파티 생성 (필요 시)
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
        return convertToDetailResponse(savedMission, memberId);
    }

    //  AI 기반 주차별 subgoal + task 생성
    private void generateAiSubGoals(Mission mission, PartyMissionCreateRequest request) {
        AiMissionResult aiResult = aiGeneratorService.generateMission(
                request.getTitle(),
                request.getPeriodWeeks(),
                mission.getMember().getId()
        );

        mission.setCategory(aiResult.getCategory());

        if (aiResult.getGoal() != null && !aiResult.getGoal().isBlank()) {
            mission.setTitle(aiResult.getGoal());
        }

        for (WeeklyPlan weekPlan : aiResult.getWeeklyPlans()) {
            LocalDate weekStart = mission.getStartDate().plusWeeks(weekPlan.getWeekNum() - 1);
            LocalDate weekEnd = weekStart.plusDays(6);

            SubGoal subGoal = SubGoal.builder()
                    .mission(mission)
                    .title(weekPlan.getTitle())
                    .orderNum(weekPlan.getWeekNum())
                    .startDate(weekStart)
                    .endDate(weekEnd)
                    .tasks(new ArrayList<>())
                    .build();

            for (DailyTask dailyTask : weekPlan.getDailyTasks()) {
                Task task = Task.builder()
                        .subGoal(subGoal)
                        .title(dailyTask.getTitle())
                        .dayNum(dailyTask.getDayNum())
                        .hasBeenEdited(false) //  Task 단위 수정 여부
                        .taskLogs(new ArrayList<>())
                        .build();
                subGoal.getTasks().add(task);
            }

            mission.getSubGoals().add(subGoal);
        }
    }

    // 기본 생성 로직
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
                    .tasks(new ArrayList<>())
                    .build();

            for (int day = 1; day <= 7; day++) {
                Task task = Task.builder()
                        .subGoal(subGoal)
                        .title(dayNames[day] + " 활동")
                        .dayNum(day)
                        .hasBeenEdited(false) //  Task 단위 수정 여부
                        .taskLogs(new ArrayList<>())
                        .build();
                subGoal.getTasks().add(task);
            }

            mission.getSubGoals().add(subGoal);
        }
    }

    //  Mission → MissionResponse 변환

    // 미션 기본 정보 (제목, 카테고리, 기간, 총 주차수, 완료 여부 등) + 진행률 계산 // subgoal, task X
    public MissionResponse convertToSimpleResponse(Mission mission,Integer memberId) {
        Integer totalWeeks = (int) ChronoUnit.WEEKS.between(
                mission.getStartDate(),
                mission.getEndDate().plusDays(1)
        );

        MissionResponse.MissionResponseBuilder builder = MissionResponse.builder()
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
                .partyId(mission.getParty() != null ? mission.getParty().getId() : null);

        if (mission.isPartyMission()) {
            builder.partyProgress(MissionResponse.PartyProgressDto.builder()
                    .myProgress(calculateService.calculatePartyMissionProgressForMe(mission, memberId))
                    .averageProgress(calculateService.calculatePartyMissionProgressAverage(mission))
                    .totalProgress(calculateService.calculatePartyMissionProgressTotal(mission))
                    .build());
        } else {
            builder.myProgressRate(calculateService.calculateMissionProgressForMember(mission, memberId));
        }

        return builder.build();
    }

    // 상세 조회  subgaol ( visible로 인해 미래 주차는 숨겨짐)
    public MissionResponse convertToDetailResponse(Mission mission, Integer memberId) {
        LocalDate today = LocalDate.now();
        int currentWeek = calculateService.getCurrentWeekNumber(mission, today);

        List<SubGoalResponse> subGoalResponses = mission.getSubGoals().stream()
                .map(sg -> buildSubGoalResponse(sg, mission, memberId, currentWeek, today, true))
                .collect(Collectors.toList());

        return convertToSimpleResponse(mission, memberId).toBuilder()
                .subGoals(subGoalResponses)
                .build();
    }

    // 관리자용 상세 조회 ( visible 제한 X )
    public MissionResponse convertToDetailResponseAdmin(Mission mission) {
        Integer memberId = mission.getMember().getId();
        LocalDate today = LocalDate.now();
        int currentWeek = calculateService.getCurrentWeekNumber(mission, today);

        List<SubGoalResponse> subGoalResponses = mission.getSubGoals().stream()
                .map(sg -> buildSubGoalResponse(sg, mission, memberId, currentWeek, today, false))
                .collect(Collectors.toList());

        return convertToSimpleResponse(mission, memberId).toBuilder()
                .subGoals(subGoalResponses)
                .build();
    }

    // subgoal DTO ( 주차별 테스크, 진행률, visible 여부)
    private SubGoalResponse buildSubGoalResponse(SubGoal sg, Mission mission, Integer memberId,
                                                 int currentWeek, LocalDate today, boolean applyVisible) {
        boolean visible = applyVisible ? calculateService.calculateVisible(mission, sg.getOrderNum(), currentWeek, today) : true;

        SubGoalResponse.SubGoalResponseBuilder builder = SubGoalResponse.builder()
                .subGoalId(sg.getId())
                .title(sg.getTitle())
                .weekNum(sg.getOrderNum())
                .startDate(sg.getStartDate())
                .endDate(sg.getEndDate())
                .visible(visible)
                .tasks(visible
                        ? taskService.toTaskResponsesBatch(sg.getTasks(), memberId, today)
                        : List.of()
                );

        if (mission.isPartyMission()) {
            builder.partyWeekProgress(SubGoalResponse.PartyWeekProgressDto.builder()
                    .myProgress(calculateService.calculateWeekProgressForMember(sg, memberId))
                    .averageProgress(calculateService.calculatePartyWeekAverage(sg))
                    .build());
        } else {
            builder.weekProgressRate(calculateService.calculateWeekProgressForMember(sg, memberId));
        }

        return builder.build();
    }

}
