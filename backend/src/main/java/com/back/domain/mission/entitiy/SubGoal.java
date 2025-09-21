package com.back.domain.mission.entitiy;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name= "sub_goals")
public class SubGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer orderNum; // 주차 ex 1주차

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;


    //일단 주차단위 수정을 할 가능성이 있어서 생성 없다면 나중에 삭제
    @Column(nullable = false)
    private boolean isEditable = true;


    @OneToMany(mappedBy = "subGoal",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> categories;
}
