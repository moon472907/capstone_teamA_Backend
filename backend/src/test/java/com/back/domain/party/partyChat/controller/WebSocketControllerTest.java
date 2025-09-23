package com.back.domain.party.partyChat.controller;

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
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

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
    private final Integer partyId = 1;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webSocketController).build();

        chatMessageDto = new ChatMessageDto();
        chatMessageDto.setPartyId(partyId);
        chatMessageDto.setSenderEmail("test@example.com");
        chatMessageDto.setContent("Hello, world!");

        chatMessage1 = new ChatMessage();
        chatMessage1.setContent("첫 번째 메시지");

        chatMessage2 = new ChatMessage();
        chatMessage2.setContent("두 번째 메시지");
    }

    @Test
    @DisplayName("STOMP 메시지 전송 테스트")
    void sendMessage_shouldSaveAndSendMessage() {
        webSocketController.sendMessage(chatMessageDto);

        verify(chatMessageService, times(1)).saveMessage(chatMessageDto);
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/party/" + partyId), eq(chatMessageDto)
        );
    }

    @Test
    @DisplayName("채팅 기록 조회 HTTP GET 요청 테스트")
    void getChatHistory_shouldReturnChatHistory() throws Exception {

        List<ChatMessage> chatHistory = Arrays.asList(chatMessage2, chatMessage1);
        when(chatMessageService.getChatHistory(partyId)).thenReturn(chatHistory);

        mockMvc.perform(get("/api/v1/parties/{partyId}/chat/history", partyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("두 번째 메시지"))
                .andExpect(jsonPath("$[1].content").value("첫 번째 메시지"));
    }
}