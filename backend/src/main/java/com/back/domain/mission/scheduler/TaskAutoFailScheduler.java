package com.back.domain.mission.scheduler;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.Task;
import com.back.domain.mission.entity.TaskLog;
import com.back.domain.mission.enums.TaskStatus;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.mission.repository.TaskRepository;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAutoFailScheduler {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;

    /**
     * 매일 자정 1분에 실행
     * 어제 날짜의 미완료 Task를 자동으로 FAILED 처리
     */
    @Scheduled(cron = "0 1 0 * * *")
    @Transactional
    public void autoFailExpiredTasks() {
        log.info("자동 실패 처리 스케줄러 시작 ");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        int yesterdayDayOfWeek = yesterday.getDayOfWeek().getValue();

        log.info("처리 대상 날짜: {}, 요일: {}", yesterday, yesterdayDayOfWeek);

        // 어제 해야 했던 모든 Task 조회
        List<Task> expiredTasks = taskRepository.findExpiredTasks(yesterday, yesterdayDayOfWeek);
        log.info("만료된 Task 개수: {}", expiredTasks.size());

        int failedCount = 0;

        for (Task task : expiredTasks) {
            Mission mission = task.getSubGoal().getMission();

            // 해당 미션의 모든 멤버 조회
            List<Integer> memberIds = getMissionMembers(mission);

            for (Integer memberId : memberIds) {
                // 이미 완료/실패 기록이 있는지 확인
                if (!taskLogRepository.existsByTaskIdAndMemberIdAndDate(
                        task.getId(), memberId, yesterday)) {

                    // 자동 실패 처리
                    TaskLog failLog = TaskLog.builder()
                            .task(task)
                            .memberId(memberId)
                            .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                            .date(yesterday)
                            .status(TaskStatus.SKIPPED)
                            .build();

                    taskLogRepository.save(failLog);
                    failedCount++;

                    log.debug("자동 실패 처리: TaskId={}, MemberId={}, Date={}",
                            task.getId(), memberId, yesterday);
                }
            }
        }

        log.info("자동 실패 처리 완료: {}건", failedCount);
        log.info("자동 실패 처리 스케줄러 종료");
    }

    /*
     미션에 속한 모든 멤버 ID 조회
     - 개인 미션: 생성자 1명
     - 파티 미션: ACCEPTED 상태의 모든 파티원
     */
    private List<Integer> getMissionMembers(Mission mission) {
        List<Integer> memberIds = new ArrayList<>();

        if (mission.isPartyMission()) {
            // 파티 미션: ACCEPTED 멤버들
            mission.getParty().getPartyMembers().stream()
                    .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                    .forEach(pm -> memberIds.add(pm.getMember().getId()));
        } else {
            // 개인 미션: 생성자만
            memberIds.add(mission.getMember().getId());
        }

        return memberIds;
    }
}