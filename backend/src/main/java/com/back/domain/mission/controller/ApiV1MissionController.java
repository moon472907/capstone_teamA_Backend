package com.back.domain.mission.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.mission.dto.request.PartyMissionCreateRequest;
import com.back.domain.mission.dto.response.MissionOverviewResponse;
import com.back.domain.mission.dto.response.MissionResponse;
import com.back.domain.mission.service.PartyMissionService;
import com.back.domain.mission.service.MissionService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
@Tag(name = "ApiV1MissionController", description = "API 미션 컨트롤러")
public class ApiV1MissionController {

    private final PartyMissionService partyMissionService;
    private final MissionService missionService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "미션 생성", description = "개인 또는 파티 미션을 생성합니다.")
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(
            @Valid @RequestBody PartyMissionCreateRequest request) {

        Member actor = rq.getActorFromDb();
        MissionResponse response = partyMissionService.createPartyMission(actor.getId(), request);

        String message = request.getMaxMembers() > 1 ?
                "파티 미션이 생성되었습니다" : "개인 미션이 생성되었습니다";

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("201", message, response));
    }

    @GetMapping
    @Operation(summary = "미션 목록 조회", description = "진행중/완료된 미션 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<MissionOverviewResponse>> getMissions() {
        Member actor = rq.getActorFromDb();
        MissionOverviewResponse response = missionService.getMissions(actor.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 목록 조회 성공", response));
    }

    @GetMapping("/{missionId}")
    @Operation(summary = "미션 상세 조회", description = "특정 미션의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionDetail(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();
        MissionResponse response = missionService.getMissionDetail(actor.getId(), missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 상세 조회 성공", response));
    }

    @DeleteMapping("/{missionId}")
    @Operation(summary = "미션 삭제", description = "미션을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteMission(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();
        missionService.deleteMission(actor.getId(), missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 삭제 성공"));
    }

    @GetMapping("/all")
    @Operation(summary = "전체 미션 목록 조회", description = "모든 사용자의 미션을 조회합니다.")
    public ResponseEntity<ApiResponse<MissionOverviewResponse>> getAllMissions() {
        MissionOverviewResponse response = missionService.getAllMissionsForAdmin();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "전체 미션 조회 성공", response));
    }

    @GetMapping("/all/{missionId}")
    @Operation(summary = "미션 상세 조회 (모든 주차)", description = "모든 주차의 Task를 제한없이 조회합니다.")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionAllDetail(
            @PathVariable Integer missionId) {

        MissionResponse response = missionService.getMissionDetailAdmin(missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 상세 조회 성공", response));
    }
}