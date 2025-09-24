package com.back.domain.mission.entitiy;

import com.back.domain.mission.enums.TaskStatus;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import static lombok.AccessLevel.PROTECTED;


@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name= "tasks_logs")
public class TaskLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private int memberId;

    // 수행한 날짜
    @Column(nullable = false)
    private LocalDate date;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;


}
