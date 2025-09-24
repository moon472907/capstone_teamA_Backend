package com.back.domain.mission.entitiy;


import com.back.domain.member.entity.Member;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
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
@Table(name="missions")
public class Mission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;*/

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionType type;

    @Column(nullable = false)
    private boolean isCompleted = false;

    @Column(nullable = false)
    private boolean isEditable = true;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubGoal> subGoals;


}
