package com.back.domain.mission.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name= "tasks")
public class Task extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_goal_id", nullable = false)
    private SubGoal subGoal;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int dayNum;     // 주차 내 며칠에 해당하는 지 ( 1 = 월, ... )

    @Column(nullable = false)
    private Boolean hasBeenEdited = false;

    private LocalDate editableUntil;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskLog> taskLogs = new ArrayList<>();


}
