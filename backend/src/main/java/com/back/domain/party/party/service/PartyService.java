package com.back.domain.party.party.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.repository.MissionCompletionLogRepository;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.service.MissionCalculateService;
import com.back.domain.party.party.dto.*;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import com.back.domain.party.party.repository.PartyMemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final PartyMemberRepository partyMemberRepository;
    private final MissionRepository missionRepository;
    private final MissionCompletionLogRepository completionLogRepository;
    private final MissionCalculateService missionCalculateService;

    private Integer calculateMyProgressRate(Integer partyId, Integer memberId) {
        Optional<Mission> missionOptional = missionRepository.findByPartyId(partyId).stream().findFirst();

        if (missionOptional.isEmpty()) {
            return 0; // 미션 정보가 없으면 0%
        }

        Mission mission = missionOptional.get();

        // 기존 MissionCalculateService의 메서드를 사용하여 진행률 계산 (0~100)
        return missionCalculateService.calculateMissionProgressForMember(mission, memberId);
    }

    public Page<PartyDto> getPublicPartyList(
            Pageable pageable,
            MissionCategory categoryFilter,
            LocalDate startDateFilter
    ) {
        // 1. Repository를 사용하여 페이징 및 정렬된 Party 리스트 조회 (Mission, Leader, Members 포함)
        Page<Party> partyPage = partyRepository.findPublicPartiesWithMissionAndMembers(pageable);
        List<Party> parties = partyPage.getContent();

        // 2. 파티 목록을 PartyDto로 변환하면서 Mission 정보를 매핑하고 필터링
        List<PartyDto> dtoList = parties.stream()
                .map(party -> {
                    // MissionRepository를 사용하여 파티에 연결된 Mission을 조회 (파티당 하나의 미션만 있다고 가정)
                    Optional<Mission> missionOptional = missionRepository.findByPartyId(party.getId()).stream().findFirst();

                    // Mission 정보를 포함하는 생성자로 DTO 생성
                    PartyDto dto = new PartyDto(party, missionOptional.orElse(null));

                    // 3. Service에서 필터링 조건 확인 (DB 쿼리로 처리할 수 없는 동적 필터링을 여기서 처리)
                    if (categoryFilter != null && (dto.getCategory() == null || !dto.getCategory().equals(categoryFilter))) {
                        return null; // 카테고리 필터 불일치
                    }
                    if (startDateFilter != null && (dto.getStartDate() == null || !dto.getStartDate().isEqual(startDateFilter))) {
                        return null; // 시작일 필터 불일치
                    }
                    return dto;
                })
                .filter(Objects::nonNull) // 필터링 조건에 맞지 않아 null이 된 항목 제거
                .collect(Collectors.toList());


        // 4. 필터링된 리스트를 Page 객체로 다시 변환하여 반환
        return new PageImpl<>(dtoList, pageable, partyPage.getTotalElements());
    }

    // 파티 정원 확인 메서드
    private void checkPartyCapacity(Party party) {
        long acceptedMembers = partyMemberRepository.countByParty_IdAndStatus(party.getId(), PartyMemberStatus.ACCEPTED);
        if (acceptedMembers >= party.getMaxMembers()) {
            throw new CustomException(ErrorCode.PARTY_CAPACITY_FULL);
        }
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, allEntries = true)
    public Party createParty(PartyRequestDto requestDto, Integer memberId) {
        Member leader = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        Party newParty = new Party();
        newParty.setName(requestDto.getName());
        newParty.setMaxMembers(requestDto.getMaxMembers());
        newParty.setPublic(requestDto.getIsPublicStatus());
        newParty.setLeader(leader);
        Party savedParty = partyRepository.save(newParty);

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(savedParty);
        partyMember.setMember(leader);
        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);

        return savedParty;
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails", "pendingRequests"}, allEntries = true)
    public void joinParty(Integer partyId, Integer memberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 1. 공개 파티만 가입 신청 가능
        if (!party.isPublic()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "비공개 파티는 초대로만 가입할 수 있습니다.");
        }

        // 2. 이미 가입되어 있거나 신청/초대 대기 중인지 확인 (및 REJECTED 재신청 처리)
        Optional<PartyMember> partyMemberOptional = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberId);

        if (partyMemberOptional.isPresent()) {
            PartyMember existingPartyMember = partyMemberOptional.get();
            PartyMemberStatus currentStatus = existingPartyMember.getStatus();

            // REJECTED 상태였다면 PENDING으로 재전환 허용
            if (currentStatus == PartyMemberStatus.REJECTED) {
                existingPartyMember.setStatus(PartyMemberStatus.PENDING);
                partyMemberRepository.save(existingPartyMember);
                return;
            }

            // 그 외 유효하거나 대기 중인 상태(ACCEPTED, PENDING, INVITED 등)라면 예외 처리
            throw new CustomException(ErrorCode.ALREADY_PARTY_MEMBER);
        }

        // 3. 파티의 정원이 가득 찼는지 확인
        checkPartyCapacity(party);

        // 4. PartyMember 객체 생성 및 상태를 PENDING으로 설정
        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(member);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void leaveParty(Integer partyId, Integer memberId) {
        PartyMember leavingMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND));

        Party party = leavingMember.getParty();

        // 파티장인지 확인
        if (Objects.equals(party.getLeader().getId(), memberId)) {
            // 본인을 제외한 모든 멤버를 가입일 순으로 정렬하여 조회
            List<PartyMember> otherMembers = partyMemberRepository.findByParty_Id(partyId);
            otherMembers.removeIf(pm -> Objects.equals(pm.getMember().getId(), memberId));
            otherMembers.sort(Comparator.comparing(PartyMember::getJoinedAt));

            // 남은 멤버가 있다면 새로운 파티장 위임
            if (!otherMembers.isEmpty()) {
                PartyMember newLeaderMember = otherMembers.getFirst();
                party.setLeader(newLeaderMember.getMember());
                // 파티원 목록에서 탈퇴 멤버 삭제
                partyMemberRepository.delete(leavingMember);
            } else {
                // 남은 멤버가 없다면 파티 삭제
                deleteParty(partyId, memberId);
            }
        } else {
            // 파티 멤버 목록에서 탈퇴 멤버 삭제
            partyMemberRepository.delete(leavingMember);
        }
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void updateParty(Integer partyId, PartyUpdateRequestDto requestDto, Integer memberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        // 요청한 멤버가 파티장이 맞는지 확인
        if (!Objects.equals(party.getLeader().getId(), memberId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        // DTO에서 변경된 값이 있으면 파티 엔티티에 반영
        if (requestDto.getName() != null) {
            party.setName(requestDto.getName());
        }
        if (requestDto.getMaxMembers() != null) {
            // 새로운 최대 인원이 현재 ACCEPTED 멤버 수보다 적으면 예외 발생
            long acceptedMembers = party.getPartyMembers().stream()
                    .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                    .count();
            if (requestDto.getMaxMembers() < acceptedMembers) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "새로운 최대 인원은 현재 활동 중인 멤버 수보다 적을 수 없습니다.");
            }
            party.setMaxMembers(requestDto.getMaxMembers());
        }
        if (requestDto.getIsPublicStatus() != null) {
            party.setPublic(requestDto.getIsPublicStatus());
        }
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, allEntries = true)
    public void deleteParty(Integer partyId, Integer memberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        // 요청한 멤버가 파티장이 맞는지 확인
        if (!Objects.equals(party.getLeader().getId(), memberId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        partyRepository.delete(party);
    }

    private String getMemberMissionStatus(Integer missionId, Integer memberId) {
        boolean isCompleted = completionLogRepository.existsByMissionIdAndMemberId(missionId, memberId);
        return isCompleted ? "COMPLETED" : "PROGRESS";
    }

    // 파티 상세 조회 (캐시 적용)
    @Cacheable(value = "partyDetails", key = "#partyId")
    @Transactional(readOnly = true)
    public PartyDto getPartyDetails(Integer partyId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        party.incrementViews();

        Mission mission = missionRepository.findByPartyId(partyId).stream().findFirst().orElse(null);

        // 파티원 DTO 리스트 생성 로직
        List<PartyMemberDto> memberDtos = party.getPartyMembers().stream()
                .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                .map(partyMember -> {
                    String memberMissionStatus = null;
                    if (mission != null) {
                        memberMissionStatus = getMemberMissionStatus( // 상태 계산 로직 사용
                                mission.getId(),
                                partyMember.getMember().getId()
                        );
                    }
                    return new PartyMemberDto(
                            partyMember.getMember(),
                            memberMissionStatus
                    );
                })
                .collect(Collectors.toList());

        PartyDto dto = new PartyDto(party, mission);
        dto.setMembers(memberDtos);

        return dto;
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails", "pendingRequests"}, allEntries = true)
    public PartyMemberStatusResponse inviteMember(Integer partyId, Integer leaderId, String invitedMemberCode) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        // 정원 확인
        checkPartyCapacity(party);

        // 코드로 멤버를 조회합니다.
        Member invitedMember = memberRepository.findByCode(invitedMemberCode)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_CODE));

        Optional<PartyMember> partyMemberOptional = partyMemberRepository.findByParty_IdAndMember_Id(partyId, invitedMember.getId());

        PartyMember partyMember;
        PartyMemberStatus newStatus = PartyMemberStatus.INVITED;

        if (partyMemberOptional.isPresent()) {
            partyMember = partyMemberOptional.get();
            PartyMemberStatus currentStatus = partyMember.getStatus();

            // REJECTED 상태였던 경우 INVITED로 재전환 허용
            if (currentStatus == PartyMemberStatus.REJECTED || currentStatus == PartyMemberStatus.LEFT || currentStatus == PartyMemberStatus.COMPLETED) {
                partyMember.setStatus(newStatus);
                partyMemberRepository.save(partyMember);
            } else {
                // 이미 ACCEPTED, PENDING, INVITED 등 유효한 상태면 예외 처리
                throw new CustomException(ErrorCode.ALREADY_PARTY_MEMBER);
            }
        } else {
            // 신규 초대: INVITED 상태로 생성
            partyMember = new PartyMember();
            partyMember.setParty(party);
            partyMember.setMember(invitedMember);
            partyMember.setStatus(newStatus);
            partyMember.setJoinedAt(LocalDateTime.now());
            partyMemberRepository.save(partyMember);
        }

        return new PartyMemberStatusResponse(invitedMember.getId(), newStatus.name());
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails", "pendingRequests"}, allEntries = true)
    public PartyMemberStatusResponse acceptInvitation(Integer partyId, Integer memberIdToAccept, Integer leaderId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        PartyMember partyMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberIdToAccept)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND));

        // PENDING 또는 INVITED 상태만 수락 가능
        if (partyMember.getStatus() != PartyMemberStatus.PENDING && partyMember.getStatus() != PartyMemberStatus.INVITED) {
            throw new CustomException(ErrorCode.INVALID_PARTY_MEMBER_STATUS);
        }

        // 파티 정원 확인
        checkPartyCapacity(party);

        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
        partyMemberRepository.save(partyMember);

        return new PartyMemberStatusResponse(memberIdToAccept, PartyMemberStatus.ACCEPTED.name());
    }

    @Transactional
    @CacheEvict(value = "pendingRequests", allEntries = true)
    public PartyMemberStatusResponse rejectInvitation(Integer partyId, Integer memberIdToReject, Integer leaderId) { // [memberIdToReject, leaderId 추가
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        PartyMember partyMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberIdToReject)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND));

        // PENDING 또는 INVITED 상태만 거절 가능
        if (partyMember.getStatus() != PartyMemberStatus.PENDING && partyMember.getStatus() != PartyMemberStatus.INVITED) {
            throw new CustomException(ErrorCode.INVALID_PARTY_MEMBER_STATUS);
        }

        PartyMemberStatus newStatus = PartyMemberStatus.REJECTED;
        partyMember.setStatus(newStatus);
        partyMemberRepository.save(partyMember);

        return new PartyMemberStatusResponse(memberIdToReject, newStatus.name());
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void kickMember(Integer partyId, Integer leaderId, Integer kickedMemberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        // 1. 추방을 요청한 멤버가 파티장인지 확인
        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        // 2. 파티장 자신을 추방하는 것 방지
        if (leaderId.equals(kickedMemberId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "파티장 자신은 추방할 수 없습니다.");
        }

        // 3. 추방하려는 멤버가 해당 파티에 속해 있는지 확인
        PartyMember kickedMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, kickedMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_MEMBER_NOT_FOUND));

        // 4. 멤버 상태가 ACCEPTED인지 확인
        if (kickedMember.getStatus() != PartyMemberStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.INVALID_PARTY_MEMBER_STATUS, "가입이 완료된 파티원만 추방할 수 있습니다.");
        }

        // 5. 파티원 삭제
        partyMemberRepository.delete(kickedMember);
    }

    @Cacheable(value = "pendingRequests", key = "#partyId")
    @Transactional(readOnly = true)
    public List<PartyMember> getPendingJoinRequests(Integer partyId, Integer leaderId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTY_NOT_FOUND));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.PARTY_FORBIDDEN);
        }

        // PENDING과 INVITED 상태를 동시에 조회
        return partyMemberRepository.findByParty_IdAndStatusIn(
                partyId,
                List.of(PartyMemberStatus.PENDING, PartyMemberStatus.INVITED)
        );
    }
    public Page<PartyDto> getMyPartyList(
            Integer memberId,
            String statusFilter, // "ongoing" 또는 "done"
            Pageable pageable
    ) {
        // 1. Repository를 사용하여 파티 목록 조회
        Page<Party> partyPage = partyRepository.findMyPartiesWithMissionAndMembers(memberId, pageable);
        List<Party> parties = partyPage.getContent();

        // 2. 필터링 및 DTO 변환
        List<PartyDto> dtoList = parties.stream()
                .map(party -> {
                    // 2-1. 로그인한 멤버의 파티 내 상태 (myStatus) 결정
                    PartyMemberStatus myStatus;
                    Optional<PartyMember> memberStatus = partyMemberRepository.findByParty_IdAndMember_Id(party.getId(), memberId);

                    if (memberStatus.isPresent()) {
                        myStatus = memberStatus.get().getStatus();
                    } else if (Objects.equals(party.getLeader().getId(), memberId)) {
                        // 리더인데 PartyMember 행이 없는 경우, ACCEPTED로 간주
                        myStatus = PartyMemberStatus.ACCEPTED;
                    } else {
                        return null; // DB 쿼리에서 걸러져야 하므로, 여기에 도달하면 데이터 이상
                    }

                    // 2-2. 상태 필터링 조건 확인
                    if ("ongoing".equalsIgnoreCase(statusFilter)) {
                        if (myStatus != PartyMemberStatus.ACCEPTED) {
                            return null; // ongoing 필터: 'ACCEPTED'만 포함
                        }
                    } else if ("done".equalsIgnoreCase(statusFilter)) {
                        // done 필터: 'COMPLETED' 또는 'LEFT'만 포함
                        Set<PartyMemberStatus> doneStatuses = Set.of(PartyMemberStatus.COMPLETED, PartyMemberStatus.LEFT);
                        if (!doneStatuses.contains(myStatus)) {
                            return null;
                        }
                    }

                    Integer myProgressRate = calculateMyProgressRate(party.getId(), memberId);

                    // 2-3. Mission 정보 조회 및 DTO 생성 (myStatus 포함 생성자 사용)
                    Optional<Mission> missionOptional = missionRepository.findByPartyId(party.getId()).stream().findFirst();


                    // myStatus를 포함하는 생성자를 호출하여 DTO 생성
                    PartyDto dto = new PartyDto(
                            party,
                            missionOptional.orElse(null),
                            myStatus.name(),
                            myProgressRate
                    );
                    return dto;
                })
                .filter(Objects::nonNull) // 필터링 조건에 맞지 않아 null이 된 항목 제거
                .collect(Collectors.toList());

        // 3. 필터링된 리스트를 Page 객체로 다시 변환
        return new PageImpl<>(dtoList, pageable, partyPage.getTotalElements());
    }
}