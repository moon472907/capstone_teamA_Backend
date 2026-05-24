package com.back.domain.world.controller;

import com.back.domain.world.dto.WorldDto;
import com.back.domain.world.service.WorldService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "World", description = "보드(World) 조회 API")
@RestController
@RequestMapping("/api/v1/worlds")
@RequiredArgsConstructor
public class WorldController {

    private final WorldService worldService;

    @Operation(summary = "보드 목록 조회")
    @GetMapping
    public ApiResponse<List<WorldDto>> getWorlds() {
        return ApiResponse.success("200", "보드 목록을 조회했습니다.", worldService.findAll());
    }

    @Operation(summary = "보드 상세 조회 (노드/엣지 포함)")
    @GetMapping("/{worldId}")
    public ApiResponse<WorldDto> getWorld(@PathVariable Integer worldId) {
        return ApiResponse.success("200", "보드를 조회했습니다.", worldService.findById(worldId));
    }
}
