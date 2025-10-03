package com.back.domain.mission.repository;

import com.back.domain.mission.entity.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Integer> {
    boolean existsByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    boolean existsByTaskIdAndStatus(Integer taskId, TaskStatus status);

    Long countByMemberIdAndDateAndStatus(Integer memberId, LocalDate date, TaskStatus status);

    @Query("""
        SELECT COUNT(t) 
        FROM Task t 
        WHERE t.subGoal.mission.member.id = :memberId
        AND t.dayNum = :dayOfWeek
        AND :date BETWEEN t.subGoal.startDate AND t.subGoal.endDate
    """)
    Long countDailyTasks(
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") int dayOfWeek
    );

    @Query("""
        SELECT COUNT(tl) 
        FROM TaskLog tl 
        WHERE tl.task.subGoal.mission.id = :missionId
        AND tl.memberId = :memberId
        AND tl.status = :status
    """)
    Long countCompletedTasksByMissionAndMember(
            @Param("missionId") Integer missionId,
            @Param("memberId") Integer memberId,
            @Param("status") TaskStatus status
    );

    @Query("""
        SELECT COUNT(tl) 
        FROM TaskLog tl 
        WHERE tl.task.subGoal.id = :subGoalId
        AND tl.memberId = :memberId
        AND tl.status = :status
    """)
    Long countCompletedTasksBySubGoalAndMember(
            @Param("subGoalId") Integer subGoalId,
            @Param("memberId") Integer memberId,
            @Param("status") TaskStatus status
    );

    Optional<TaskLog> findTopByTaskIdAndMemberIdOrderByDateDesc(Integer taskId, Integer memberId);

    Optional<TaskLog> findByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    @Query("SELECT tl FROM TaskLog tl " +
            "WHERE tl.task.id IN :taskIds " +
            "AND tl.memberId = :memberId " +
            "AND tl.date = :date")
    List<TaskLog> findByTaskIdsAndMemberIdAndDate(
            @Param("taskIds") List<Integer> taskIds,
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date
    );

    @Query("SELECT tl FROM TaskLog tl " +
            "WHERE tl.task.id IN :taskIds " +
            "AND tl.memberId = :memberId " +
            "AND tl.id IN (" +
            "    SELECT MAX(tl2.id) FROM TaskLog tl2 " +
            "    WHERE tl2.task.id = tl.task.id " +
            "    AND tl2.memberId = :memberId" +
            ")")
    List<TaskLog> findLastCompletedByTaskIds(
            @Param("taskIds") List<Integer> taskIds,
            @Param("memberId") Integer memberId
    );

    boolean existsByTaskIdAndMemberIdAndDateAndStatus(
            Integer taskId,
            Integer memberId,
            LocalDate date,
            TaskStatus status
    );
}