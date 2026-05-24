package com.back.global.websocket;

import com.back.domain.member.service.MemberService;
import com.back.domain.player.repository.PlayerRepository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern GAME_TOPIC = Pattern.compile("^/topic/game/(\\d+)$");

    private final MemberService memberService;
    private final PlayerRepository playerRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand cmd = accessor.getCommand();
        if (StompCommand.CONNECT.equals(cmd)) {
            handleConnect(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(cmd)) {
            handleSubscribe(accessor);
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (token == null || token.isBlank()) {
            throw new MessagingException("WebSocket 연결에 인증 토큰이 필요합니다.");
        }

        Map<String, Object> payload;
        try {
            payload = memberService.payload(token);
        } catch (Exception e) {
            throw new MessagingException("유효하지 않은 토큰입니다: " + e.getMessage());
        }

        int memberId = (int) payload.get("id");
        String email = (String) payload.get("email");
        accessor.setUser(new MemberPrincipal(memberId, email));

        log.debug("WebSocket authenticated: memberId={}", memberId);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (!(user instanceof MemberPrincipal principal)) {
            throw new MessagingException("인증되지 않은 세션입니다.");
        }

        String destination = accessor.getDestination();
        if (destination == null) return;

        Matcher m = GAME_TOPIC.matcher(destination);
        if (!m.matches()) return; // 게임 토픽이 아니면 별도 검증 없음

        int gameId = Integer.parseInt(m.group(1));
        boolean isPlayer = playerRepository.existsByGameIdAndMemberId(gameId, principal.memberId());
        if (!isPlayer) {
            log.warn("Subscribe denied: memberId={} not a player of gameId={}", principal.memberId(), gameId);
            throw new MessagingException("해당 게임의 플레이어가 아닙니다.");
        }
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
