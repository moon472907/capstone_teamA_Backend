package com.back.domain.party.paryChat.service;

import com.back.domain.member.repository.MemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.repository.ChatMessageRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;
    private final CacheManager cacheManager;

    @Transactional
    public void saveMessage(ChatMessageDto chatMessageDto) {
        // 엔티티를 가져와 메시지를 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .content(chatMessageDto.getContent())
                .party(partyRepository.findById(chatMessageDto.getPartyId())
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "파티를 찾을 수 없습니다.")))
                .sender(memberRepository.findByEmail(chatMessageDto.getSenderEmail())
                        .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다.")))
                .build();
        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    @CacheEvict(value = "chatHistory", key = "#chatMessageDto.partyId")
    public ChatMessage updateMessage(ChatMessageDto chatMessageDto) {
        ChatMessage chatMessage = getAuthorizedChatMessage(chatMessageDto.getId(), chatMessageDto.getSenderEmail(), "수정");

        // 메시지 내용을 업데이트하고 반환합니다.
        chatMessage.setContent(chatMessageDto.getContent());
        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void deleteMessage(Integer messageId, String senderEmail) {
        ChatMessage chatMessage = getAuthorizedChatMessage(messageId, senderEmail, "삭제");

        // 캐시 무효화 키로 사용할 partyId를 미리 저장
        Integer partyId = chatMessage.getParty().getId();

        // 메시지 삭제
        chatMessageRepository.delete(chatMessage);

        // 캐시를 명시적으로 무효화합니다.
        if (cacheManager.getCache("chatHistory") != null) {
            Objects.requireNonNull(cacheManager.getCache("chatHistory")).evict(partyId);
        }
    }

    private ChatMessage getAuthorizedChatMessage(Integer messageId, String senderEmail, String operation) {
        // 1. 메시지 ID로 메시지 엔티티를 찾습니다.
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        // 2. 메시지 보낸 사람과 요청자의 이메일이 동일한지 확인합니다.
        if (!chatMessage.getSender().getEmail().equals(senderEmail)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "메시지를 " + operation + "할 권한이 없습니다.");
        }

        return chatMessage;
    }

    @Cacheable(value = "chatHistory", key = "#partyId")
    public Page<ChatMessage> getChatHistory(Integer partyId, Pageable pageable) {
        return chatMessageRepository.findByPartyIdOrderByCreateDateDesc(partyId, pageable);
    }
}