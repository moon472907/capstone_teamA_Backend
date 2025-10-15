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
    Optional<TaskLog> findByTaskIdAndMemberIdAndDate(
            Integer taskId, Integer memberId, LocalDate date);

    boolean existsByTaskIdAndMemberIdAndDate(
            Integer taskId, Integer memberId, LocalDate date);

    Optional<TaskLog> findTopByTaskIdAndMemberIdOrderByDateDesc(
            Integer taskId, Integer memberId);

    // 배치 조회 ( N+1 방지)
    @Query("SELECT tl FROM TaskLog tl WHERE tl.task.id IN :taskIds " +
            "AND tl.memberId = :memberId AND tl.date = :date")
    List<TaskLog> findByTaskIdsAndMemberIdAndDate(
            @Param("taskIds") List<Integer> taskIds,
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date);

    @Query("SELECT tl FROM TaskLog tl WHERE tl.task.id IN :taskIds " +
            "AND tl.memberId = :memberId " +
            "AND tl.date = (SELECT MAX(tl2.date) FROM TaskLog tl2 " +
            "WHERE tl2.task.id = tl.task.id AND tl2.memberId = :memberId)")
    List<TaskLog> findLastCompletedByTaskIds(
            @Param("taskIds") List<Integer> taskIds,
            @Param("memberId") Integer memberId);

    // N + 1 방지
    @Query("SELECT COUNT(DISTINCT tl.memberId) FROM TaskLog tl " +
            "WHERE tl.task.id = :taskId AND tl.date = :date " +
            "AND tl.status = :status AND tl.memberId IN :memberIds")
    Long countCompletedMembers(
            @Param("taskId") Integer taskId,
            @Param("date") LocalDate date,
            @Param("status") TaskStatus status,
            @Param("memberIds") List<Integer> memberIds);

    // 일일 진행률 곗ㄴ
    @Query("SELECT COUNT(DISTINCT t.id) FROM Task t " +
            "JOIN t.subGoal sg " +
            "JOIN sg.mission m " +
            "LEFT JOIN m.party p " +
            "LEFT JOIN p.partyMembers pm " +
            "WHERE t.dayNum = :dayNum " +
            "AND :date BETWEEN sg.startDate AND sg.endDate " +
            "AND m.isCompleted = false " +
            "AND (" +
            "    (m.party IS NULL AND m.member.id = :memberId) OR " +  // 개인 미션
            "    (m.party IS NOT NULL AND pm.member.id = :memberId AND pm.status = 'ACCEPTED')" +  // 파티 미션
            ")")
    Long countDailyTasks(
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date,
            @Param("dayNum") Integer dayNum);

    @Query("SELECT COUNT(tl) FROM TaskLog tl " +
            "WHERE tl.memberId = :memberId " +
            "AND tl.date = :date " +
            "AND tl.status = :status")
    Long countByMemberIdAndDateAndStatus(
            @Param("memberId") Integer memberId,
            @Param("date") LocalDate date,
            @Param("status") TaskStatus status);

    // 완료된 태스크 수 조회 (미션 진행률용)
    @Query("SELECT COUNT(tl) FROM TaskLog tl " +
            "WHERE tl.task.subGoal.mission.id = :missionId " +
            "AND tl.memberId = :memberId " +
            "AND tl.status = :status")
    Long countCompletedTasksByMissionAndMember(
            @Param("missionId") Integer missionId,
            @Param("memberId") Integer memberId,
            @Param("status") TaskStatus status);

    // 주차별 완료된 태스크 수
    @Query("SELECT COUNT(tl) FROM TaskLog tl " +
            "WHERE tl.task.subGoal.id = :subGoalId " +
            "AND tl.memberId = :memberId " +
            "AND tl.status = :status")
    Long countCompletedTasksBySubGoalAndMember(
            @Param("subGoalId") Integer subGoalId,
            @Param("memberId") Integer memberId,
            @Param("status") TaskStatus status);


    boolean existsByTaskIdAndStatus(Integer taskId, TaskStatus status);
}