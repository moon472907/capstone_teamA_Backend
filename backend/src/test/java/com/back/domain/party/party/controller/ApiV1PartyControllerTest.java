package com.back.domain.party.party.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.party.party.dto.InvitationDto;
import com.back.domain.party.party.dto.PartyRequestDto;
import com.back.domain.party.party.dto.PartyUpdateRequestDto;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import com.back.domain.party.party.repository.PartyMemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiV1PartyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PartyMemberRepository partyMemberRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Member leader;
    private Member member1;
    private Member invitedMember;

    private final String uniqueEmailPrefix = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        partyMemberRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        partyRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        leader = Member.builder()
                .email(uniqueEmailPrefix + "leader@test.com")
                .password("password")
                .name("리더")
                .birth(LocalDate.of(1995, 1, 1))
                .gender(MemberGender.MALE)
                .code(UUID.randomUUID().toString().substring(0, 6))
                .build();
        memberRepository.save(leader);

        member1 = Member.builder()
                .email(uniqueEmailPrefix + "member1@test.com")
                .password("password")
                .name("멤버1")
                .birth(LocalDate.of(2000, 5, 10))
                .gender(MemberGender.FEMALE)
                .code(UUID.randomUUID().toString().substring(0, 6))
                .build();
        memberRepository.save(member1);

        invitedMember = Member.builder()
                .email(uniqueEmailPrefix + "invited@test.com")
                .password("password")
                .name("초대받은사람")
                .birth(LocalDate.of(1997, 3, 22))
                .gender(MemberGender.MALE)
                .code(UUID.randomUUID().toString().substring(0, 6))
                .build();
        memberRepository.save(invitedMember);
    }

    @AfterEach
    void tearDown() {
        partyMemberRepository.deleteAllInBatch();
        missionRepository.deleteAllInBatch();
        partyRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    // Authentication 객체를 생성하는 헬퍼 메서드
    private Authentication createAuthentication(Integer memberId) {
        return new UsernamePasswordAuthenticationToken(String.valueOf(memberId), null, null);
    }

    @Test
    @DisplayName("파티 생성 통합 테스트 성공")
    void createParty_success_integration() throws Exception {
        PartyRequestDto requestDto = new PartyRequestDto();
        requestDto.setName("통합 테스트 파티");
        requestDto.setMaxMembers(5);
        requestDto.setIsPublicStatus(true);

        mockMvc.perform(post("/api/v1/parties")
                        .with(authentication(createAuthentication(leader.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content.name").value("통합 테스트 파티"))
                .andExpect(jsonPath("$.code").value("201"))
                .andExpect(jsonPath("$.message").value("파티 생성 성공"));
    }

    @Test
    @DisplayName("파티 가입 신청 통합 테스트 성공")
    void joinParty_success_integration() throws Exception {
        Party party = new Party();
        party.setName("공개 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        mockMvc.perform(post("/api/v1/parties/{partyId}/join", party.getId())
                        .with(authentication(createAuthentication(member1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 가입 신청 성공"));
    }

    @Test
    @DisplayName("파티 탈퇴 통합 테스트 성공")
    void leaveParty_success_integration() throws Exception {
        Party party = new Party();
        party.setName("탈퇴 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyMember leaderMember = new PartyMember();
        leaderMember.setParty(party);
        leaderMember.setMember(leader);
        leaderMember.setStatus(PartyMemberStatus.ACCEPTED);
        leaderMember.setJoinedAt(LocalDateTime.now().minusHours(1));
        partyMemberRepository.save(leaderMember);

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(delete("/api/v1/parties/{partyId}/leave", party.getId())
                        .with(authentication(createAuthentication(member1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 탈퇴 성공"));
    }

    @Test
    @DisplayName("파티 수정 통합 테스트 성공")
    void updateParty_success_integration() throws Exception {
        Party party = new Party();
        party.setName("수정 전 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyUpdateRequestDto requestDto = new PartyUpdateRequestDto();
        requestDto.setName("수정 후 파티");
        requestDto.setMaxMembers(4);
        requestDto.setIsPublicStatus(false);

        mockMvc.perform(patch("/api/v1/parties/{partyId}", party.getId())
                        .with(authentication(createAuthentication(leader.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 수정 성공"));
    }

    @Test
    @DisplayName("파티 삭제 통합 테스트 성공")
    void deleteParty_success_integration() throws Exception {
        Party party = new Party();
        party.setName("삭제 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        mockMvc.perform(delete("/api/v1/parties/{partyId}", party.getId())
                        .with(authentication(createAuthentication(leader.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 삭제 성공"));
    }

    @Test
    @DisplayName("파티 목록 조회 통합 테스트 성공 (공개 파티 목록 조회)")
    void getPartyList_success_integration() throws Exception {
        Mission mission = Mission.builder()
                .title("테스트 미션")
                .category(MissionCategory.EXERCISE)
                .type(MissionType.CUSTOM)
                .startDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 31))
                .member(leader)
                .build();
        missionRepository.save(mission);

        Party party1 = new Party();
        party1.setName("공개 파티1");
        party1.setLeader(leader);
        party1.setMaxMembers(5);
        party1.setPublic(true);
        LocalDateTime date1 = LocalDateTime.now().minusDays(1);
        setBaseEntityCreateDate(party1, date1);
        partyRepository.save(party1);

        PartyMember leaderMember1 = new PartyMember();
        leaderMember1.setParty(party1);
        leaderMember1.setMember(leader);
        leaderMember1.setStatus(PartyMemberStatus.ACCEPTED);
        leaderMember1.setJoinedAt(date1);
        partyMemberRepository.save(leaderMember1);


        mission.setParty(party1);
        missionRepository.save(mission);

        Party party2 = new Party();
        party2.setName("공개 파티2");
        party2.setLeader(member1);
        party2.setMaxMembers(3);
        party2.setPublic(true);
        LocalDateTime date2 = LocalDateTime.now().minusDays(2);
        setBaseEntityCreateDate(party2, date2);
        partyRepository.save(party2);

        PartyMember leaderMember2 = new PartyMember();
        leaderMember2.setParty(party2);
        leaderMember2.setMember(member1);
        leaderMember2.setStatus(PartyMemberStatus.ACCEPTED);
        leaderMember2.setJoinedAt(date2);
        partyMemberRepository.save(leaderMember2);

        Party privateParty = new Party();
        privateParty.setName("비공개 파티");
        privateParty.setLeader(leader);
        privateParty.setMaxMembers(2);
        privateParty.setPublic(false);
        LocalDateTime date3 = LocalDateTime.now().minusDays(3);
        setBaseEntityCreateDate(privateParty, date3);
        partyRepository.save(privateParty);

        PartyMember leaderPrivate = new PartyMember();
        leaderPrivate.setParty(privateParty);
        leaderPrivate.setMember(leader);
        leaderPrivate.setStatus(PartyMemberStatus.ACCEPTED);
        leaderPrivate.setJoinedAt(date3);
        partyMemberRepository.save(leaderPrivate);


        mockMvc.perform(get("/api/v1/parties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 목록 조회 성공"))

                .andExpect(jsonPath("$.content.content[0].name").value("공개 파티1"))
                .andExpect(jsonPath("$.content.content[0].leaderId").value(leader.getId()))
                .andExpect(jsonPath("$.content.content[0].isPublic").value(true))
                .andExpect(jsonPath("$.content.content[0].currentMembers").value(1))
                .andExpect(jsonPath("$.content.content[0].category").value(MissionCategory.EXERCISE.name()))
                .andExpect(jsonPath("$.content.content[0].startDate").value("2025-10-01"))
                .andExpect(jsonPath("$.content.content[0].endDate").value("2025-10-31"))
                .andExpect(jsonPath("$.content.content[0].missionId").value(mission.getId()))
                .andExpect(jsonPath("$.content.content[0].createDate").value(
                        LocalDate.now().toString()
                ))

                .andExpect(jsonPath("$.content.content[1].name").value("공개 파티2"))
                .andExpect(jsonPath("$.content.content[1].leaderId").value(member1.getId()))
                .andExpect(jsonPath("$.content.content[1].isPublic").value(true))
                .andExpect(jsonPath("$.content.content[1].currentMembers").value(1))
                .andExpect(jsonPath("$.content.content[1].category").isEmpty())
                .andExpect(jsonPath("$.content.content[1].startDate").isEmpty())
                .andExpect(jsonPath("$.content.content[1].endDate").isEmpty())
                .andExpect(jsonPath("$.content.content[1].missionId").isEmpty())
                .andExpect(jsonPath("$.content.content[1].createDate").value(
                        LocalDate.now().toString()
                ));
    }

    @Test
    @DisplayName("특정 파티 조회 통합 테스트 성공")
    void getPartyDetails_success_integration() throws Exception {
        Party party = new Party();
        party.setName("조회 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        mockMvc.perform(get("/api/v1/parties/{partyId}", party.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.name").value("조회 파티"))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 상세 조회 성공"));
    }

    @Test
    @DisplayName("파티 초대 통합 테스트 성공")
    void inviteMember_success_integration() throws Exception {
        Party party = new Party();
        party.setName("초대 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        InvitationDto invitationDto = new InvitationDto();
        invitationDto.setInvitedMemberCode(invitedMember.getCode());

        mockMvc.perform(post("/api/v1/parties/{partyId}/invite", party.getId())
                        .with(authentication(createAuthentication(leader.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invitationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 초대 성공"));
    }

    @Test
    @DisplayName("초대/신청 수락 통합 테스트 성공")
    void acceptInvitation_success_integration() throws Exception {
        Party party = new Party();
        party.setName("수락 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(post("/api/v1/parties/{partyId}/accept", party.getId())
                        .with(authentication(createAuthentication(member1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("초대/신청 수락 성공"));
    }

    @Test
    @DisplayName("초대/신청 거절 통합 테스트 성공")
    void rejectInvitation_success_integration() throws Exception {
        Party party = new Party();
        party.setName("거절 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(post("/api/v1/parties/{partyId}/reject", party.getId())
                        .with(authentication(createAuthentication(member1.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("초대/신청 거절 성공"));
    }

    @Test
    @DisplayName("파티원 추방 통합 테스트 성공")
    void kickMember_success_integration() throws Exception {
        Party party = new Party();
        party.setName("추방 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyMember leaderMember = new PartyMember();
        leaderMember.setParty(party);
        leaderMember.setMember(leader);
        leaderMember.setStatus(PartyMemberStatus.ACCEPTED);
        leaderMember.setJoinedAt(LocalDateTime.now().minusHours(1));
        partyMemberRepository.save(leaderMember);

        // 추방될 멤버 추가
        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(delete("/api/v1/parties/{partyId}/members/{kickedMemberId}", party.getId(), member1.getId())
                        .with(authentication(createAuthentication(leader.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티원 추방 성공"));
    }

    @Test
    @DisplayName("가입 신청/초대 목록 조회 통합 테스트 성공")
    void getPendingJoinRequests_success_integration() throws Exception {
        Party party = new Party();
        party.setName("대기 파티");
        party.setLeader(leader);
        party.setMaxMembers(5);
        party.setPublic(true);
        partyRepository.save(party);

        PartyMember leaderMember = new PartyMember();
        leaderMember.setParty(party);
        leaderMember.setMember(leader);
        leaderMember.setStatus(PartyMemberStatus.ACCEPTED);
        leaderMember.setJoinedAt(LocalDateTime.now().minusHours(1));
        partyMemberRepository.save(leaderMember);

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(get("/api/v1/parties/{partyId}/requests", party.getId())
                        .with(authentication(createAuthentication(leader.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("멤버1"))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("가입 신청/초대 목록 조회 성공"));
    }

    /**
     * private/protected 필드를 Reflection을 사용하여 설정하는 헬퍼 메서드 (테스트용)
     * BaseEntity의 createDate 필드에 접근하여 강제로 값을 설정합니다.
     */
    private void setBaseEntityCreateDate(Object entity, LocalDateTime dateTime) {
        try {
            // Party가 BaseEntity를 상속받으므로 getSuperclass() 사용
            Field field = entity.getClass().getSuperclass().getDeclaredField("createDate");
            field.setAccessible(true);
            field.set(entity, dateTime);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // 테스트 실패로 처리
            throw new RuntimeException("Reflection 오류: createDate 필드 접근 실패", e);
        }
    }
}