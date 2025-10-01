package com.back.domain.mission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

    @OneToMany(mappedBy = "subGoal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    public boolean isCurrentWeek() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    public boolean isPast() {
        return LocalDate.now().isAfter(endDate);
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setSubGoal(this);
    }

    public void setMission(Mission mission) {
        this.mission = mission;
        if (mission != null && !mission.getSubGoals().contains(this)) {
            mission.getSubGoals().add(this);
        }
    }

}
