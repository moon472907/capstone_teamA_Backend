package com.back.global.redis;

import com.back.domain.game.redis.GameSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class RedisGameStateService {

    private static final String SESSION_KEY_PREFIX = "game:session:";
    private static final Duration SESSION_TTL = Duration.ofHours(2);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper redisObjectMapper;

    public RedisGameStateService(StringRedisTemplate redisTemplate,
                                 @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisObjectMapper = redisObjectMapper;
    }

    public void saveSession(GameSession session) {
        try {
            String key = SESSION_KEY_PREFIX + session.getGameId();
            String json = redisObjectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(key, json, SESSION_TTL);
        } catch (Exception e) {
            log.error("Failed to save game session {}", session.getGameId(), e);
            throw new RuntimeException("게임 세션 저장에 실패했습니다.", e);
        }
    }

    public Optional<GameSession> loadSession(Integer gameId) {
        try {
            String key = SESSION_KEY_PREFIX + gameId;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return Optional.empty();
            return Optional.of(redisObjectMapper.readValue(json, GameSession.class));
        } catch (DataAccessException | IllegalStateException e) {
            // Redis 연결 불가/종료 중(애플리케이션 셧다운 등) — 복구 가능하므로 스택 없이 경량 로깅
            log.warn("Redis unavailable while loading game session {}: {}", gameId, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to load game session {}", gameId, e);
            return Optional.empty();
        }
    }

    public void deleteSession(Integer gameId) {
        redisTemplate.delete(SESSION_KEY_PREFIX + gameId);
    }
}
