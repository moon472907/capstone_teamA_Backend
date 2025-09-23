package com.back.domain.party.paryChat.controller;

import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/chat")
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage") // 클라이언트가 메시지를 보내는 경로 (예: /app/chat.sendMessage)
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        // 1. 메시지를 데이터베이스에 저장
        chatMessageService.saveMessage(chatMessageDto);

        // 2. 메시지를 해당 파티의 채팅방으로 전송
        messagingTemplate.convertAndSend("/topic/party/" + chatMessageDto.getPartyId(), chatMessageDto);
    }

    // HTTP API를 통해 채팅 기록을 가져오는 엔드포인트 추가
    @GetMapping("/history")
    @Operation(summary = "채팅 기록 조회", description = "특정 파티의 채팅 기록을 조회합니다.")
    public List<ChatMessage> getChatHistory(@PathVariable Integer partyId) {
        return chatMessageService.getChatHistory(partyId);
    }
}