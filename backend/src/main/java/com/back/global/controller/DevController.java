package com.back.global.controller;

import com.back.global.util.DevTimeProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
@Profile("dev")  // 개발 환경에서만 활성화
@Tag(name = "DevController", description = "[개발용] 시간 조작 API")
public class DevController {

    private final DevTimeProvider timeProvider;

    @PostMapping("/time/set")
    @Operation(summary = "[개발용] 시간 고정", description = "시스템 시간을 특정 날짜로 고정합니다")
    public ResponseEntity<?> setFixedTime(@RequestParam String date) {
        LocalDate parsed = LocalDate.parse(date);
        timeProvider.setFixedDate(parsed);

        return ResponseEntity.ok(Map.of(
                "message", "시간이 " + date + "로 고정되었습니다",
                "fixedDate", date
        ));
    }

    @DeleteMapping("/time/reset")
    @Operation(summary = "[개발용] 시간 고정 해제", description = "실제 시간으로 복원합니다")
    public ResponseEntity<?> resetTime() {
        timeProvider.reset();

        return ResponseEntity.ok(Map.of(
                "message", "실제 시간으로 복원되었습니다"
        ));
    }

    @GetMapping("/time/current")
    @Operation(summary = "[개발용] 현재 시간 확인", description = "현재 시스템이 인식하는 시간을 확인합니다")
    public ResponseEntity<?> getCurrentTime() {
        LocalDate current = timeProvider.today();
        LocalDate fixed = timeProvider.getFixedDate();

        Map<String, Object> response = new HashMap<>();
        response.put("fixed", fixed != null);
        response.put("fixedDate", fixed);
        response.put("currentDate", current);
        response.put("dayOfWeek", current.getDayOfWeek().toString());

        return ResponseEntity.ok(response);
    }
}