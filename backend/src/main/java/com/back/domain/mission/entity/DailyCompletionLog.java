package com.back.domain.mission.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_completion_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCompletionLog extends BaseEntity {

    @Column(nullable = false)
    private Integer memberId;

    @Column(nullable = false)
    private LocalDate completedDate;
}