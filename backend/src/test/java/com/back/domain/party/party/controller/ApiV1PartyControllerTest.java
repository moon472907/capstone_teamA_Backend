package com.back.domain.party.party.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.repository.MemberRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    private ObjectMapper objectMapper;

    private Member leader;
    private Member member1;
    private Member invitedMember;

    private final String uniqueEmailPrefix = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        partyMemberRepository.deleteAllInBatch();
        partyRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        leader = Member.builder()
                .email(uniqueEmailPrefix + "leader@test.com")
                .password("password")
                .name("리더")
                .birth(LocalDate.of(1995, 1, 1)) // Changed from .age(30)
                .gender(MemberGender.MALE)
                .build();
        memberRepository.save(leader);

        member1 = Member.builder()
                .email(uniqueEmailPrefix + "member1@test.com")
                .password("password")
                .name("멤버1")
                .birth(LocalDate.of(2000, 5, 10)) // Changed from .age(25)
                .gender(MemberGender.FEMALE)
                .build();
        memberRepository.save(member1);

        invitedMember = Member.builder()
                .email(uniqueEmailPrefix + "invited@test.com")
                .password("password")
                .name("초대받은사람")
                .birth(LocalDate.of(1997, 3, 22)) // Changed from .age(28)
                .gender(MemberGender.MALE)
                .build();
        memberRepository.save(invitedMember);
    }

    @AfterEach
    void tearDown() {
        partyMemberRepository.deleteAllInBatch();
        partyRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("파티 생성 통합 테스트 성공")
    void createParty_success_integration() throws Exception {
        PartyRequestDto requestDto = new PartyRequestDto();
        requestDto.setName("통합 테스트 파티");
        requestDto.setMaxMembers(5);
        requestDto.setIsPublicStatus(true);

        mockMvc.perform(post("/api/v1/parties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .param("memberId", String.valueOf(leader.getId())))
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
                        .param("memberId", String.valueOf(member1.getId())))
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

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(delete("/api/v1/parties/{partyId}/leave", party.getId())
                        .param("memberId", String.valueOf(member1.getId())))
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .param("memberId", String.valueOf(leader.getId())))
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
                        .param("memberId", String.valueOf(leader.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 삭제 성공"));
    }

    @Test
    @DisplayName("파티 목록 조회 통합 테스트 성공")
    void getPartyList_success_integration() throws Exception {
        Party party1 = new Party();
        party1.setName("파티1");
        party1.setLeader(leader);
        party1.setMaxMembers(5);
        party1.setPublic(true);
        partyRepository.save(party1);

        Party party2 = new Party();
        party2.setName("파티2");
        party2.setLeader(leader);
        party2.setMaxMembers(5);
        party2.setPublic(true);
        partyRepository.save(party2);

        mockMvc.perform(get("/api/v1/parties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(2))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("파티 목록 조회 성공"));
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
        invitationDto.setInvitedMemberEmail(invitedMember.getEmail());

        mockMvc.perform(post("/api/v1/parties/{partyId}/invite", party.getId())
                        .param("leaderId", String.valueOf(leader.getId()))
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
                        .param("memberId", String.valueOf(member1.getId())))
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
                        .param("memberId", String.valueOf(member1.getId())))
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

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(delete("/api/v1/parties/{partyId}/members/{kickedMemberId}", party.getId(), member1.getId())
                        .param("leaderId", String.valueOf(leader.getId())))
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

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member1);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        mockMvc.perform(get("/api/v1/parties/{partyId}/requests", party.getId())
                        .param("leaderId", String.valueOf(leader.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("멤버1"))
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("가입 신청/초대 목록 조회 성공"));
    }
}