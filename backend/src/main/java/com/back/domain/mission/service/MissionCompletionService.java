package com.back.domain.mission.service;

import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.entity.MissionCompletionLog;
import com.back.domain.mission.event.MissionCompletedEvent;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionCompletionLogRepository;
import com.back.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MissionCompletionService {

    private final MissionRepository missionRepository;
    private final MissionCalculateService missionCalculateService;
    private final ApplicationEventPublisher eventPublisher;
    private final MissionCompletionLogRepository completionLogRepository;

    @Transactional
    public void checkAndCompleteMission(Integer missionId, Integer memberId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        int memberProgress = missionCalculateService.calculateMissionProgressForMember(
                mission, memberId);

        // 개인이 100% 달성하면 이벤트 발행 (개인/파티 미션 동일)
        if (memberProgress >= 100) {
            // 개인 미션
            if (!mission.isPartyMission()) {
                if (!mission.isCompleted()) {  // 개인 미션은 한 번만
                    mission.setCompleted(true);
                    publishCompletionEvent(mission, memberId);
                    saveCompletionLog(mission.getId(), memberId);
                }
            }
            // 파티 미션
            else {
                if (!hasAlreadyCompleted(mission.getId(), memberId)) {  // 중복 체크
                    publishCompletionEvent(mission, memberId);
                    saveCompletionLog(mission.getId(), memberId);
                }
            }
        }
    }

    private void publishCompletionEvent(Mission mission, Integer memberId) {
        eventPublisher.publishEvent(MissionCompletedEvent.builder()
                .missionId(mission.getId())
                .memberId(memberId)
                .isPartyMission(mission.isPartyMission())
                .partyId(mission.isPartyMission() ? mission.getParty().getId() : null)
                .completedDate(LocalDate.now())
                .build());
    }

    // 중복 체크 구현
    private boolean hasAlreadyCompleted(Integer missionId, Integer memberId) {
        return completionLogRepository.existsByMissionIdAndMemberId(missionId, memberId);
    }

    // 완료 로그 저장
    private void saveCompletionLog(Integer missionId, Integer memberId) {
        MissionCompletionLog log = MissionCompletionLog.builder()
                .missionId(missionId)
                .memberId(memberId)
                .completedDate(LocalDate.now())
                .build();
        completionLogRepository.save(log);
    }
}