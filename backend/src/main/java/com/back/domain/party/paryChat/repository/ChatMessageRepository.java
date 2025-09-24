package com.back.domain.party.paryChat.repository;

import com.back.domain.party.paryChat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    Page<ChatMessage> findByPartyIdOrderByCreateDateDesc(Integer partyId, Pageable pageable);
}