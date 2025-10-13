package com.back.domain.party.paryChat.controller;

import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.service.ChatMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/chat")
@RequiredArgsConstructor
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_TOPIC = "chat-messages";

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto) {
        // 인증을 사용하지 않으므로, 클라이언트가 DTO에 담아 보낸 senderEmail을 그대로 사용합니다.
        String senderEmail = chatMessageDto.getSenderEmail();
        log.info("Message received from sender: {}", senderEmail);

        // 1. 메시지를 Kafka로 발행
        try {
            String jsonMessage = objectMapper.writeValueAsString(chatMessageDto);
            kafkaTemplate.send("chat-messages", jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert ChatMessageDto to JSON for Kafka: {}", e.getMessage());
        }

        // 2. 메시지를 해당 파티의 채팅방으로 즉시 전송 (실시간성 확보)
        messagingTemplate.convertAndSend("/topic/party/" + chatMessageDto.getPartyId(), chatMessageDto);
    }

    @MessageMapping("/chat.updateMessage")
    public void updateMessage(@Payload ChatMessageDto chatMessageDto) {

        // DTO의 senderEmail을 사용하여 권한을 확인하고 메시지 수정
        ChatMessage updatedMessage = chatMessageService.updateMessage(chatMessageDto);

        ChatMessageDto updatedDto = new ChatMessageDto(updatedMessage);
        messagingTemplate.convertAndSend("/topic/party/" + updatedDto.getPartyId(), updatedDto);
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(@Payload ChatMessageDto chatMessageDto) {

        // DTO의 senderEmail을 사용하여 권한을 확인하고 메시지 삭제
        String senderEmail = chatMessageDto.getSenderEmail(); // DTO에서 Email을 가져와 사용
        chatMessageService.deleteMessage(chatMessageDto.getId(), senderEmail);

        ChatMessageDto deletedDto = new ChatMessageDto();
        deletedDto.setId(chatMessageDto.getId());
        deletedDto.setPartyId(chatMessageDto.getPartyId());
        deletedDto.setContent(null);

        messagingTemplate.convertAndSend("/topic/party/" + deletedDto.getPartyId(), deletedDto);
    }

    // HTTP API를 통해 채팅 기록을 가져오는 엔드포인트 추가
    @GetMapping("/history")
    @Operation(summary = "채팅 기록 조회", description = "특정 파티의 채팅 기록을 조회합니다.")
    public Page<ChatMessageDto> getChatHistory(
            @PathVariable Integer partyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return chatMessageService.getChatHistory(partyId, pageable);
    }
}