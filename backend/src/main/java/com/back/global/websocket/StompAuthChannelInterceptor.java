package com.back.global.websocket;

import com.back.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final MemberService memberService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String token = resolveToken(accessor);
        if (token == null || token.isBlank()) {
            throw new MessagingException("WebSocket 연결에 인증 토큰이 필요합니다.");
        }

        Map<String, Object> payload = memberService.payload(token);
        if (payload == null) {
            throw new MessagingException("유효하지 않은 토큰입니다.");
        }

        int memberId = (int) payload.get("id");
        String email = (String) payload.get("email");
        accessor.setUser(new MemberPrincipal(memberId, email));

        log.debug("WebSocket authenticated: memberId={}", memberId);
        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        String cookieHeader = accessor.getFirstNativeHeader("cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String trimmed = cookie.trim();
                if (trimmed.startsWith("accessToken=")) {
                    return trimmed.substring("accessToken=".length());
                }
            }
        }
        return null;
    }

    public record MemberPrincipal(int memberId, String email) implements Principal {
        @Override
        public String getName() {
            return String.valueOf(memberId);
        }
    }
}
