package com.back.domain.mission.repository;

import com.back.domain.mission.entity.SubGoalCompletionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubGoalCompletionLogRepository extends JpaRepository<SubGoalCompletionLog, Integer> {

    boolean existsBySubGoalIdAndMemberId(Integer subGoalId, Integer memberId);

    Optional<SubGoalCompletionLog> findBySubGoalIdAndMemberId(Integer subGoalId, Integer memberId);

    Long countByMemberId(Integer memberId);
}