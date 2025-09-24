package com.back.domain.mission.controller;


import com.back.domain.member.entity.Member;
import com.back.domain.mission.dto.request.MissionCreateRequest;
import com.back.domain.mission.dto.request.MissionUpdateRequest;
import com.back.domain.mission.dto.response.MissionOverviewResponse;
import com.back.domain.mission.dto.response.MissionResponse;
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
    private final MissionService missionService;
    private final Rq rq;

    @PostMapping
    @Operation(summary = "미션 생성", description = "AI 또는 커스텀 미션을 생성합니다.")
    public ResponseEntity<ApiResponse<MissionResponse>> createMission(
            @Valid @RequestBody MissionCreateRequest request){
        Member actor = rq.getActorFromDb();
        MissionResponse response = missionService.createMission(actor.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("201","미션 생성 성공", response));
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
    @Operation(summary = "미션 상세 조회", description = "특정 미션의 상세 정보를 조회하는 API")
    public ResponseEntity<ApiResponse<MissionResponse>> getMissionDetail(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();

        MissionResponse response = missionService.getMissionDetail(actor.getId(), missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 상세 조회 성공", response));
    }

    @PutMapping("")
    @Operation(summary = "미션 수정", description = "미션의 태스크를 수정하는 API (1회 제한)")
    public ResponseEntity<ApiResponse<MissionResponse>> updateMission(
            @Valid @RequestBody MissionUpdateRequest request) {

        Member actor = rq.getActorFromDb();

        MissionResponse response = missionService.updateMission(actor.getId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 수정 성공", response));
    }

    @DeleteMapping("/{missionId}")
    @Operation(summary = "미션 삭제", description = "미션을 삭제하는 API")
    public ResponseEntity<ApiResponse<Void>> deleteMission(
            @PathVariable Integer missionId) {

        Member actor = rq.getActorFromDb();

        // TODO: MissionService에 deleteMission 메서드 추가 필요
        // missionService.deleteMission(actor.getId(), missionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "미션 삭제 성공"));
    }
}
