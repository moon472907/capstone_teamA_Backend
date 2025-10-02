package com.back.domain.mission.repository;

import com.back.domain.mission.entity.SubGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface SubGoalRepository extends JpaRepository<SubGoal, Integer> {

    List<SubGoal> findByMissionId(Integer missionId);

    List<SubGoal> findByMissionIdOrderByOrderNum(Integer missionId);

    // 2단계: SubGoals의 Tasks 조회
    @Query("""
        SELECT DISTINCT sg FROM SubGoal sg
        LEFT JOIN FETCH sg.tasks
        WHERE sg.mission.id = :missionId
        """)
    List<SubGoal> findByMissionIdWithTasks(@Param("missionId") Integer missionId);
}
