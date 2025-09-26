package com.back.domain.mission.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.mission.dto.request.TaskCompleteRequest;
import com.back.domain.mission.dto.response.TaskCompleteResponse;
import com.back.domain.mission.dto.response.TaskResponse;
import com.back.domain.mission.service.TaskService;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "ApiV1TaskController", description = "API 태스크 컨트롤러")
public class ApiV1TaskController {

    private final TaskService taskService;
    private final Rq rq;

    @GetMapping("/today")
    @Operation(summary = "오늘의 태스크 조회", description = "오늘 수행해야 할 태스크 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTodayTasks() {
        Member actor = rq.getActorFromDb();
        List<TaskResponse> response = taskService.getTodayTasks(actor.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "오늘의 태스크 조회 성공", response));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "특정 날짜 태스크 조회", description = "특정 날짜의 태스크 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        Member actor = rq.getActorFromDb();
        List<TaskResponse> response = taskService.getTasksByDate(actor.getId(), date);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "태스크 조회 성공", response));
    }

    @PostMapping("/complete")
    @Operation(summary = "태스크 완료", description = "태스크를 완료 처리합니다.")
    public ResponseEntity<ApiResponse<TaskCompleteResponse>> completeTask(
            @Valid @RequestBody TaskCompleteRequest request) {

        Member actor = rq.getActorFromDb();
        TaskCompleteResponse response = taskService.completeTask(actor.getId(), request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "태스크 완료 처리 성공", response));
    }
}

