package com.back.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDate today();
    LocalDateTime now();
}
