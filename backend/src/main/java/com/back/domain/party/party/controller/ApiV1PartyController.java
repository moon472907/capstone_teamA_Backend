package com.back.domain.party.party.controller;

import com.back.domain.party.party.dto.*;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.entity.PartyMember;
import com.back.domain.party.party.service.PartyService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
@Tag(name = "ApiV1PartyController", description = "API 파티 컨트롤러")
public class ApiV1PartyController {

    private final PartyService partyService;

    @PostMapping
    @Operation(summary = "파티 생성", description = "파티를 생성하는 API")
    public ResponseEntity<ApiResponse<PartyDto>> createParty(
            @Valid @RequestBody PartyRequestDto requestDto,
            @RequestParam("memberId") Integer memberId
    ) {
        Party createdParty = partyService.createParty(requestDto, memberId);
        PartyDto partyDto = new PartyDto(createdParty);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("201", "파티 생성 성공", partyDto));
    }

    @PostMapping("/{partyId}/join")
    @Operation(summary = "공개 파티 가입 신청", description = "공개 파티에 가입을 신청하는 API. 파티장이 수락해야 가입 완료됩니다.")
    public ResponseEntity<ApiResponse<Void>> joinParty(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.joinParty(partyId, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 가입 신청 성공"));
    }

    @DeleteMapping("/{partyId}/leave")
    @Operation(summary = "파티 탈퇴", description = "가입된 파티를 탈퇴하는 API")
    public ResponseEntity<ApiResponse<Void>> leaveParty(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.leaveParty(partyId, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 탈퇴 성공"));
    }

    @PatchMapping("/{partyId}")
    @Operation(summary = "파티 수정", description = "파티의 이름, 최대 멤버 수, 공개 여부를 수정하는 API")
    public ResponseEntity<ApiResponse<Void>> updateParty(
            @PathVariable Integer partyId,
            @Valid @RequestBody PartyUpdateRequestDto requestDto,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.updateParty(partyId, requestDto, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 수정 성공"));
    }

    @DeleteMapping("/{partyId}")
    @Operation(summary = "파티 삭제", description = "파티를 삭제하는 API")
    public ResponseEntity<ApiResponse<Void>> deleteParty(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.deleteParty(partyId, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 삭제 성공"));
    }

    @GetMapping
    @Operation(summary = "파티 목록 조회", description = "공개 파티 목록을 조회하는 API")
    public ResponseEntity<ApiResponse<List<PartyDto>>> getPartyList() {
        List<Party> parties = partyService.getPartyList();
        List<PartyDto> partyDtos = parties.stream()
                .map(PartyDto::new)
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 목록 조회 성공", partyDtos));
    }

    @GetMapping("/{partyId}")
    @Operation(summary = "특정 파티 조회", description = "특정 파티의 상세 정보를 조회하는 API")
    public ResponseEntity<ApiResponse<PartyDto>> getPartyDetails(@PathVariable Integer partyId) {
        Party party = partyService.getPartyDetails(partyId);
        PartyDto partyDto = new PartyDto(party);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 상세 조회 성공", partyDto));
    }

    @PostMapping("/{partyId}/invite")
    @Operation(summary = "파티 초대 (이메일)", description = "파티장이 다른 멤버를 이메일 주소로 파티에 초대하는 API")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable Integer partyId,
            @RequestParam("leaderId") Integer leaderId,
            @RequestBody @Valid InvitationDto invitationDto
    ) {
        partyService.inviteMember(partyId, leaderId, invitationDto.getInvitedMemberEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 초대 성공"));
    }

    @PostMapping("/{partyId}/accept")
    @Operation(summary = "초대/신청 수락", description = "초대/신청 대기 중인 멤버를 파티원이 되도록 수락하는 API")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.acceptInvitation(partyId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "초대/신청 수락 성공"));
    }

    @PostMapping("/{partyId}/reject")
    @Operation(summary = "초대/신청 거절", description = "초대/신청 대기 중인 멤버를 거절하는 API")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.rejectInvitation(partyId, memberId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "초대/신청 거절 성공"));
    }

    @DeleteMapping("/{partyId}/members/{kickedMemberId}")
    @Operation(summary = "파티원 추방", description = "파티장이 특정 파티원을 추방하는 API")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Integer partyId,
            @RequestParam("leaderId") Integer leaderId,
            @PathVariable Integer kickedMemberId
    ) {
        partyService.kickMember(partyId, leaderId, kickedMemberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티원 추방 성공"));
    }

    @GetMapping("/{partyId}/requests")
    @Operation(summary = "파티 가입 신청/초대 목록 조회", description = "파티장이 가입 신청 또는 초대 대기 중인 멤버 목록을 조회하는 API")
    public ResponseEntity<ApiResponse<List<PartyMemberDto>>> getPendingJoinRequests(
            @PathVariable Integer partyId,
            @RequestParam("leaderId") Integer leaderId
    ) {
        List<PartyMember> pendingRequests = partyService.getPendingJoinRequests(partyId, leaderId);
        List<PartyMemberDto> requestDtos = pendingRequests.stream()
                .map(pm -> new PartyMemberDto(pm.getMember()))
                .collect(Collectors.toList());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "가입 신청/초대 목록 조회 성공", requestDtos));
    }
}