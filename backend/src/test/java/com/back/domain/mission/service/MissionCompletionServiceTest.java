package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.entity.*;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.MissionCompletionLogRepository;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import com.back.domain.party.party.repository.PartyMemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MissionCompletionServiceTest {

    @Autowired
    private MissionCompletionService missionCompletionService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private MissionCompletionLogRepository completionLogRepository;

    @Autowired
    private TaskLogRepository taskLogRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    private Member testMember;
    private Member partyMember1;
    private Member partyMember2;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("testuser")
                .email("test@test.com")
                .birth(LocalDate.of(1990, 1, 1))
                .gender(MemberGender.MALE)
                .build();
        memberRepository.save(testMember);

        partyMember1 = Member.builder()
                .name("party1")
                .email("party1@test.com")
                .birth(LocalDate.of(1992, 2, 2))
                .gender(MemberGender.FEMALE)
                .build();
        memberRepository.save(partyMember1);

        partyMember2 = Member.builder()
                .name("party2")
                .email("party2@test.com")
                .birth(LocalDate.of(1993, 3, 3))
                .gender(MemberGender.MALE)
                .build();
        memberRepository.save(partyMember2);
    }

    @Test
    @DisplayName("개인 미션 100% 달성 시 완료 이벤트 발행")
    void checkAndCompleteMission_Personal100Percent_Success() {
        Mission mission = createPersonalMissionWithAllTasksCompleted();

        missionCompletionService.checkAndCompleteMission(
                mission.getId(),
                testMember.getId()
        );

        Mission updatedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(updatedMission.isCompleted()).isTrue();

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMemberId()).isEqualTo(testMember.getId());
        assertThat(logs.get(0).getMissionId()).isEqualTo(mission.getId());
    }

    @Test
    @DisplayName("개인 미션 100% 미만 시 완료 처리 안됨")
    void checkAndCompleteMission_PersonalUnder100Percent_NotCompleted() {

        Mission mission = createPersonalMissionWithPartialCompletion(50);

        missionCompletionService.checkAndCompleteMission(
                mission.getId(),
                testMember.getId()
        );


        Mission updatedMission = missionRepository.findById(mission.getId()).orElseThrow();
        assertThat(updatedMission.isCompleted()).isFalse();

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).isEmpty();
    }

    @Test
    @DisplayName("중복 완료 체크 - 같은 멤버가 다시 100% 달성해도 이벤트 한 번만 발행")
    void checkAndCompleteMission_DuplicateCheck_OnlyOncePublished() {
        Mission mission = createPersonalMissionWithAllTasksCompleted();

        missionCompletionService.checkAndCompleteMission(
                mission.getId(),
                testMember.getId()
        );

        missionCompletionService.checkAndCompleteMission(
                mission.getId(),
                testMember.getId()
        );

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).hasSize(1);
    }

    @Test
    @DisplayName("파티 미션 - 개인 100% 달성 시 완료 이벤트 발행")
    void checkAndCompleteMission_PartyMember100Percent_Success() {
        Mission partyMission = createPartyMissionWithMembers();
        completeAllTasksForMember(partyMission, testMember.getId());

        missionCompletionService.checkAndCompleteMission(
                partyMission.getId(),
                testMember.getId()
        );

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMemberId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("파티 미션 - 여러 멤버가 각각 100% 달성 시 각자 이벤트 발행")
    void checkAndCompleteMission_PartyMultipleMembers_EachEventPublished() {
        Mission partyMission = createPartyMissionWithMembers();
        completeAllTasksForMember(partyMission, testMember.getId());
        completeAllTasksForMember(partyMission, partyMember1.getId());

        missionCompletionService.checkAndCompleteMission(
                partyMission.getId(),
                testMember.getId()
        );
        missionCompletionService.checkAndCompleteMission(
                partyMission.getId(),
                partyMember1.getId()
        );

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).hasSize(2);
        assertThat(logs)
                .extracting(MissionCompletionLog::getMemberId)
                .containsExactlyInAnyOrder(testMember.getId(), partyMember1.getId());
    }

    @Test
    @DisplayName("파티 미션 - 일부 멤버만 100% 달성 시 해당 멤버만 완료 처리")
    void checkAndCompleteMission_PartyPartialCompletion_OnlyCompletedMemberLogged() {
        Mission partyMission = createPartyMissionWithMembers();
        completeAllTasksForMember(partyMission, testMember.getId()); // 100%
        completePartialTasksForMember(partyMission, partyMember1.getId(), 50); // 50%

        missionCompletionService.checkAndCompleteMission(
                partyMission.getId(),
                testMember.getId()
        );
        missionCompletionService.checkAndCompleteMission(
                partyMission.getId(),
                partyMember1.getId()
        );

        List<MissionCompletionLog> logs = completionLogRepository.findAll();
        assertThat(logs).hasSize(1); // testMember만 기록됨
        assertThat(logs.get(0).getMemberId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 미션 완료 체크 시 예외 발생")
    void checkAndCompleteMission_MissionNotFound_ThrowsException() {
        assertThatThrownBy(() ->
                missionCompletionService.checkAndCompleteMission(99999, testMember.getId()))
                .isInstanceOf(Exception.class);
    }

    // ===== Helper Methods =====

    private Mission createPersonalMissionWithAllTasksCompleted() {
        LocalDate startDate = LocalDate.now().minusDays(6);
        Mission mission = Mission.builder()
                .member(testMember)
                .title("완료 테스트 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();

        SubGoal subGoal = SubGoal.builder()
                .mission(mission)
                .title("1주차")
                .orderNum(1)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .tasks(new ArrayList<>())
                .build();

        for (int day = 1; day <= 7; day++) {
            Task task = Task.builder()
                    .subGoal(subGoal)
                    .title(day + "일차")
                    .dayNum(day)
                    .hasBeenEdited(false)
                    .taskLogs(new ArrayList<>())
                    .build();
            subGoal.getTasks().add(task);
        }

        mission.getSubGoals().add(subGoal);
        missionRepository.save(mission);

        completeAllTasksForMember(mission, testMember.getId());

        return mission;
    }

    private Mission createPersonalMissionWithPartialCompletion(int percentage) {
        LocalDate startDate = LocalDate.now().minusDays(6);
        Mission mission = Mission.builder()
                .member(testMember)
                .title("부분 완료 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();

        SubGoal subGoal = SubGoal.builder()
                .mission(mission)
                .title("1주차")
                .orderNum(1)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .tasks(new ArrayList<>())
                .build();

        for (int day = 1; day <= 7; day++) {
            Task task = Task.builder()
                    .subGoal(subGoal)
                    .title(day + "일차")
                    .dayNum(day)
                    .hasBeenEdited(false)
                    .taskLogs(new ArrayList<>())
                    .build();
            subGoal.getTasks().add(task);
        }

        mission.getSubGoals().add(subGoal);
        missionRepository.save(mission);

        completePartialTasksForMember(mission, testMember.getId(), percentage);

        return mission;
    }

    private Mission createPartyMissionWithMembers() {
        LocalDate startDate = LocalDate.now().minusDays(1);

        // Party 생성
        Party party = new Party();
        party.setName("테스트 파티");
        party.setMaxMembers(3);
        party.setPublic(true);
        party.setLeader(testMember);
        party.setViews(0);
        partyRepository.save(party);

        // Mission 생성
        Mission mission = Mission.builder()
                .member(testMember)
                .party(party)
                .title("파티 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();

        SubGoal subGoal = SubGoal.builder()
                .mission(mission)
                .title("1주차")
                .orderNum(1)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .tasks(new ArrayList<>())
                .build();

        for (int day = 1; day <= 7; day++) {
            Task task = Task.builder()
                    .subGoal(subGoal)
                    .title(day + "일차")
                    .dayNum(day)
                    .hasBeenEdited(false)
                    .taskLogs(new ArrayList<>())
                    .build();
            subGoal.getTasks().add(task);
        }

        mission.getSubGoals().add(subGoal);
        missionRepository.save(mission);

        PartyMember pm1 = new PartyMember();
        pm1.setParty(party);
        pm1.setMember(testMember);
        pm1.setStatus(PartyMemberStatus.ACCEPTED);
        pm1.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(pm1);

        PartyMember pm2 = new PartyMember();
        pm2.setParty(party);
        pm2.setMember(partyMember1);
        pm2.setStatus(PartyMemberStatus.ACCEPTED);
        pm2.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(pm2);

        PartyMember pm3 = new PartyMember();
        pm3.setParty(party);
        pm3.setMember(partyMember2);
        pm3.setStatus(PartyMemberStatus.ACCEPTED);
        pm3.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(pm3);

        return mission;
    }

    private void completeAllTasksForMember(Mission mission, Integer memberId) {
        mission.getSubGoals().forEach(subGoal -> {
            subGoal.getTasks().forEach(task -> {
                LocalDate taskDate = subGoal.getStartDate().plusDays(task.getDayNum() - 1);

                TaskLog taskLog = TaskLog.builder()
                        .task(task)
                        .memberId(memberId)
                        .partyId(mission.getParty() != null ? mission.getParty().getId() : null)
                        .date(taskDate)
                        .status(TaskStatus.COMPLETED)
                        .build();
                taskLogRepository.save(taskLog);
            });
        });
    }

    private void completePartialTasksForMember(Mission mission, Integer memberId, int percentage) {
        List<Task> allTasks = mission.getSubGoals().stream()
                .flatMap(sg -> sg.getTasks().stream())
                .toList();

        int tasksToComplete = (int) Math.ceil(allTasks.size() * percentage / 100.0);

        for (int i = 0; i < tasksToComplete && i < allTasks.size(); i++) {
            Task task = allTasks.get(i);
            SubGoal subGoal = task.getSubGoal();
            LocalDate taskDate = subGoal.getStartDate().plusDays(task.getDayNum() - 1);

            TaskLog taskLog = TaskLog.builder()
                    .task(task)
                    .memberId(memberId)
                    .partyId(mission.getParty() != null ? mission.getParty().getId() : null)
                    .date(taskDate)
                    .status(TaskStatus.COMPLETED)
                    .build();
            taskLogRepository.save(taskLog);
        }
    }
}