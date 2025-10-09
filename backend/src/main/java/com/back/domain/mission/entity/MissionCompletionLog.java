package com.back.domain.mission.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "mission_completion_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"mission_id", "member_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionCompletionLog extends BaseEntity {


    @Column(name = "mission_id", nullable = false)
    private Integer missionId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(nullable = false)
    private LocalDate completedDate;
}