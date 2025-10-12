package com.back.domain.statistics.repository;

import com.back.domain.statistics.entity.MemberStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberStatisticsRepository extends JpaRepository<MemberStatistics, Integer> {

    Optional<MemberStatistics> findByMemberId(Integer memberId);
}