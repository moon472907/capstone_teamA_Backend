package com.back.domain.mission.entity;


import com.back.domain.member.entity.Member;
import com.back.domain.mission.enums.MissionCategory;
import com.back.domain.mission.enums.MissionType;
import com.back.domain.party.party.entity.Party;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
@Table(name="missions")
public class Mission extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;  // 미션 생성자

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", unique = true, nullable = true)
    private Party party; // null == 개인미션

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
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<SubGoal> subGoals = new ArrayList<>();

    public boolean isPartyMission() {
        return party != null;
    }

}