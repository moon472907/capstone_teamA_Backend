package com.back.domain.party.party.repository;

import com.back.domain.party.party.entity.Party;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Integer> {

    @Override
    @EntityGraph(attributePaths = {
            "leader",
            "partyMembers.member",
            "partyMembers.member.title",
            "partyMembers.member.item"
    })
    Optional<Party> findById(Integer partyId);

    @Query(value = "SELECT DISTINCT p FROM Party p " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN FETCH p.partyMembers pm " +
            "LEFT JOIN FETCH pm.member mbm " +
            "LEFT JOIN FETCH mbm.title t " +
            "LEFT JOIN FETCH mbm.item i " +
            "LEFT JOIN Mission m ON m.party = p " +
            "WHERE p.isPublic = true",
            countQuery = "SELECT COUNT(p) FROM Party p WHERE p.isPublic = true")
    Page<Party> findPublicPartiesWithMissionAndMembers(Pageable pageable);

    @Query(value = "SELECT p FROM Party p " +
            "LEFT JOIN FETCH p.leader l " +
            "LEFT JOIN FETCH p.partyMembers pm " +
            "LEFT JOIN FETCH pm.member mbm " +
            "LEFT JOIN FETCH mbm.title t " +
            "LEFT JOIN FETCH mbm.item i " +
            "WHERE p.leader.id = :memberId " +
            "OR EXISTS (SELECT pm2 FROM PartyMember pm2 WHERE pm2.party = p AND pm2.member.id = :memberId)" +
            "GROUP BY p.id, l.id, mbm.id, pm.status, pm.joinedAt, t.id, i.id",
            countQuery = "SELECT COUNT(DISTINCT p) FROM Party p " +
                    "LEFT JOIN PartyMember pm ON pm.party = p " +
                    "WHERE p.leader.id = :memberId OR pm.member.id = :memberId")
    Page<Party> findMyPartiesWithMissionAndMembers(@Param("memberId") Integer memberId, Pageable pageable);
}