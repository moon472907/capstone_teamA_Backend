package com.back.domain.mission.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.mission.dto.response.ProgressResponse;
import com.back.domain.mission.entitiy.Mission;
import com.back.domain.mission.repository.MissionRepository;
import com.back.domain.mission.service.MissionCalculateService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "ApiV1ProgressController", description = "진행률 조회 API")
public class ApiV1ProgressController {

    private final MissionCalculateService missionCalculateService;
    private final MissionRepository missionRepository;
    private final Rq rq;

    @GetMapping("/daily")
    @Operation(summary = "일일 진행률 조회", description = "특정 날짜의 일일 진행률을 조회합니다.")
    public ResponseEntity<ApiResponse<ProgressResponse>> getDailyProgress(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Member actor = rq.getActorFromDb();
        if (date == null) date = LocalDate.now();

        Integer progressRate = missionCalculateService.calculateDailyProgress(actor.getId(), date);

        ProgressResponse response = ProgressResponse.builder()
                .type("DAILY")
                .date(date)
                .progressRate(progressRate)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "일일 진행률 조회 성공", response));
    }

    @GetMapping("/mission/{missionId}")
    @Operation(summary = "미션 전체 진행률 조회", description = "특정 미션의 전체 진행률을 조회합니다.")
    public ResponseEntity<ApiResponse<ProgressResponse>> getMissionProgress(
            @PathVariable Integer missionId) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다"));

        Integer progressRate = missionCalculateService.calculateMissionProgress(mission);
        Integer currentWeek = missionCalculateService.calculateCurrentWeek(mission);

        ProgressResponse response = ProgressResponse.builder()
                .type("MISSION")
                .missionId(missionId)
                .progressRate(progressRate)
                .currentWeek(currentWeek)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 진행률 조회 성공", response));
    }

    @GetMapping("/mission/{missionId}/week")
    @Operation(summary = "주간 진행률 조회", description = "특정 미션의 현재 주차 진행률을 조회합니다.")
    public ResponseEntity<ApiResponse<ProgressResponse>> getWeeklyProgress(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다"));

        LocalDate today = LocalDate.now();
        Integer progressRate = missionCalculateService.calculateWeeklyProgress(actor.getId(), mission, today);
        Integer currentWeek = missionCalculateService.calculateCurrentWeek(mission);

        ProgressResponse response = ProgressResponse.builder()
                .type("WEEKLY")
                .missionId(missionId)
                .weekNum(currentWeek)
                .progressRate(progressRate)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "주간 진행률 조회 성공", response));
    }
}
