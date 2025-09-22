package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.SubGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubGoalRepository extends JpaRepository<SubGoal, Integer> {
    //특정 미션에 속한 SubGoal whghl
    List<SubGoal> findByMissionId(Integer missionId);
}
