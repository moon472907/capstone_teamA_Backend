package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class SecurityUser extends User implements OAuth2User {
    @Getter
    private final int id;

    public SecurityUser(
            int id,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(email, password != null ? password : "", authorities);
        this.id = id;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public String getName() {
        return getUsername();
    }
}