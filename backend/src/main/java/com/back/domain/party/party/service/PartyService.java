package com.back.domain.party.party.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.party.party.dto.PartyRequestDto;
import com.back.domain.party.party.dto.PartyUpdateRequestDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyService {

    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final PartyMemberRepository partyMemberRepository;

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
    @CacheEvict(value = {"partyList", "partyDetails"}, allEntries = true)
    public void joinParty(Integer partyId, Integer memberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 1. 공개 파티만 가입 신청 가능
        if (!party.isPublic()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "비공개 파티는 초대로만 가입할 수 있습니다.");
        }

        // 2. 이미 가입되어 있거나 신청/초대 대기 중인지 확인
        if (partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberId).isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 가입되어 있거나 신청/초대 대기 중인 파티입니다.");
        }

        // 3. 파티의 정원이 가득 찼는지 확인
        long acceptedMembers = partyMemberRepository.countByParty_IdAndStatus(partyId, PartyMemberStatus.ACCEPTED);
        if (acceptedMembers >= party.getMaxMembers()) {
            throw new CustomException(ErrorCode.CONFLICT, "파티의 정원이 가득 찼습니다.");
        }

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
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

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
            } else {
                // 남은 멤버가 없다면 파티 삭제
                partyRepository.delete(party);
            }
        }

        // 파티 멤버 목록에서 탈퇴 멤버 삭제
        partyMemberRepository.delete(leavingMember);
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void updateParty(Integer partyId, PartyUpdateRequestDto requestDto, Integer memberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 요청한 멤버가 파티장이 맞는지 확인
        if (!Objects.equals(party.getLeader().getId(), memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "파티 수정 권한이 없습니다.");
        }

        // DTO에서 변경된 값이 있으면 파티 엔티티에 반영
        if (requestDto.getName() != null) {
            party.setName(requestDto.getName());
        }
        if (requestDto.getMaxMembers() != null) {
            // 새로운 최대 인원이 현재 멤버 수보다 적으면 예외 발생
            if (requestDto.getMaxMembers() < party.getPartyMembers().size()) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "새로운 최대 인원은 현재 멤버 수보다 적을 수 없습니다.");
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
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 요청한 멤버가 파티장이 맞는지 확인
        if (!Objects.equals(party.getLeader().getId(), memberId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "파티 삭제 권한이 없습니다.");
        }

        // 파티에 속한 모든 멤버 관계를 먼저 삭제
        List<PartyMember> partyMembers = partyMemberRepository.findByParty_Id(partyId);
        partyMemberRepository.deleteAll(partyMembers);

        // 파티 삭제
        partyRepository.deleteById(partyId);
    }

    @Cacheable(value = "partyList", key = "'all'")
    @Transactional(readOnly = true)
    public List<Party> getPartyList() {
        return partyRepository.findByIsPublic(true);
    }

    @Cacheable(value = "partyDetails", key = "#partyId")
    @Transactional(readOnly = true)
    public Party getPartyDetails(Integer partyId) {
        return partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 파티를 찾을 수 없습니다."));
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void inviteMember(Integer partyId, Integer leaderId, String invitedMemberCode) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "파티 초대 권한이 없습니다.");
        }

        // 정원 확인 로직을 개선된 쿼리로 변경
        long acceptedMembers = partyMemberRepository.countByParty_IdAndStatus(partyId, PartyMemberStatus.ACCEPTED);
        if (acceptedMembers >= party.getMaxMembers()) {
            throw new CustomException(ErrorCode.CONFLICT, "파티의 정원이 가득 찼습니다.");
        }

        // 코드로 멤버를 조회합니다.
        Member invitedMember = memberRepository.findByCode(invitedMemberCode)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 코드를 가진 멤버를 찾을 수 없습니다."));

        if (partyMemberRepository.findByParty_IdAndMember_Id(partyId, invitedMember.getId()).isPresent()) {
            throw new CustomException(ErrorCode.CONFLICT, "이미 파티에 가입되어 있거나 초대 대기 중인 멤버입니다.");
        }

        PartyMember partyMember = new PartyMember();
        partyMember.setParty(party);
        partyMember.setMember(invitedMember);
        partyMember.setStatus(PartyMemberStatus.PENDING);
        partyMember.setJoinedAt(LocalDateTime.now());
        partyMemberRepository.save(partyMember);
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void acceptInvitation(Integer partyId, Integer memberId) {
        PartyMember partyMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "초대 정보를 찾을 수 없습니다."));

        if (partyMember.getStatus() != PartyMemberStatus.PENDING) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "초대 대기 상태가 아닙니다.");
        }

        // 파티 정원 확인
        long acceptedMembers = partyMemberRepository.countByParty_IdAndStatus(partyMember.getParty().getId(), PartyMemberStatus.ACCEPTED);
        if (acceptedMembers >= partyMember.getParty().getMaxMembers()) {
            throw new CustomException(ErrorCode.CONFLICT, "파티의 정원이 가득 찼습니다.");
        }

        partyMember.setStatus(PartyMemberStatus.ACCEPTED);
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void rejectInvitation(Integer partyId, Integer memberId) {
        PartyMember partyMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "초대 정보를 찾을 수 없습니다."));

        if (partyMember.getStatus() != PartyMemberStatus.PENDING) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "초대 대기 상태가 아닙니다.");
        }

        partyMemberRepository.delete(partyMember);
    }

    @Transactional
    @CacheEvict(value = {"partyList", "partyDetails"}, key = "#partyId")
    public void kickMember(Integer partyId, Integer leaderId, Integer kickedMemberId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다."));

        // 1. 추방을 요청한 멤버가 파티장인지 확인
        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "파티원 추방 권한이 없습니다.");
        }

        // 2. 파티장 자신을 추방하는 것 방지
        if (leaderId.equals(kickedMemberId)) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "파티장 자신은 추방할 수 없습니다.");
        }

        // 3. 추방하려는 멤버가 해당 파티에 속해 있는지 확인
        PartyMember kickedMember = partyMemberRepository.findByParty_IdAndMember_Id(partyId, kickedMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "해당 파티원을 찾을 수 없습니다."));

        // 4. 멤버 상태가 ACCEPTED인지 확인
        if (kickedMember.getStatus() != PartyMemberStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "가입이 완료된 파티원만 추방할 수 있습니다.");
        }

        // 5. 파티원 삭제
        partyMemberRepository.delete(kickedMember);
    }

    @Cacheable(value = "pendingRequests", key = "#partyId")
    @Transactional(readOnly = true)
    public List<PartyMember> getPendingJoinRequests(Integer partyId, Integer leaderId) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다."));

        if (!Objects.equals(party.getLeader().getId(), leaderId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "권한이 없습니다.");
        }

        return partyMemberRepository.findByParty_IdAndStatus(partyId, PartyMemberStatus.PENDING);
    }
}