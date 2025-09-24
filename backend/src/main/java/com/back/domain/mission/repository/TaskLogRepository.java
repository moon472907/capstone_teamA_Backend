package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Integer> { // Integer → Long

    List<TaskLog> findByTaskId(Integer taskId);
    List<TaskLog> findByMemberId(Integer memberId);
    List<TaskLog> findByMemberIdAndDate(Integer memberId, LocalDate date);
    List<TaskLog> findByMemberIdAndDateBetween(Integer memberId, LocalDate startDate, LocalDate endDate);
    Long countCompletedTasksByDate(LocalDate date);

    //특정 날짜에 이미 완료했는지 체크
    boolean existsByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    // 최근 완료 기록
    Optional<TaskLog> findTopByTaskIdAndMemberIdOrderByDateDesc(Integer taskId, Integer memberId);

    // 특정 태스크 & 날짜 로그
    Optional<TaskLog> findByTaskIdAndMemberIdAndDate(Integer taskId, Integer memberId, LocalDate date);

    // 완료 횟수
    Long countByTaskIdAndStatus(Integer taskId, TaskStatus status);

    // 특정 날짜의 완료된 태스크 수 (회원 + 날짜 + 상태)
    Long countByMemberIdAndDateAndStatus(Integer memberId, LocalDate date, TaskStatus status);

    //특정 미션의 완료된 태스크 수 (JOIN 필요)
    @Query("SELECT COUNT(DISTINCT tl.task.id) FROM TaskLog tl " +
            "JOIN tl.task t " +
            "JOIN t.subGoal sg " +
            "WHERE sg.mission.id = :missionId " +
            "AND tl.status = :status")
    Long countCompletedTasksByMission(@Param("missionId") Integer missionId,
                                      @Param("status") TaskStatus status);

    // 특정 SubGoal의 완료된 태스크 수
    @Query("SELECT COUNT(DISTINCT tl.task.id) FROM TaskLog tl " +
            "WHERE tl.task.subGoal.id = :subGoalId " +
            "AND tl.status = :status")
    Long countCompletedTasksBySubGoal(@Param("subGoalId") Integer subGoalId,
                                      @Param("status") TaskStatus status);

    // 해당 날짜에 수행해야 할 태스크 개수
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