package com.back.domain.party.paryChat.repository;

import com.back.domain.member.entity.Member;
import com.back.domain.party.paryChat.entity.ChatMessage;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    Page<ChatMessage> findByPartyIdOrderByCreateDateDesc(Integer partyId, Pageable pageable);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.sender = NULL WHERE cm.sender = :member")
    int setSenderToNullByMember(@Param("member") Member member);
}