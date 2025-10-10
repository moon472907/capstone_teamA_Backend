package com.back.global.controller;

import com.back.domain.mission.scheduler.MissionEndScheduler;
import com.back.domain.mission.scheduler.TaskAutoFailScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev/scheduler")
@RequiredArgsConstructor
@Profile("dev")  // 개발 환경에서만
@Tag(name = "DevSchedulerController", description = "[개발용] 스케줄러 수동 실행 API")
public class DevSchedulerController {

    private final TaskAutoFailScheduler taskAutoFailScheduler;
    private final MissionEndScheduler missionEndScheduler;

    @PostMapping("/task-auto-fail")
    @Operation(summary = "[개발용] 태스크 자동 실패 처리",
            description = "어제 미완료 태스크를 SKIPPED 처리합니다")
    public ResponseEntity<?> runTaskAutoFail() {
        try {
            taskAutoFailScheduler.autoFailExpiredTasks();
            return ResponseEntity.ok(Map.of(
                    "message", "태스크 자동 실패 처리 완료",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "실행 실패: " + e.getMessage(),
                    "success", false
            ));
        }
    }

    @PostMapping("/mission-end")
    @Operation(summary = "[개발용] 미션 종료 처리",
            description = "어제 종료된 미션들을 완료 처리합니다")
    public ResponseEntity<?> runMissionEnd() {
        try {
            missionEndScheduler.completeEndedMissions();
            return ResponseEntity.ok(Map.of(
                    "message", "미션 종료 처리 완료",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "실행 실패: " + e.getMessage(),
                    "success", false
            ));
        }
    }
}