package com.back.domain.mission.service;

import com.back.domain.mission.dto.response.MissionOverviewResponse;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final PartyMissionService partyMissionService;

    // 특정 회원의 전체 미션 조회 ( 진행 중과 완료로 분리)
    public MissionOverviewResponse getMissions(Integer memberId) {
        List<Mission> activeMissions = missionRepository.findByMemberIdAndIsCompleted(memberId, false);
        List<Mission> completedMissions = missionRepository.findByMemberIdAndIsCompleted(memberId, true);

        List<MissionResponse> activeResponses = activeMissions.stream()
                .map(m -> partyMissionService.convertToResponse(m, false))
                .collect(Collectors.toList());

        List<MissionResponse> completedResponses = completedMissions.stream()
                .map(m -> partyMissionService.convertToResponse(m, false))
                .collect(Collectors.toList());

        return MissionOverviewResponse.builder()
                .activeMissions(activeResponses)
                .completedMissions(completedResponses)
                .activeMissionCount(activeResponses.size())
                .remainingSlots(5 - activeResponses.size())
                .build();
    }

    // 미션 상세 조회 - task 까지 나옴
    public MissionResponse getMissionDetail(Integer memberId, Integer missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        return partyMissionService.convertToResponse(mission, true);
    }

    //미션 삭제
    @Transactional
    public void deleteMission(Integer memberId, Integer missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        if (!mission.getMember().getId().equals(memberId)) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        missionRepository.delete(mission);
    }
}