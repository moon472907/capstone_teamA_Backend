package com.back.domain.party.paryChat.entity;

import com.back.domain.member.entity.Member;
import com.back.domain.party.party.entity.Party;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
@SoftDelete
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_partyId_createDate", columnList = "party_id, create_date DESC")
})
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    private String content;
}