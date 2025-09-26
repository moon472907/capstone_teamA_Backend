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
}