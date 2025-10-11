package com.back.domain.statistics.controller;


import com.back.domain.member.entity.Member;
import com.back.domain.statistics.dto.StatisticsResponse;
import com.back.domain.statistics.entity.MemberStatistics;
import com.back.domain.statistics.repository.MemberStatisticsRepository;
import com.back.global.common.ApiResponse;
import com.back.global.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Statistics", description = "통계 API")
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final MemberStatisticsRepository statsRepo;
    private final Rq rq;

    @GetMapping("/me")
    @Operation(summary = "내 통계 조회", description = "데일리/주차/미션 완료 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<StatisticsResponse>> getMyStatistics() {
        Member actor = rq.getActorFromDb();

        MemberStatistics stats = statsRepo.findByMemberId(actor.getId())
                .orElse(null);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("200", "통계 조회 성공", StatisticsResponse.from(stats)));
    }
}