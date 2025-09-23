package com.back.global.security;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberService memberService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthUserId = "";
        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        String email = "";
        String password = "";
        String name = "";

        switch (providerTypeCode) {
            case "KAKAO" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> kakao_account = (Map<String, Object>) attributes.get("kakao_account");

                email = kakao_account.get("email").toString();
                name = ((Map<String, Object>)kakao_account.get("profile")).get("nickname").toString();
            }
            case "GOOGLE" -> {
                oauthUserId = oAuth2User.getName();
                name = (String) oAuth2User.getAttributes().get("name");
            }
            case "NAVER" -> {
                Map<String, Object> attributes = oAuth2User.getAttributes();
                Map<String, Object> attributesProperties = (Map<String, Object>) attributes.get("response");

                oauthUserId = (String) attributesProperties.get("id");
                name = (String) attributesProperties.get("nickname");
            }
        }
        //email = providerTypeCode + "__%s".formatted(oauthUserId);

        Member member = memberService.social_login(email, password, name);

        return new SecurityUser(
                member.getId(),
                member.getEmail(),
                member.getPassword(),
                member.getAuthorities()
        );
    }
}