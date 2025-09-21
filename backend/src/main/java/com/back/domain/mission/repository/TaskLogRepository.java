package com.back.domain.mission.repository;

import com.back.domain.mission.entitiy.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskLogRepository extends JpaRepository<TaskLog, Integer> {
    List<TaskLog> findByTaskId(Integer taskId);
    List<TaskLog> findByMemberId(Integer memberId );
    List<TaskLog> findByTaskAndMemberId(Integer taskId, Integer memberId);
}
