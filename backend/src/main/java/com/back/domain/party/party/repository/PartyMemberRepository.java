package com.back.domain.party.party.repository;

import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberId;
import com.back.domain.party.party.entity.PartyMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyMemberRepository extends JpaRepository<PartyMember, PartyMemberId> {

    // 특정 파티의 모든 파티원 관계를 찾는 메서드
    List<PartyMember> findByParty_Id(Integer partyId);

    // 특정 파티의 특정 멤버 관계를 찾는 메서드
    Optional<PartyMember> findByParty_IdAndMember_Id(Integer partyId, Integer memberId);

    // 특정 파티에 속한 특정 상태의 파티원들을 찾는 메서드
    List<PartyMember> findByParty_IdAndStatus(Integer partyId, PartyMemberStatus status);

    // 파티원 수 조회용 메서드 추가
    long countByParty_IdAndStatus(Integer partyId, PartyMemberStatus status);

}