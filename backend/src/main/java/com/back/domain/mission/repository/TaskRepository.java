package com.back.domain.mission.repository;

import com.back.domain.mission.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findBySubGoalId(Integer subGoalId);

    @Query("""
        SELECT t FROM Task t 
        JOIN t.subGoal sg 
        JOIN sg.mission m 
        WHERE m.member.id = :memberId 
        AND :today BETWEEN sg.startDate AND sg.endDate 
        AND t.dayNum = :dayOfWeek 
        AND m.isCompleted = false
    """)
    List<Task> findTodayTasks(
            @Param("memberId") Integer memberId,
            @Param("today") LocalDate today,
            @Param("dayOfWeek") Integer dayOfWeek
    );

    @Query("""
        SELECT t FROM Task t 
        JOIN t.subGoal sg 
        JOIN sg.mission m 
        WHERE m.member.id = :memberId 
        AND :date BETWEEN sg.startDate AND sg.endDate 
        AND t.dayNum = :dayNum 
        AND m.isCompleted = false
    """)
    List<Task> findTasksByDate(
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date,
            @Param("dayNum") Integer dayNum
    );
}