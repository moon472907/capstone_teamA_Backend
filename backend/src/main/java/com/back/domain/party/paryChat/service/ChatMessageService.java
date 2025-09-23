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

    @Transactional
    public void updateMessage(Integer messageId, String newContent, String senderEmail) {
        // 메시지 ID로 메시지 엔티티를 찾습니다.
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        // 메시지 보낸 사람과 수정 요청자가 동일한지 확인합니다.
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다."));

        if (chatMessage.getSender().getId() != sender.getId()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "메시지를 수정할 권한이 없습니다.");
        }

        // 메시지 내용을 업데이트합니다.
        chatMessage.setContent(newContent);
        chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void deleteMessage(Integer messageId, String senderEmail) {
        // 메시지 ID로 메시지 엔티티를 찾습니다.
        ChatMessage chatMessage = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "메시지를 찾을 수 없습니다."));

        // 메시지 보낸 사람과 삭제 요청자가 동일한지 확인합니다.
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "멤버를 찾을 수 없습니다."));

        if (chatMessage.getSender().getId() != sender.getId()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "메시지를 삭제할 권한이 없습니다.");
        }

        // 메시지를 삭제합니다.
        chatMessageRepository.delete(chatMessage);
    }

    public List<ChatMessage> getChatHistory(Integer partyId) {
        return chatMessageRepository.findByPartyIdOrderByCreateDateDesc(partyId);
    }
}