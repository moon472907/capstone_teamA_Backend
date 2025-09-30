package com.back.domain.member.service;

import com.back.domain.member.entity.Member;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import com.back.standard.util.Ut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class AuthService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private int accessTokenExpirationSeconds;

    private final String kakaoURL = "https://kapi.kakao.com/v1/user/unlink";
    private final String googleURL = "https://oauth2.googleapis.com/revoke";
    private final String naverURL = "https://nid.naver.com/oauth2.0/token";

    @Value("${SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__NAVER__CLIENT_ID}")
    private String naverClientId;
    @Value("${SPRING__SECURITY__OAUTH2__CLIENT__REGISTRATION__NAVER__CLIENT_SECRET}")
    private String naverClientSecret;

    public void delete_social(String provider, String accessToken) {
        HttpClient client= HttpClient.newHttpClient();
        try {
            HttpRequest request = null;
            switch (provider) {
                case "KAKAO" -> {
                    request = HttpRequest.newBuilder()
                            .uri(URI.create(kakaoURL))
                            .header("Authorization", "Bearer " + accessToken)
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                }
                case "GOOGLE" -> {
                    request = HttpRequest.newBuilder()
                            .uri(URI.create(googleURL + "?token=" + accessToken))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                }
                case "NAVER" -> {
                    request = HttpRequest.newBuilder()
                            .uri(URI.create(naverURL +
                                            "?grant_type=delete" +
                                            "&access_token=" + accessToken +
                                            "&client_id=" + naverClientId +
                                            "&client_secret=" + naverClientSecret
                            ))
                            .POST(HttpRequest.BodyPublishers.noBody())
                            .build();
                }
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.printf("[회원 탈퇴] Code: %s / Body: %s\n", response.statusCode(), response.body());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "[Member] Fail: 소셜 탈퇴");
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
        Map<String, Object> parsedPayload = Ut.jwt.payload(jwtSecretKey, accessToken);

        if (parsedPayload == null) return null;

        int id = (int) parsedPayload.get("id");
        String email = (String) parsedPayload.get("email");

        return Map.of("id", id, "email", email);
    }
}