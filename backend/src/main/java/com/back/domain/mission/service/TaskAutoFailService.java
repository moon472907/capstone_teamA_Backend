package com.back.domain.mission.service;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.entity.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskAutoFailService {

    private final TaskLogRepository taskLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processExpiredTasks(List<Task> tasks, LocalDate date) {
        List<TaskLog> failLogs = new ArrayList<>();

        for (Task task : tasks) {
            Mission mission = task.getSubGoal().getMission();
            List<Integer> memberIds = getMissionMembers(mission);

            for (Integer memberId : memberIds) {
                // 이미 완료/실패 기록이 있는지 확인
                if (!taskLogRepository.existsByTaskIdAndMemberIdAndDate(
                        task.getId(), memberId, date)) {

                    TaskLog failLog = TaskLog.builder()
                            .task(task)
                            .memberId(memberId)
                            .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                            .date(date)
                            .status(TaskStatus.SKIPPED)
                            .build();

                    failLogs.add(failLog);
                }
            }
        }

        if (!failLogs.isEmpty()) {
            taskLogRepository.saveAll(failLogs);
        }

        return failLogs.size();
    }

    //미션에 속한 모든 멤버 ID 조회
    private List<Integer> getMissionMembers(Mission mission) {
        List<Integer> memberIds = new ArrayList<>();

        if (mission.isPartyMission()) {
            mission.getParty().getPartyMembers().stream()
                    .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                    .forEach(pm -> memberIds.add(pm.getMember().getId()));
        } else {
            memberIds.add(mission.getMember().getId());
        }

        return memberIds;
    }
}