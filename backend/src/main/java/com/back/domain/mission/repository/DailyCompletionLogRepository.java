package com.back.domain.mission.repository;

import com.back.domain.mission.entity.DailyCompletionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyCompletionLogRepository extends JpaRepository<DailyCompletionLog, Integer> {

    boolean existsByMemberIdAndCompletedDate(Integer memberId, LocalDate date);

    Optional<DailyCompletionLog> findByMemberIdAndCompletedDate(Integer memberId, LocalDate date);

    Long countByMemberId(Integer memberId);

    List<DailyCompletionLog> findByMemberIdOrderByCompletedDateDesc(Integer memberId);
}