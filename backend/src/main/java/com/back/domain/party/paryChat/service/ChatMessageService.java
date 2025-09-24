package com.back.domain.party.paryChat.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.party.party.repository.PartyRepository;
import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.repository.ChatMessageRepository;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final PartyRepository partyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    @CacheEvict(value = "chatHistory", key = "#chatMessageDto.partyId")
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
    @CacheEvict(value = "chatHistory", key = "#result.party.id")
    public ChatMessage updateMessage(Integer messageId, String newContent, String senderEmail) {
        // 메시지 ID로 메시지 엔티티를 찾습니다.
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        // 메시지 보낸 사람과 수정 요청자가 동일한지 확인합니다.
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다."));

        if (chatMessage.getSender().getId() != sender.getId()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "메시지를 수정할 권한이 없습니다.");
        }

        // 메시지 내용을 업데이트하고 반환합니다.
        chatMessage.setContent(newContent);
        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    @CacheEvict(value = "chatHistory", key = "#result.party.id")
    public ChatMessage deleteMessage(Integer messageId, String senderEmail) {
        // 메시지 ID로 메시지 엔티티를 찾습니다.
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        // 메시지 보낸 사람과 삭제 요청자가 동일한지 확인합니다.
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다."));

        if (chatMessage.getSender().getId() != sender.getId()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "메시지를 삭제할 권한이 없습니다.");
        }

        // 메시지를 삭제하고, 캐시 무효화를 위해 반환합니다.
        chatMessageRepository.delete(chatMessage);
        return chatMessage;
    }

    @Cacheable(value = "chatHistory", key = "{#partyId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<ChatMessage> getChatHistory(Integer partyId, Pageable pageable) {
        return chatMessageRepository.findByPartyIdOrderByCreateDateDesc(partyId, pageable);
    }
}