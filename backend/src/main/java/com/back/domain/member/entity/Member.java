package com.back.domain.member.entity;

import com.back.domain.item.entity.Item;
import com.back.domain.item.entity.ItemType;
import com.back.domain.title.entity.Title;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.*;

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
    @Enumerated(EnumType.STRING)
    private MemberGender gender = MemberGender.NONE;

    // *** 상태 및 장착한 아이템 정보 ***
    private int level = 1;
    private int xp = 0;
    private int money = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private Title title;
    @OneToMany(fetch = FetchType.LAZY)
    @MapKey(name = "type")
    private Map<ItemType, Item> items;

    // *** 개발자용 정보 ***
    private MemberRole role = MemberRole.USER;
    private String apiKey = null;

    //생성자(회원 가입)
    public Member(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;

        this.title = null;
        this.items = new EnumMap<>(ItemType.class);
        for(ItemType type : ItemType.values()) {
            this.items.put(type, null);
        }

        this.apiKey = UUID.randomUUID().toString();
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

        if (this.role == MemberRole.ADMIN) {
            authorities.add("ROLE_ADMIN");
        }

        return authorities;
    }
}