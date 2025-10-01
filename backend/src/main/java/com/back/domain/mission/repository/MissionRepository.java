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

    Long countByMemberIdAndIsCompleted(Integer memberId, boolean isCompleted);

    @Query("""
        SELECT DISTINCT m FROM Mission m
        LEFT JOIN FETCH m.party p
        WHERE m.member.id = :memberId 
        AND m.isCompleted = :isCompleted
        ORDER BY m.createDate DESC
        """)
    List<Mission> findByMemberIdAndIsCompletedWithParty(
            @Param("memberId") Integer memberId,
            @Param("isCompleted") boolean isCompleted
    );

    @Query("""
        SELECT DISTINCT m FROM Mission m
        LEFT JOIN FETCH m.subGoals sg
        WHERE m.id = :missionId
        """)
    Optional<Mission> findByIdWithSubGoals(@Param("missionId") Integer missionId);

    //쿼리 변환 이유 -> 두 개의 list를 동시에 fetch join해서 에러가 뜸 (해결 방안 : 쿼리 2단계로 분리)
    //N + 1 문제 해결하려고 병합했다가 계속 오류 발생

    @Query("""
        SELECT DISTINCT m FROM Mission m
        LEFT JOIN FETCH m.party p
        ORDER BY m.createDate DESC
        """)
    List<Mission> findAllWithParty();
}