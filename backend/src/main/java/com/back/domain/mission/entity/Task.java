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

    // 수정 관리 필드
    @Column(nullable = false)
    @Builder.Default
    private Boolean hasBeenEdited = false;  // 수정 이력

    @Column
    private LocalDate editDeadline;  // 수정 마감일

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskLog> taskLogs = new ArrayList<>();


}
