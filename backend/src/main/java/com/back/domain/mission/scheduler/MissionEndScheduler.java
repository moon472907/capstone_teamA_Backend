package com.back.domain.mission.scheduler;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.service.MissionCompletionService;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionEndScheduler {

    private final MissionRepository missionRepository;
    private final MissionCompletionService missionCompletionService;

    // 매일 자정 5분에 실행
    @Scheduled(cron = "0 5 0 * * *")
    public void completeEndedMissions() {
        log.info("미션 종료 처리 스케줄러 시작");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Mission> endedMissions = missionRepository.findByEndDate(yesterday);

        if (endedMissions.isEmpty()) {
            log.info("어제 종료된 미션 없음: {}", yesterday);
            return;
        }

        log.info("종료된 미션 {}개 발견", endedMissions.size());

        int totalChecked = 0;

        for (Mission mission : endedMissions) {
            try {
                // 개인 미션
                if (!mission.isPartyMission()) {
                    log.info("미션 ID {}: 개인 미션 최종 체크", mission.getId());

                    // 마지막 완료 체크 (100%면 이벤트 발행)
                    missionCompletionService.checkAndCompleteMission(
                            mission.getId(),
                            mission.getMember().getId()
                    );
                    totalChecked++;
                }
                // 파티 미션
                else {
                    List<PartyMember> activeMembers = mission.getParty().getPartyMembers().stream()
                            .filter(pm -> pm.getStatus() == PartyMemberStatus.ACCEPTED)
                            .toList();

                    log.info("미션 ID {}: 파티원 {}명 체크 시작", mission.getId(), activeMembers.size());

                    for (PartyMember pm : activeMembers) {
                        missionCompletionService.checkAndCompleteMission(
                                mission.getId(),
                                pm.getMember().getId()
                        );
                        totalChecked++;
                    }
                }

                // 미션 자체는 무조건 종료 처리
                if (!mission.isCompleted()) {
                    mission.setCompleted(true);
                    log.info("미션 종료 처리: missionId={}", mission.getId());
                }

            } catch (Exception e) {
                log.error("미션 종료 처리 중 오류 발생: missionId={}", mission.getId(), e);
            }
        }

        log.info("미션 종료 처리 완료: {}개 미션, {}명 체크", endedMissions.size(), totalChecked);
    }
}