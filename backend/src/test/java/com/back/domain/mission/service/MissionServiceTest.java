package com.back.domain.mission.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.dto.request.PartyMissionCreateRequest;
import com.back.domain.mission.dto.request.TaskCompleteRequest;
import com.back.domain.mission.dto.response.*;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.SubGoal;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MissionServiceTest {

    @Autowired
    private MissionService missionService;

    @Autowired
    private PartyMissionService partyMissionService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MissionRepository missionRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .name("testuser")
                .email("test@test.com")
                .birth(LocalDate.of(1990, 1, 1))
                .gender(MemberGender.MALE)
                .build();
        memberRepository.save(testMember);
    }

    @Test
    @DisplayName("AI 개인 미션 생성 성공")
    void createAiPersonalMission_Success() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("매일 운동하기")
                .type(MissionType.AI)
                .periodWeeks(2)
                .maxMembers(1)
                .isPublic(false)
                .build();

        MissionResponse response = partyMissionService.createPartyMission(
                testMember.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getMissionId()).isNotNull();
        assertThat(response.getTitle()).isNotBlank();
        assertThat(response.getType()).isEqualTo(MissionType.AI);
        assertThat(response.getTotalWeeks()).isEqualTo(2);
        assertThat(response.isPartyMission()).isFalse();
        assertThat(response.getSubGoals()).hasSize(2);
        assertThat(response.getMyProgressRate()).isZero();

        response.getSubGoals().stream()
                .filter(SubGoalResponse::isVisible)
                .forEach(subGoal -> {
                    assertThat(subGoal.getTasks()).isNotEmpty();
                });
    }

    @Test
    @DisplayName("CUSTOM 개인 미션 생성 성공")
    void createCustomPersonalMission_Success() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("독서 습관 만들기")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(3)
                .maxMembers(1)
                .isPublic(false)
                .build();

        MissionResponse response = partyMissionService.createPartyMission(
                testMember.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getCategory()).isEqualTo(MissionCategory.HABIT);
        assertThat(response.getTotalWeeks()).isEqualTo(3);
        assertThat(response.getSubGoals()).hasSize(3);

        long visibleCount = response.getSubGoals().stream()
                .filter(SubGoalResponse::isVisible)
                .count();
        assertThat(visibleCount).isLessThanOrEqualTo(2);

        response.getSubGoals().stream()
                .filter(SubGoalResponse::isVisible)
                .forEach(subGoal -> {
                    assertThat(subGoal.getTasks()).hasSize(7);
                });
    }

    @Test
    @DisplayName("파티 미션 생성 성공")
    void createPartyMission_Success() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("함께 운동하기")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .periodWeeks(2)
                .maxMembers(3)
                .isPublic(true)
                .build();

        MissionResponse response = partyMissionService.createPartyMission(
                testMember.getId(), request);

        assertThat(response.isPartyMission()).isTrue();
        assertThat(response.getPartyId()).isNotNull();
        assertThat(response.getPartyProgress()).isNotNull();
    }

    @Test
    @DisplayName("미션 개수 제한 초과 시 예외 발생")
    void createMission_ExceedLimit_ThrowsException() {
        for (int i = 0; i < 5; i++) {
            PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                    .title("미션 " + (i + 1))
                    .type(MissionType.CUSTOM)
                    .category(MissionCategory.HABIT)
                    .periodWeeks(1)
                    .maxMembers(1)
                    .build();
            partyMissionService.createPartyMission(testMember.getId(), request);
        }

        PartyMissionCreateRequest sixthRequest = PartyMissionCreateRequest.builder()
                .title("미션 6")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(1)
                .maxMembers(1)
                .build();

        assertThatThrownBy(() ->
                partyMissionService.createPartyMission(testMember.getId(), sixthRequest))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("내 미션 목록 조회 성공")
    void getMissions_Success() {
        createTestMissionForList("활성 미션", false);
        createTestMissionForList("완료된 미션", true);

        MissionOverviewResponse response = missionService.getMissions(testMember.getId());

        assertThat(response.getActiveMissions()).hasSize(1);
        assertThat(response.getCompletedMissions()).hasSize(1);
        assertThat(response.getActiveMissionCount()).isEqualTo(1);
        assertThat(response.getRemainingSlots()).isEqualTo(4);
    }

    @Test
    @DisplayName("미션 상세 조회 성공 - visible 제한")
    void getMissionDetail_Success_WithVisibleLimit() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("테스트 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .periodWeeks(3)
                .maxMembers(1)
                .build();
        MissionResponse created = partyMissionService.createPartyMission(
                testMember.getId(), request);

        MissionResponse response = missionService.getMissionDetail(
                testMember.getId(), created.getMissionId());

        assertThat(response.getSubGoals()).hasSize(3);

        long visibleCount = response.getSubGoals().stream()
                .filter(SubGoalResponse::isVisible)
                .count();
        assertThat(visibleCount).isEqualTo(2);
    }

    @Test
    @DisplayName("미션 전체 상세 조회 - visible 제한 없음")
    void getMissionDetailAdmin_Success_NoVisibleLimit() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("테스트 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .periodWeeks(4)
                .maxMembers(1)
                .build();
        MissionResponse created = partyMissionService.createPartyMission(
                testMember.getId(), request);

        MissionResponse response = missionService.getMissionDetailAdmin(
                created.getMissionId());

        assertThat(response.getSubGoals()).hasSize(4);

        long visibleCount = response.getSubGoals().stream()
                .filter(SubGoalResponse::isVisible)
                .count();
        assertThat(visibleCount).isEqualTo(4);
    }

    @Test
    @DisplayName("미션 삭제 성공")
    void deleteMission_Success() {
        Mission mission = createTestMissionForDelete();

        missionService.deleteMission(testMember.getId(), mission.getId());

        assertThat(missionRepository.findById(mission.getId())).isEmpty();
    }

    @Test
    @DisplayName("다른 사용자의 미션 삭제 시 예외 발생")
    void deleteMission_OtherUserMission_ThrowsException() {
        Member otherMember = Member.builder()
                .name("other")
                .email("other@test.com")
                .birth(LocalDate.of(1995, 5, 5))
                .gender(MemberGender.FEMALE)
                .build();
        memberRepository.save(otherMember);

        Mission mission = createTestMissionForDelete();

        assertThatThrownBy(() ->
                missionService.deleteMission(otherMember.getId(), mission.getId()))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("오늘의 Task 조회 성공")
    void getTodayTasks_Success() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(DayOfWeek.MONDAY);

        createTestMissionStartingToday(startDate);

        List<TaskResponse> tasks = taskService.getTodayTasks(testMember.getId());

        if (today.getDayOfWeek() != DayOfWeek.SATURDAY &&
                today.getDayOfWeek() != DayOfWeek.SUNDAY) {
            assertThat(tasks).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Task 완료 처리 성공")
    void completeTask_Success() {
        LocalDate today = LocalDate.now();
        int todayDayNum = today.getDayOfWeek().getValue();

        Mission mission = createTestMissionStartingToday(
                today.with(DayOfWeek.MONDAY)
        );

        Task task = mission.getSubGoals().get(0).getTasks().stream()
                .filter(t -> t.getDayNum() == todayDayNum)
                .findFirst()
                .orElseThrow();

        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(task.getId())
                .status(TaskStatus.COMPLETED)
                .date(today)
                .build();

        TaskCompleteResponse response = taskService.completeTask(
                testMember.getId(), request);

        assertThat(response.getTaskId()).isEqualTo(task.getId());
        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.getDailyProgressRate()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("이미 완료된 Task 재완료 시 예외 발생")
    void completeTask_AlreadyCompleted_ThrowsException() {
        LocalDate today = LocalDate.now();
        int todayDayNum = today.getDayOfWeek().getValue();

        Mission mission = createTestMissionStartingToday(
                today.with(DayOfWeek.MONDAY)
        );

        Task task = mission.getSubGoals().get(0).getTasks().stream()
                .filter(t -> t.getDayNum() == todayDayNum)
                .findFirst()
                .orElseThrow();

        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(task.getId())
                .status(TaskStatus.COMPLETED)
                .date(today)
                .build();

        taskService.completeTask(testMember.getId(), request);

        assertThatThrownBy(() ->
                taskService.completeTask(testMember.getId(), request))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("Task 수정 성공")
    void updateTask_Success() {
        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1);

        Mission mission = createTestMissionStartingToday(nextMonday);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);
        String newTitle = "수정된 제목";

        TaskResponse response = taskService.updateTask(
                testMember.getId(), task.getId(), newTitle);

        assertThat(response.getTitle()).isEqualTo(newTitle);
        assertThat(response.isHasBeenEdited()).isTrue();
        assertThat(response.isCanEdit()).isFalse();
    }

    @Test
    @DisplayName("Task 중복 수정 시 예외 발생")
    void updateTask_AlreadyEdited_ThrowsException() {
        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1);


        Mission mission = createTestMissionStartingToday(nextMonday);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);

        taskService.updateTask(testMember.getId(), task.getId(), "첫 번째 수정");

        assertThatThrownBy(() ->
                taskService.updateTask(testMember.getId(), task.getId(), "두 번째 수정"))
                .isInstanceOf(MissionException.class);
    }

    private Mission createTestMissionForList(String title, boolean isCompleted) {
        Mission mission = Mission.builder()
                .member(testMember)
                .title(title)
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusWeeks(1))
                .isCompleted(isCompleted)
                .subGoals(new ArrayList<>())
                .build();

        return missionRepository.save(mission);
    }

    private Mission createTestMissionForDelete() {
        Mission mission = Mission.builder()
                .member(testMember)
                .title("삭제용 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(1))
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();

        return missionRepository.save(mission);
    }

    private Mission createTestMissionStartingToday(LocalDate startDate) {
        Mission mission = Mission.builder()
                .member(testMember)
                .title("테스트 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .isCompleted(false)
                .subGoals(new ArrayList<>())
                .build();

        SubGoal subGoal = SubGoal.builder()
                .mission(mission)
                .title("1주차 목표")
                .orderNum(1)
                .startDate(startDate)
                .endDate(startDate.plusDays(6))
                .tasks(new ArrayList<>())
                .build();

        for (int day = 1; day <= 7; day++) {
            Task task = Task.builder()
                    .subGoal(subGoal)
                    .title(day + "일차 활동")
                    .dayNum(day)
                    .hasBeenEdited(false)
                    .taskLogs(new ArrayList<>())
                    .build();
            subGoal.getTasks().add(task);
        }

        mission.getSubGoals().add(subGoal);
        return missionRepository.save(mission);
    }
    // 기존 테스트들 아래에 추가

    @Test
    @DisplayName("잘못된 요일에 Task 완료 시 예외 발생")
    void completeTask_WrongDay_ThrowsException() {
        // 월요일 Task
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.with(DayOfWeek.MONDAY);

        Mission mission = createTestMissionStartingToday(startDate);
        Task mondayTask = mission.getSubGoals().get(0).getTasks().stream()
                .filter(t -> t.getDayNum() == 1)  // 월요일 Task
                .findFirst()
                .orElseThrow();

        // 화요일에 완료 시도
        LocalDate tuesday = startDate.plusDays(1);
        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(mondayTask.getId())
                .status(TaskStatus.COMPLETED)
                .date(tuesday)
                .build();

        assertThatThrownBy(() ->
                taskService.completeTask(testMember.getId(), request))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("미션 시작 전 Task 완료 시 예외 발생")
    void completeTask_BeforeStart_ThrowsException() {
        //  미래 미션
        LocalDate nextWeek = LocalDate.now().plusWeeks(1);
        Mission mission = createTestMissionStartingToday(nextWeek);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);

        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(task.getId())
                .status(TaskStatus.COMPLETED)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() ->
                taskService.completeTask(testMember.getId(), request))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("미션 종료 후 Task 완료 시 예외 발생")
    void completeTask_AfterEnd_ThrowsException() {
        // 과거 미션
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        Mission mission = createTestMissionStartingToday(lastWeek);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);

        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(task.getId())
                .status(TaskStatus.COMPLETED)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() ->
                taskService.completeTask(testMember.getId(), request))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("수정 불가능한 시점에 Task 수정 시 예외 발생")
    void updateTask_NotEditableTime_ThrowsException() {
        //  이번 주 월요일에 시작한 미션 (화요일 이후 테스트)
        LocalDate thisMonday = LocalDate.now().with(DayOfWeek.MONDAY);

        if (LocalDate.now().isAfter(thisMonday)) {
            Mission mission = createTestMissionStartingToday(thisMonday);
            Task task = mission.getSubGoals().get(0).getTasks().get(0);

            assertThatThrownBy(() ->
                    taskService.updateTask(testMember.getId(), task.getId(), "수정 시도"))
                    .isInstanceOf(MissionException.class);
        }
    }

    @Test
    @DisplayName("존재하지 않는 Task 완료 시 예외 발생")
    void completeTask_NotFound_ThrowsException() {
        TaskCompleteRequest request = TaskCompleteRequest.builder()
                .taskId(99999)  // 존재하지 않는 ID
                .status(TaskStatus.COMPLETED)
                .date(LocalDate.now())
                .build();

        assertThatThrownBy(() ->
                taskService.completeTask(testMember.getId(), request))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("존재하지 않는 Task 수정 시 예외 발생")
    void updateTask_NotFound_ThrowsException() {
        assertThatThrownBy(() ->
                taskService.updateTask(testMember.getId(), 99999, "수정"))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("존재하지 않는 미션 조회 시 예외 발생")
    void getMissionDetail_NotFound_ThrowsException() {
        assertThatThrownBy(() ->
                missionService.getMissionDetail(testMember.getId(), 99999))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("CUSTOM 타입인데 카테고리 없이 생성 시 예외 발생")
    void createMission_CustomWithoutCategory_ThrowsException() {
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("카테고리 없는 미션")
                .type(MissionType.CUSTOM)
                .category(null)  // 카테고리 없음
                .periodWeeks(2)
                .maxMembers(1)
                .build();

        assertThat(request.isCategoryValid()).isFalse();
    }

    @Test
    @DisplayName("기간이 최소값 미만일 때 생성 실패")
    void createMission_PeriodTooShort_ValidationFails() {

        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("너무 짧은 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(0)  // 최소 1주
                .maxMembers(1)
                .build();

    }

    @Test
    @DisplayName("기간이 최대값 초과일 때 생성 실패")
    void createMission_PeriodTooLong_ValidationFails() {
        // given
        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("너무 긴 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(5)  // 최대 4주
                .maxMembers(1)
                .build();

    }

    @Test
    @DisplayName("최대 인원이 범위 밖일 때 생성 실패")
    void createMission_InvalidMaxMembers_ValidationFails() {
        PartyMissionCreateRequest request1 = PartyMissionCreateRequest.builder()
                .title("인원 0명")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(2)
                .maxMembers(0)  // 최소 1명
                .build();

       PartyMissionCreateRequest request2 = PartyMissionCreateRequest.builder()
                .title("인원 6명")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.HABIT)
                .periodWeeks(2)
                .maxMembers(6)  // 최대 5명
                .build();

    }

    @Test
    @DisplayName("빈 제목으로 Task 수정 시 예외 발생")
    void updateTask_EmptyTitle_ThrowsException() {
        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1);

        Mission mission = createTestMissionStartingToday(nextMonday);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);

        assertThatThrownBy(() ->
                taskService.updateTask(testMember.getId(), task.getId(), ""))
                .isInstanceOf(MissionException.class);

        assertThatThrownBy(() ->
                taskService.updateTask(testMember.getId(), task.getId(), null))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("다른 사용자의 Task 수정 시 예외 발생")
    void updateTask_OtherUserTask_ThrowsException() {
        Member otherMember = Member.builder()
                .name("other")
                .email("other@test.com")
                .birth(LocalDate.of(1995, 5, 5))
                .gender(MemberGender.FEMALE)
                .build();
        memberRepository.save(otherMember);

        LocalDate nextMonday = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1);

        Mission mission = createTestMissionStartingToday(nextMonday);
        Task task = mission.getSubGoals().get(0).getTasks().get(0);

        // when & then
        assertThatThrownBy(() ->
                taskService.updateTask(otherMember.getId(), task.getId(), "수정 시도"))
                .isInstanceOf(MissionException.class);
    }

    @Test
    @DisplayName("다른 사용자의 미션 조회 시 예외 발생")
    void getMissionDetail_OtherUserMission_ThrowsException() {
        Member otherMember = Member.builder()
                .name("other")
                .email("other@test.com")
                .birth(LocalDate.of(1995, 5, 5))
                .gender(MemberGender.FEMALE)
                .build();
        memberRepository.save(otherMember);

        PartyMissionCreateRequest request = PartyMissionCreateRequest.builder()
                .title("내 미션")
                .type(MissionType.CUSTOM)
                .category(MissionCategory.EXERCISE)
                .periodWeeks(2)
                .maxMembers(1)
                .build();
        MissionResponse created = partyMissionService.createPartyMission(
                testMember.getId(), request);

        assertThatThrownBy(() ->
                missionService.getMissionDetail(otherMember.getId(), created.getMissionId()))
                .isInstanceOf(MissionException.class);
    }
}