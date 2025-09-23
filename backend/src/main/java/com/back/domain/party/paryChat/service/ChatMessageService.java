package com.back.domain.party.paryChat.service;

import com.back.domain.member.repository.MemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.repository.ChatMessageRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveMessage(ChatMessageDto chatMessageDto) {
        // 엔티티를 가져와 메시지를 생성
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(chatMessageDto.getContent());
        chatMessage.setParty(partyRepository.findById(chatMessageDto.getPartyId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다.")));
        chatMessage.setSender(memberRepository.findByEmail(chatMessageDto.getSenderEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다.")));

        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getChatHistory(Integer partyId) {
        return chatMessageRepository.findByPartyIdOrderByCreateDateDesc(partyId);
    }
}