package com.back.domain.mission.entitiy;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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
    private int orderNum;     // 같은 dayNum 안에서 순서 구분하기 위한 엔티티 ( 여러 task 안쓰면 상관 X )

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskLog> taskLogs = new ArrayList<>();

}
