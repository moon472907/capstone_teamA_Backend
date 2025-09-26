package com.back.domain.mission.entity;

import com.back.domain.mission.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // 파티와 멤버 정보 모두 필요
    @Column(nullable = true)
    private Integer partyId;  // 어떤 파티

    @Column(nullable = false)
    private Integer memberId;  // 어떤 멤버가 완료했는지

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;


}
