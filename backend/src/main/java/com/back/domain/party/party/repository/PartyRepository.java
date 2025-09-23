package com.back.domain.party.party.repository;

import com.back.domain.party.party.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {

    // 파티 이름으로 파티를 찾는 메서드
    Optional<Party> findByName(String name);

    // 파티장이 특정 멤버인 파티를 찾는 메서드
    Optional<Party> findByLeader_Id(Integer leaderId);

    // 공개 파티를 조회하는 메서드
    List<Party> findByIsPublic(boolean isPublic);

    // 특정 미션을 진행하는 파티를 찾는 메서드 (미션 도메인 완성 시 사용)
    // Optional<Party> findByMission_Id(Integer missionId);
}