package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer> {
    List<Mission> findByMemberId(Integer memberId);
    Long countByMemberIdAndIsCompleted(Integer memberId, boolean isCompleted);

}
