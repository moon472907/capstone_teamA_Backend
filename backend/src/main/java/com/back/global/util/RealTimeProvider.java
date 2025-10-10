package com.back.global.util;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("!dev") //dev가 아닌 모든 환경
public class RealTimeProvider implements TimeProvider {

    @Override
    public LocalDate today() {
        return LocalDate.now();  // 항상 실제 날짜
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();  // 항상 실제 시간
    }
}
