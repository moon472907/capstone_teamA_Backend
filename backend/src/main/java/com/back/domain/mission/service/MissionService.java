package com.back.domain.mission.service;

import com.back.domain.member.repository.MemberRepository;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.repository.SubGoalRepository;
import com.back.domain.mission.repository.TaskLogRepository;
import com.back.domain.mission.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionService {
    private final MissionRepository missionRepository;
    private final SubGoalRepository subGoalRepository;
    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;
    private final MemberRepository memberRepository;
    //미션 생성 (  )


    //미션 조회

    // 미션 삭제
}
