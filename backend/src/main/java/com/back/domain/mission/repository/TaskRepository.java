package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> { // Integer → Long

    // SubGoal별 태스크 조회
    List<Task> findBySubGoalId(Integer subGoalId);

    // Mission별 태스크 조회
    List<Task> findBySubGoalMissionId(Integer missionId);

    // Mission별 총 태스크 수
    Long countBySubGoalMissionId(Integer missionId);

    //오늘의 태스크 조회
    @Query("SELECT t FROM Task t " +
            "JOIN t.subGoal sg " +
            "JOIN sg.mission m " +
            "WHERE m.member.id = :memberId " +
            "AND :today BETWEEN sg.startDate AND sg.endDate " +
            "AND t.dayNum = :dayOfWeek")
    List<Task> findTodayTasks(@Param("memberId") Integer memberId,
                              @Param("today") LocalDate today,
                              @Param("dayOfWeek") Integer dayOfWeek);

    //특정 날짜의 태스크 조회
    @Query("SELECT t FROM Task t " +
            "JOIN t.subGoal sg " +
            "JOIN sg.mission m " +
            "WHERE m.member.id = :memberId " +
            "AND :date BETWEEN sg.startDate AND sg.endDate " +
            "AND t.dayNum = :dayNum " +
            "AND m.isCompleted = false")
    List<Task> findTasksByDate(@Param("memberId") Integer memberId,
                               @Param("date") LocalDate date,
                               @Param("dayNum") Integer dayNum);

    // 미션별 태스크 조회
    @Query("SELECT t FROM Task t " +
            "JOIN t.subGoal sg " +
            "WHERE sg.mission.id = :missionId " +
            "ORDER BY sg.orderNum, t.dayNum")
    List<Task> findByMissionId(@Param("missionId") Integer missionId);

    //미션별 총 태스크 수

    @Query("SELECT COUNT(t) FROM Task t " +
            "JOIN t.subGoal sg " +
            "WHERE sg.mission.id = :missionId")
    Long countByMissionId(@Param("missionId") Integer missionId);


}