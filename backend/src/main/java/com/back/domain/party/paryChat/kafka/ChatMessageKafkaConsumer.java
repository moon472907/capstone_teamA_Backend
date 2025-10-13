package com.back.domain.party.paryChat.kafka;

import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatMessageKafkaConsumer {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    private final SimpMessageSendingOperations messagingTemplate;


    @KafkaListener(topics = "chat-messages", groupId = "chat-db-saver-group")
    @Transactional
    public void consumeChatMessage(String message) throws Exception {

        ChatMessageDto chatMessageDto = objectMapper.readValue(message, ChatMessageDto.class);

        // 메시지를 데이터베이스에 저장
        ChatMessageDto broadcastDto = chatMessageService.saveMessage(chatMessageDto);

        // 메시지가 저장된 후, WebSocket을 통해 클라이언트에게 브로드캐스트
        String destination = "/topic/party/" + broadcastDto.getPartyId();
        messagingTemplate.convertAndSend(destination, broadcastDto);
    }
}