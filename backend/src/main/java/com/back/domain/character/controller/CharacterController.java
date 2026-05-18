package com.back.domain.character.controller;

import com.back.domain.character.dto.CharacterDto;
import com.back.domain.character.service.CharacterService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Character", description = "캐릭터 목록 API")
@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    @Operation(summary = "캐릭터 목록 조회")
    @GetMapping
    public ApiResponse<List<CharacterDto>> getCharacters() {
        return ApiResponse.success("200", "캐릭터 목록입니다.", characterService.getAll());
    }
}
