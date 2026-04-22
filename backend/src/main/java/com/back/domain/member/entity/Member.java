package com.back.domain.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
@SoftDelete
public class Member extends BaseEntity {
    // *** 회원 정보 ***
    @Column(unique = true)
    private String email;
    private String password;
    private String name;
    private String code = null;
    private LocalDate birth = LocalDate.of(1, 1, 1);

    //생성자(회원 가입)
    public Member(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;

    }

    //생성자(SecurityUser용)
    public Member(int id, String email) {
        setId(id);
        this.email = email;
    }

    // *** 인증/인가 메서드 ***
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesAsStringList()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    private List<String> getAuthoritiesAsStringList() {
        List<String> authorities = new ArrayList<>();



        return authorities;
    }
}