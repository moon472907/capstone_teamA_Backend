package com.back.standard.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class Ut {
    public static class jwt {

        public static class JwtExpiredException extends RuntimeException {
            public JwtExpiredException(String msg) { super(msg); }
        }

        public static class JwtInvalidException extends RuntimeException {
            public JwtInvalidException(String msg) { super(msg); }
        }

        private static SecretKey key(String secret) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }

        public static String toString(String secret, int expireSeconds, Map<String, Object> body) {
            ClaimsBuilder claimsBuilder = Jwts.claims();

            for (Map.Entry<String, Object> entry : body.entrySet()) {
                claimsBuilder.add(entry.getKey(), entry.getValue());
            }

            Claims claims = claimsBuilder.build();

            Date issuedAt = new Date();
            Date expiration = new Date(issuedAt.getTime() + 1000L * expireSeconds);

            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(issuedAt)
                    .expiration(expiration)
                    .signWith(key(secret))
                    .compact();
        }

        public static boolean isValid(String secret, String jwtStr) {
            try {
                Jwts.parser().verifyWith(key(secret)).build().parse(jwtStr);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        public static Map<String, Object> payload(String secret, String jwtStr) {
            try {
                return (Map<String, Object>) Jwts
                        .parser()
                        .verifyWith(key(secret))
                        .build()
                        .parse(jwtStr)
                        .getPayload();
            } catch (ExpiredJwtException e) {
                throw new JwtExpiredException("토큰이 만료되었습니다.");
            } catch (JwtException | IllegalArgumentException e) {
                throw new JwtInvalidException("유효하지 않은 토큰입니다.");
            }
        }
    }
}