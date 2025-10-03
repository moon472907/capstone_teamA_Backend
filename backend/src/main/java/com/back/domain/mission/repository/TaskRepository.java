package com.back.domain.mission.repository;

import com.back.domain.mission.entity.Task;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
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
    SELECT DISTINCT t FROM Task t
    JOIN FETCH t.subGoal sg
    JOIN FETCH sg.mission m
    LEFT JOIN m.party p
    LEFT JOIN p.partyMembers pm
    WHERE (m.member.id = :memberId 
           OR (pm.member.id = :memberId AND pm.status = 'ACCEPTED'))
    AND m.isCompleted = false
    AND t.dayNum = :dayNum
    AND :date BETWEEN sg.startDate AND sg.endDate
    AND :date BETWEEN m.startDate AND m.endDate
    ORDER BY m.id, sg.orderNum, t.dayNum
    """)
    List<Task> findTodayTasks(
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date,
            @Param("dayNum") int dayNum
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

    // 특정 날짜에 해야했던 모든 task 조회 자동 실패 처리용 )
    @Query("""
        SELECT t 
        FROM Task t 
        JOIN t.subGoal sg 
        JOIN sg.mission m 
        WHERE t.dayNum = :dayOfWeek
        AND :date BETWEEN sg.startDate AND sg.endDate
        AND m.isCompleted = false
    """)
    List<Task> findExpiredTasks(
            @Param("date") LocalDate date,
            @Param("dayOfWeek") int dayOfWeek
    );

    //페이징 관련
    @Query("SELECT t FROM Task t " +
            "JOIN FETCH t.subGoal sg " +
            "JOIN FETCH sg.mission m " +
            "WHERE t.dayNum = :dayOfWeek " +
            "AND :date BETWEEN sg.startDate AND sg.endDate " +
            "AND :date BETWEEN m.startDate AND m.endDate")
    Slice<Task> findExpiredTasksSlice(
            @Param("date") LocalDate date,
            @Param("dayOfWeek") int dayOfWeek,
            PageRequest pageRequest
    );

}