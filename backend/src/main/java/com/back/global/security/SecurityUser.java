package com.back.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class SecurityUser extends User {
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
}
