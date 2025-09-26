package com.back.domain.mission.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.mission.dto.request.WeeklyUpdateRequest;
import com.back.domain.mission.dto.response.SubGoalResponse;
import com.back.domain.mission.service.WeeklyMissionUpdateService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/missions/weekly")
@RequiredArgsConstructor
@Tag(name = "ApiV1WeeklyUpdateController", description = "주차별 미션 수정 API")
public class ApiV1WeeklyUpdateController {

    private final WeeklyMissionUpdateService weeklyMissionUpdateService;
    private final Rq rq;

    @GetMapping("/{missionId}/editable")
    @Operation(summary = "수정 가능한 주차 조회", description = "현재 수정 가능한 주차 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<SubGoalResponse>>> getEditableWeeks(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();
        List<SubGoalResponse> response = weeklyMissionUpdateService.getEditableWeeks(actor.getId(), missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "수정 가능한 주차 조회 성공", response));
    }

    @PutMapping("/update")
    @Operation(summary = "주차별 수정", description = "특정 주차의 내용을 수정합니다 (주차별 1회 제한)")
    public ResponseEntity<ApiResponse<SubGoalResponse>> updateWeekly(
            @Valid @RequestBody WeeklyUpdateRequest request) {

        Member actor = rq.getActorFromDb();
        SubGoalResponse response = weeklyMissionUpdateService.updateWeekly(actor.getId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "주차 수정 성공", response));
    }

    @PostMapping("/{missionId}/initialize")
    @Operation(summary = "수정 기한 초기화", description = "미션 시작 시 주차별 수정 기한을 설정합니다")
    public ResponseEntity<ApiResponse<Void>> initializeEditablePeriods(
            @PathVariable Integer missionId) {

        weeklyMissionUpdateService.initializeEditablePeriods(missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "수정 기한 초기화 성공"));
    }
}