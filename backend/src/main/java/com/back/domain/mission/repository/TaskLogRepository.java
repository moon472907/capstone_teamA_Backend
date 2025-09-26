package com.back.domain.mission.repository;

import com.back.domain.mission.entity.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Integer> {

    boolean existsByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    Optional<TaskLog> findByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    Optional<TaskLog> findTopByTaskIdAndMemberIdOrderByDateDesc(Integer taskId, Integer memberId);

    Long countByMemberIdAndDateAndStatus(Integer memberId, LocalDate date, TaskStatus status);

    @Query("SELECT COUNT(DISTINCT tl.task.id) FROM TaskLog tl " +
            "JOIN tl.task t " +
            "JOIN t.subGoal sg " +
            "WHERE sg.mission.id = :missionId " +
            "AND tl.status = :status")
    Long countCompletedTasksByMission(@Param("missionId") Integer missionId,
                                      @Param("status") TaskStatus status);

    @Query("SELECT COUNT(DISTINCT tl.task.id) FROM TaskLog tl " +
            "WHERE tl.task.subGoal.id = :subGoalId " +
            "AND tl.status = :status")
    Long countCompletedTasksBySubGoal(@Param("subGoalId") Integer subGoalId,
                                      @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t " +
            "JOIN t.subGoal sg " +
            "JOIN sg.mission m " +
            "WHERE m.member.id = :memberId " +
            "AND :date BETWEEN sg.startDate AND sg.endDate " +
            "AND t.dayNum = :dayOfWeek")
    Long countDailyTasks(@Param("memberId") Integer memberId,
                         @Param("date") LocalDate date,
                         @Param("dayOfWeek") Integer dayOfWeek);
}