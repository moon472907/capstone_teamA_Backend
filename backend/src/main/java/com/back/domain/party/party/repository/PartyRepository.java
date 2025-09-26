package com.back.domain.party.party.repository;

import com.back.domain.party.party.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @EntityGraph(attributePaths = "leader")
    List<Party> findByIsPublic(boolean isPublic);

    @Override
    @EntityGraph(attributePaths = {"leader", "partyMembers.member"})
    Optional<Party> findById(Integer partyId);

    @Query(value = "SELECT DISTINCT p FROM Party p " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN p.partyMembers pm " +
            "LEFT JOIN Mission m ON m.party = p " +
            "WHERE p.isPublic = true",
            countQuery = "SELECT COUNT(p) FROM Party p WHERE p.isPublic = true")
    Page<Party> findPublicPartiesWithMissionAndMembers(Pageable pageable);
}