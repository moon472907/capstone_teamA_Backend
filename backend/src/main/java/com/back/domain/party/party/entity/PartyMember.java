package com.back.domain.party.party.entity;

import com.back.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@IdClass(PartyMemberId.class)
@Table(name = "party_member", indexes = {
        @Index(name = "idx_party_member_composite", columnList = "party_id, member_id"),
        @Index(name = "idx_party_status", columnList = "party_id, status")
})
public class PartyMember {

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "party_id")
    private Party party;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PartyMemberStatus status;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}