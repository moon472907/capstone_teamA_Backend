package com.back.domain.mission.repository;

import com.back.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer> {

    List<Mission> findByMemberId(Integer memberId);

    List<Mission> findByMemberIdAndIsCompleted(Integer memberId, Boolean isCompleted);

    Long countByMemberIdAndIsCompleted(Integer memberId, Boolean isCompleted);

    @Query("SELECT m FROM Mission m WHERE m.party.id = :partyId")
    List<Mission> findByPartyId(@Param("partyId") Integer partyId);

    @Query("SELECT DISTINCT m FROM Mission m " +
            "LEFT JOIN FETCH m.member " +
            "LEFT JOIN FETCH m.subGoals sg " +
            "LEFT JOIN FETCH sg.tasks " +
            "WHERE m.id = :missionId")
    Optional<Mission> findByIdWithDetails(@Param("missionId") Integer missionId);

    @Query("SELECT DISTINCT m FROM Mission m " +
            "LEFT JOIN FETCH m.member " +
            "LEFT JOIN FETCH m.party p " +
            "LEFT JOIN FETCH p.partyMembers pm " +
            "LEFT JOIN FETCH pm.member " +
            "LEFT JOIN FETCH m.subGoals sg " +
            "LEFT JOIN FETCH sg.tasks " +
            "WHERE m.id = :missionId")
    Optional<Mission> findByIdWithDetailsAndParty(@Param("missionId") Integer missionId);

    @Query("SELECT DISTINCT m FROM Mission m " +
            "LEFT JOIN FETCH m.member " +
            "LEFT JOIN FETCH m.party " +
            "WHERE m.member.id = :memberId AND m.isCompleted = :isCompleted")
    List<Mission> findByMemberIdAndIsCompletedWithParty(
            @Param("memberId") Integer memberId,
            @Param("isCompleted") boolean isCompleted
    );
}