package com.back.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sub_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Integer orderNum;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean hasBeenEdited = false;  // Boolean으로 변경

    private LocalDate editableUntil;

    @OneToMany(mappedBy = "subGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    // Lombok @Getter가 Boolean 타입에 대해 getHasBeenEdited() 생성
    // 필요시 명시적으로 추가
    public Boolean getHasBeenEdited() {
        return hasBeenEdited;
    }

    public boolean canEdit() {
        if (Boolean.TRUE.equals(hasBeenEdited)) return false;

        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) return true;
        if (editableUntil != null && !today.isAfter(editableUntil)) return true;

        return false;
    }
}
