package com.back.domain.mission.service;

import com.back.domain.mission.dto.response.MissionOverviewResponse;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.entity.Mission;
import com.back.domain.mission.exception.MissionErrorCode;
import com.back.domain.mission.exception.MissionException;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.SubGoalRepository;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.entity.PartyMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionRepository missionRepository;
    private final PartyMissionService partyMissionService;
    private final SubGoalRepository subGoalRepository;
    private final MissionCalculateService missionCalculateService;
    private static final int MAX_MISSIONS_PER_USER = 5;

    // 특정 회원의 전체 미션 조회 ( 진행 중과 완료로 분리)
    public MissionOverviewResponse getMissions(Integer memberId) {
        List<Mission> activeMissions = missionRepository.findByMemberIdAndIsCompletedWithParty(memberId, false);
        List<Mission> completedMissions = missionRepository.findByMemberIdAndIsCompletedWithParty(memberId, true);

        List<MissionResponse> activeResponses = activeMissions.stream()
                .map(m -> partyMissionService.convertToSimpleResponse(m, memberId))
                .collect(Collectors.toList());

        List<MissionResponse> completedResponses = completedMissions.stream()
                .map(m -> partyMissionService.convertToSimpleResponse(m, memberId))
                .collect(Collectors.toList());

        return MissionOverviewResponse.builder()
                .activeMissions(activeResponses)
                .completedMissions(completedResponses)
                .activeMissionCount(activeResponses.size())
                .remainingSlots(MAX_MISSIONS_PER_USER - activeResponses.size())
                .build();
    }

    // 미션 상세 조회 - task 까지 나옴
    public MissionResponse getMissionDetail(Integer memberId, Integer missionId) {
        Mission mission = missionRepository.findByIdWithSubGoals(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        subGoalRepository.findByMissionIdWithTasks(missionId);

        validateMissionAccess(mission, memberId, false);

        return partyMissionService.convertToDetailResponse(mission, memberId);
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

    // 미션 접근 권한 검증
    // 개인 미션 : 본인만 접근 가능
    // 파티 미션 : ACCEPTED 상태 멤버만 접근 가능
    // 공개 파티 : 외부인도 읽기 전용 접근 가능
    private void validateMissionAccess(Mission mission, Integer memberId, boolean requireWrite) {
        // 개인 미션
        if (!mission.isPartyMission()) {
            if (!mission.getMember().getId().equals(memberId)) {
                throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
            }
            return;
        }


        // 파티 미션
        PartyMember partyMember = mission.getParty().getPartyMembers().stream()
                .filter(pm -> pm.getMember().getId().equals(memberId))
                .findFirst()
                .orElse(null);

        // 파티 멤버가 아닌 경우
        if (partyMember == null) {
            // 공개 파티이고 읽기 전용이면 허용
            if (!requireWrite && mission.getParty().isPublic()) {
                return;
            }
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }

        // ACCEPTED 상태가 아니면 접근 불가
        if (partyMember.getStatus() != PartyMemberStatus.ACCEPTED) {
            throw new MissionException(MissionErrorCode.MEMBER_FORBIDDEN);
        }
    }


    //관리자용 전체 미션 목록
    public MissionOverviewResponse getAllMissionsForAdmin() {
        List<Mission> allMissions = missionRepository.findAllWithParty();

        List<MissionResponse> activeMissions = new ArrayList<>();
        List<MissionResponse> completedMissions = new ArrayList<>();

        for (Mission m : allMissions) {
            MissionResponse response = partyMissionService.convertToSimpleResponse(
                    m, m.getMember().getId()
            );

            if (m.isCompleted()) {
                completedMissions.add(response);
            } else {
                activeMissions.add(response);
            }
        }

        return MissionOverviewResponse.builder()
                .activeMissions(activeMissions)
                .completedMissions(completedMissions)
                .activeMissionCount(activeMissions.size())
                .remainingSlots(null)
                .build();
    }

    //  관리자 상세 조회
    public MissionResponse getMissionDetailAdmin(Integer missionId) {
        Mission mission = missionRepository.findByIdWithSubGoals(missionId)
                .orElseThrow(() -> new MissionException(MissionErrorCode.MISSION_NOT_FOUND));

        subGoalRepository.findByMissionIdWithTasks(missionId);

        return partyMissionService.convertToDetailResponseAdmin(mission);
    }



}