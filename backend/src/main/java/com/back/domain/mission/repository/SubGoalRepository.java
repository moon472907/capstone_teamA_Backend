package com.back.domain.mission.repository;

import com.back.domain.mission.entity.SubGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubGoalRepository extends JpaRepository<SubGoal, Integer> {

    List<SubGoal> findByMissionId(Integer missionId);

    List<SubGoal> findByMissionIdOrderByOrderNum(Integer missionId);
}
