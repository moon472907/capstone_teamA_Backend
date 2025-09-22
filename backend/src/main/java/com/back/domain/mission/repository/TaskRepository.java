package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> { // Integer → Long

    // SubGoal별 태스크 조회
    List<Task> findBySubGoalId(Integer subGoalId);

    // SubGoal별 태스크를 dayNum 순서로 조회
    List<Task> findBySubGoalIdOrderByDayNumAscOrderNumAsc(Integer subGoalId);

    // 특정 요일의 태스크들 조회
    List<Task> findBySubGoal_MissionIdAndDayNum(Integer missionId, Integer dayNum);
}