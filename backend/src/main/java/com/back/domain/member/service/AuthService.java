package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.standard.util.Ut;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MIN_SECRET_BYTES = 32; // HS256: 256-bit minimum
    private static final String INVALIDATED_KEY_PREFIX = "auth:expired_before:";
    private static final Duration INVALIDATED_KEY_TTL = Duration.ofHours(1);

    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    private final StringRedisTemplate redisTemplate;

    @PostConstruct
    void validateSecret() {
        if (jwtSecretKey == null || jwtSecretKey.isBlank()) {
            throw new IllegalStateException("custom.jwt.secretKey 가 설정되지 않았습니다.");
        }
        int len = jwtSecretKey.getBytes(StandardCharsets.UTF_8).length;
        if (len < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret 은 최소 " + MIN_SECRET_BYTES + " 바이트(현재: " + len + ")가 필요합니다.");
        }
    }

    String genAccessToken(Member member) {
        long id = member.getId();
        String email = member.getEmail();

        return Ut.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                Map.of("id", id, "email", email)
        );
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload;
        try {
            parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);
        } catch (Ut.jwt.JwtExpiredException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (Ut.jwt.JwtInvalidException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        int id = (int) parsedPayload.get("id");
        String email = (String) parsedPayload.get("email");

        if (isIssuedBeforeInvalidation(id, parsedPayload.get("iat"))) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "게임 종료로 무효화된 토큰입니다.");
        }

        return Map.of("id", id, "email", email);
    }

    /**
     * Marks all tokens issued for this member up to now as invalid.
     * Called when a game ends so its players must re-authenticate.
     */
    void invalidateMemberTokens(int memberId) {
        redisTemplate.opsForValue().set(
                INVALIDATED_KEY_PREFIX + memberId,
                String.valueOf(System.currentTimeMillis()),
                INVALIDATED_KEY_TTL
        );
    }

    private boolean isIssuedBeforeInvalidation(int memberId, Object iatClaim) {
        if (iatClaim == null) return false;
        String thresholdStr = redisTemplate.opsForValue().get(INVALIDATED_KEY_PREFIX + memberId);
        if (thresholdStr == null) return false;

        long iatMs = toEpochMillis(iatClaim);
        long threshold = Long.parseLong(thresholdStr);
        return iatMs < threshold;
    }

    private static long toEpochMillis(Object iatClaim) {
        if (iatClaim instanceof Number n) return n.longValue() * 1000L;
        if (iatClaim instanceof Date d) return d.getTime();
        return 0L;
    }
}
