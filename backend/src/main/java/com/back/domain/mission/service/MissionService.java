package com.back.domain.mission.service;

import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.SubGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {
    private final MissionRepository missionRepository;
    private final SubGoalRepository subGoalRepository;

    //미션 생성 ( 최대 15주, 시작일 월요일 )


    //미션 조회

    // 미션 삭제
}
