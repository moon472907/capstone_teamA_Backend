package com.back.domain.party.party.controller;

import com.back.domain.party.party.dto.PartyDto;
import com.back.domain.party.party.dto.PartyRequestDto;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.party.service.PartyService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "파티 가입", description = "공개 파티에 가입하는 API")
    public ResponseEntity<ApiResponse<Void>> joinParty(
            @PathVariable Integer partyId,
            @RequestParam("memberId") Integer memberId
    ) {
        partyService.joinParty(partyId, memberId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "파티 가입 성공"));
    }
}