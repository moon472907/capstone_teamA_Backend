package com.back.global.redis;

import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RedisLockService {

    private static final String LOCK_KEY_PREFIX = "game:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(Integer gameId, String lockValue) {
        String key = LOCK_KEY_PREFIX + gameId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, lockValue, LOCK_TTL);
        return Boolean.TRUE.equals(success);
    }

    public void unlock(Integer gameId, String lockValue) {
        String key = LOCK_KEY_PREFIX + gameId;
        String current = redisTemplate.opsForValue().get(key);
        if (lockValue.equals(current)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * Executes the given action while holding a Redis lock on the game.
     * Throws {@link ErrorCode#GAME_ACTION_LOCKED} if the lock cannot be acquired.
     */
    public <T> T executeWithLock(Integer gameId, Supplier<T> action) {
        String lockValue = UUID.randomUUID().toString();
        if (!tryLock(gameId, lockValue)) {
            throw new CustomException(ErrorCode.GAME_ACTION_LOCKED);
        }
        try {
            return action.get();
        } finally {
            unlock(gameId, lockValue);
        }
    }
}
