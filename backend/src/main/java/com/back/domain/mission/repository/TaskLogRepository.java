package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> { // Integer → Long

    List<TaskLog> findByTaskId(Integer taskId);
    List<TaskLog> findByMemberId(Integer memberId);

    // 수정: 메서드명과 매개변수 타입 수정
    List<TaskLog> findByTaskIdAndMemberId(Integer taskId, Integer memberId);

    // 특정 날짜의 완료 기록 조회
    List<TaskLog> findByMemberIdAndDate(Integer memberId, LocalDate date);

    // 특정 기간의 완료 기록 조회
    List<TaskLog> findByMemberIdAndDateBetween(Integer memberId, LocalDate startDate, LocalDate endDate);
}