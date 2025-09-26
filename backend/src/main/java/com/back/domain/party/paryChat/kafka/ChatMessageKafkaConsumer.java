package com.back.domain.party.paryChat.kafka;

import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.service.ChatMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChatMessageKafkaConsumer {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chat-messages", groupId = "chat-db-saver-group")
    @Transactional
    public void consumeChatMessage(String message) throws Exception { // throws Exception 추가
        // 1. JSON 문자열을 ChatMessageDto 객체로 변환
        // 메시지 직렬화/역직렬화 오류는 Kafka 설정의 Deserializer가 처리합니다.
        ChatMessageDto chatMessageDto = objectMapper.readValue(message, ChatMessageDto.class);

        // 2. ChatMessageService를 사용하여 DB에 저장
        // 이 saveMessage 메서드 내에서 데이터베이스 오류(CustomException)가 발생하면,
        // 예외가 외부로 던져져 DefaultErrorHandler가 메시지 재처리(Retry)를 시작합니다.
        chatMessageService.saveMessage(chatMessageDto);

        // 메시지 처리가 성공하면 트랜잭션이 커밋되고 Kafka 오프셋이 업데이트됩니다.
    }
}