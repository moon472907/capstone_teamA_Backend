package com.back.domain.party.partyChat.controller;

import com.back.domain.member.entity.Member;
import com.back.domain.party.party.entity.Party;
import com.back.domain.party.paryChat.controller.WebSocketController;
import com.back.domain.party.paryChat.dto.ChatMessageDto;
import com.back.domain.party.paryChat.entity.ChatMessage;
import com.back.domain.party.paryChat.service.ChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WebSocketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private WebSocketController webSocketController;

    private ChatMessageDto chatMessageDto;
    private ChatMessage chatMessage1;
    private ChatMessage chatMessage2;
    private Party party;
    private Member sender;
    private User user;
    private final Integer partyId = 1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webSocketController).build();

        // Mock 객체 생성
        party = mock(Party.class);
        sender = mock(Member.class);
        user = new User("test@example.com", "password", Collections.emptyList());

        // Mock 객체 동작 설정
        lenient().when(party.getId()).thenReturn(partyId);
        lenient().when(sender.getEmail()).thenReturn("test@example.com");

        // DTO 객체 생성 (setter 사용)
        chatMessageDto = new ChatMessageDto();
        chatMessageDto.setPartyId(partyId);
        chatMessageDto.setSenderEmail(sender.getEmail());
        chatMessageDto.setContent("Hello, world!");

        // ChatMessage 객체 빌더 패턴으로 생성
        chatMessage1 = ChatMessage.builder()
                .content("첫 번째 메시지")
                .party(party)
                .sender(sender)
                .build();
        chatMessage2 = ChatMessage.builder()
                .content("두 번째 메시지")
                .party(party)
                .sender(sender)
                .build();
    }

    @Test
    @DisplayName("STOMP 메시지 전송 테스트")
    void sendMessage_shouldSaveAndSendMessage() {
        webSocketController.sendMessage(chatMessageDto, user);

        verify(chatMessageService, times(1)).saveMessage(chatMessageDto);
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/party/" + partyId), eq(chatMessageDto)
        );
    }

    @Test
    @DisplayName("STOMP 메시지 수정 테스트")
    void updateMessage_shouldUpdateAndBroadcast() {
        String updatedContent = "Updated content!";
        chatMessageDto.setId(1);
        chatMessageDto.setContent(updatedContent);

        ChatMessage updatedChatMessage = ChatMessage.builder()
                .content(updatedContent)
                .party(party)
                .sender(sender)
                .build();
        when(chatMessageService.updateMessage(any(ChatMessageDto.class))).thenReturn(updatedChatMessage);

        webSocketController.updateMessage(chatMessageDto, user);

        verify(chatMessageService, times(1)).updateMessage(
                eq(chatMessageDto)
        );
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/party/" + chatMessageDto.getPartyId()), any(ChatMessageDto.class)
        );
    }

    @Test
    @DisplayName("STOMP 메시지 삭제 테스트")
    void deleteMessage_shouldDeleteAndBroadcast() {
        Integer partyId = 1;
        Integer messageId = 10;
        String senderEmail = "test@example.com";

        ChatMessageDto deleteDto = new ChatMessageDto();
        deleteDto.setId(messageId);
        deleteDto.setPartyId(partyId);

        User user = new User(senderEmail, "", Collections.emptyList());

        doNothing().when(chatMessageService).deleteMessage(eq(deleteDto.getId()), eq(senderEmail));

        webSocketController.deleteMessage(deleteDto, user);

        verify(chatMessageService, times(1)).deleteMessage(
                eq(deleteDto.getId()), eq(senderEmail)
        );

        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/party/" + deleteDto.getPartyId()), any(ChatMessageDto.class)
        );
    }

    @Test
    @DisplayName("채팅 기록 조회 HTTP GET 요청 테스트 (페이지네이션)")
    void getChatHistory_shouldReturnPagedChatHistory() throws Exception {
        List<ChatMessage> chatHistory = Arrays.asList(chatMessage2, chatMessage1);
        Pageable pageable = PageRequest.of(0, 20);
        Page<ChatMessage> pagedChatHistory = new PageImpl<>(chatHistory, pageable, chatHistory.size());

        when(chatMessageService.getChatHistory(eq(partyId), any(Pageable.class)))
                .thenReturn(pagedChatHistory);

        mockMvc.perform(get("/api/v1/parties/{partyId}/chat/history", partyId)
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].content").value("두 번째 메시지"))
                .andExpect(jsonPath("$.content[1].content").value("첫 번째 메시지"));
    }
}