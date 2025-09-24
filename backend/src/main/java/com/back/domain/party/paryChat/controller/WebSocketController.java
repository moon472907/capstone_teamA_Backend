package com.back.domain.party.paryChat.controller;

import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/chat")
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.sendMessage") // 클라이언트가 메시지를 보내는 경로 (예: /app/chat.sendMessage)
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, @AuthenticationPrincipal User user) {
        // 1. 메시지를 데이터베이스에 저장
        chatMessageDto.setSenderEmail(user.getUsername()); // 인증된 사용자 이메일로 설정
        chatMessageService.saveMessage(chatMessageDto);

        // 2. 메시지를 해당 파티의 채팅방으로 전송
        messagingTemplate.convertAndSend("/topic/party/" + chatMessageDto.getPartyId(), chatMessageDto);
    }

    @MessageMapping("/chat.updateMessage")
    public void updateMessage(@Payload ChatMessageDto chatMessageDto, @AuthenticationPrincipal User user) {
        // 인증된 사용자의 이메일을 DTO에 설정
        chatMessageDto.setSenderEmail(user.getUsername());

        // 메시지를 업데이트하고 반환된 최신 정보를 브로드캐스트합니다.
        ChatMessage updatedMessage = chatMessageService.updateMessage(chatMessageDto); // DTO를 직접 넘기도록 수정
        ChatMessageDto updatedDto = new ChatMessageDto(updatedMessage);
        messagingTemplate.convertAndSend("/topic/party/" + updatedDto.getPartyId(), updatedDto);
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(@Payload ChatMessageDto chatMessageDto, @AuthenticationPrincipal User user) {
        // 인증된 사용자의 이메일을 가져옵니다.
        String senderEmail = user.getUsername();

        // 1. 서비스 메서드를 호출하여 메시지를 삭제합니다. 이 메서드는 이제 반환값이 없습니다.
        chatMessageService.deleteMessage(chatMessageDto.getId(), senderEmail);

        // 2. 클라이언트에 삭제 사실을 알리기 위한 DTO를 생성합니다.
        ChatMessageDto deletedDto = new ChatMessageDto();
        deletedDto.setId(chatMessageDto.getId());
        deletedDto.setPartyId(chatMessageDto.getPartyId());
        deletedDto.setContent(null); // 삭제되었음을 명확히 하기 위해 content를 null로 설정

        // 3. 삭제된 메시지 정보를 브로드캐스트합니다.
        messagingTemplate.convertAndSend("/topic/party/" + deletedDto.getPartyId(), deletedDto);
    }

    // HTTP API를 통해 채팅 기록을 가져오는 엔드포인트 추가
    @GetMapping("/history")
    @Operation(summary = "채팅 기록 조회", description = "특정 파티의 채팅 기록을 조회합니다.")
    public Page<ChatMessage> getChatHistory(
            @PathVariable Integer partyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return chatMessageService.getChatHistory(partyId, pageable);
    }
}