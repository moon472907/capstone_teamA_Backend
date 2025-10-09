package com.back.domain.mission.repository;

import com.back.domain.mission.entity.MissionCompletionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionCompletionLogRepository extends JpaRepository<MissionCompletionLog, Integer> {

    boolean existsByMissionIdAndMemberId(Integer missionId, Integer memberId);
}