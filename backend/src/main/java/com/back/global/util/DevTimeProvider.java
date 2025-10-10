package com.back.global.util;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Profile("dev")
public class DevTimeProvider implements TimeProvider {

    private LocalDate fixedDate = null;
    @Override
    public LocalDate today() {
        return fixedDate != null ? fixedDate : LocalDate.now();
    }

    @Override
    public LocalDateTime now() {
        LocalDate date = today();
        return date.atStartOfDay();
    }

    public void setFixedDate(LocalDate date) {
        this.fixedDate = date;
    }

    public void reset() {
        this.fixedDate = null;
    }

    public LocalDate getFixedDate() {
        return fixedDate;
    }
}
