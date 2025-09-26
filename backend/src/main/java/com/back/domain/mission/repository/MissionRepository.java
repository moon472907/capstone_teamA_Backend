package com.back.domain.mission.repository;

import com.back.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer> {

    List<Mission> findByMemberId(Integer memberId);

    List<Mission> findByMemberIdAndIsCompleted(Integer memberId, Boolean isCompleted);

    Long countByMemberIdAndIsCompleted(Integer memberId, Boolean isCompleted);

    @Query("SELECT m FROM Mission m WHERE m.party.id = :partyId")
    List<Mission> findByPartyId(@Param("partyId") Integer partyId);
}