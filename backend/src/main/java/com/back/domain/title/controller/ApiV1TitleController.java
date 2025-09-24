package com.back.domain.title.controller;


import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.dto.TitleDto;
import com.back.domain.title.service.TitleService;
import com.back.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/Title")
@RequiredArgsConstructor
@Tag(name = "ApiV1TitleController", description = "API 칭호 컨트롤러")

public class ApiV1TitleController {
    private final TitleService titleService;

    @PostMapping
    @Transactional
    @Operation(summary = "칭호 생성 ")
    public ApiResponse<TitleDto>  createTitle (@Valid  @RequestBody CreateTitleDto createTitleDto) {
        return ApiResponse.success("201", "칭호 생성 성공", titleService.createTitle(createTitleDto));
    }

    @GetMapping("/{id}")
    @Transactional
    @Operation(summary = "칭호 단건 조회")
    public ApiResponse<TitleDto> findAllTitles( @PathVariable int  id)
    {
        return new ApiResponse<>("200", "칭호 전체 조회 완료",  titleService.findById(id));
    }
    @GetMapping
    @Transactional
    @Operation(summary = "칭호 전체 조회")
    public ApiResponse<List<TitleDto>> findAllTitles()
    {
        return new ApiResponse<>("200", "칭호 전체 조회 완료",  titleService.findAll());
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation
    public ApiResponse<TitleDto> updateTitle(@PathVariable int id, @Valid  @RequestBody TitleDto titleDto)
    {
        return new ApiResponse<>("200", "칭호 수정 성공", titleService.updateTitle(id, titleDto));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "칭호 삭제")
    public ApiResponse<Void> deleteTitle(@PathVariable int id)
    {
        titleService.deleteTitle(id);
        return ApiResponse.success("200", "칭호 삭제 성공");
    }
}
