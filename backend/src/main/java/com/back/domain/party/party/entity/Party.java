package com.back.domain.party.party.entity;

import com.back.domain.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Party extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maxMembers;

    @Column(nullable = false)
    private boolean isPublic;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leader_id")
    private Member leader;

    @Column(nullable = false)
    private Integer views = 0;

    public void incrementViews() {
        this.views++;
    }

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartyMember> partyMembers = new ArrayList<>();
}