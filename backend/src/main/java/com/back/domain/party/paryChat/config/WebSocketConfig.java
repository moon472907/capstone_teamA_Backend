package com.back.domain.party.paryChat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 메시지를 구독하는 엔드포인트 설정 (예: /topic/party/{partyId})
        registry.enableSimpleBroker("/topic");
        // 클라이언트가 서버로 메시지를 보내는 엔드포인트 설정 (예: /app/chat)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 연결을 위한 STOMP 엔드포인트 설정
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns(
                "http://localhost:3000",
                "https://www.nutree.noredsun.com")
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(1024 * 1024); // 예: 1MB로 메시지 크기 제한
    }
}