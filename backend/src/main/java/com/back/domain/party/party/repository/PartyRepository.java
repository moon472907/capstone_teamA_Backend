package com.back.domain.party.party.repository;

import com.back.domain.party.party.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {

    @Override
    @EntityGraph(attributePaths = {"leader", "partyMembers.member"})
    Optional<Party> findById(Integer partyId);

    @Query(value = "SELECT DISTINCT p FROM Party p " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN FETCH p.partyMembers pm " +
            "LEFT JOIN FETCH pm.member mbm " +
            "LEFT JOIN Mission m ON m.party = p " +
            "WHERE p.isPublic = true",
            countQuery = "SELECT COUNT(p) FROM Party p WHERE p.isPublic = true")
    Page<Party> findPublicPartiesWithMissionAndMembers(Pageable pageable);

    @Query(value = "SELECT p FROM Party p " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN FETCH p.partyMembers pm " +
            "LEFT JOIN FETCH pm.member mbm " +
            "WHERE p.leader.id = :memberId " + // 리더인 경우
            "OR EXISTS (SELECT pm2 FROM PartyMember pm2 WHERE pm2.party = p AND pm2.member.id = :memberId)" + // 멤버인 경우
            "GROUP BY p.id, l.id, mbm.id, pm.status, pm.joinedAt",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Party p " +
                    "LEFT JOIN PartyMember pm ON pm.party = p " +
                    "WHERE p.leader.id = :memberId OR pm.member.id = :memberId")
    Page<Party> findMyPartiesWithMissionAndMembers(Integer memberId, Pageable pageable);
}