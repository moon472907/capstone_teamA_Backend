package com.back.domain.member.entity;

import com.back.domain.item.entity.Item;
import com.back.domain.title.entity.Title;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
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

    // *** 상태 정보 ***
    private int level = 1;
    private int xp = 0;
    private int xpReq = 5000;
    private int money = 0;

    // *** 장착한 칭호/아이템 정보 ***
    @ManyToOne(fetch = FetchType.LAZY)
    private Title title;
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    // *** 보유한 칭호/아이템 정보 ***
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Title> ownedTitles;
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Item> ownedItems;

    // *** 개발자용 정보 ***
    private MemberRole role = MemberRole.USER;
    private String apiKey = null;
    private String socialAccessToken = null;
    @OneToOne(cascade = CascadeType.ALL)
    private MemberStatistic statistic;

    //생성자(회원 가입)
    public Member(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;

        this.title = null;
        this.item = null;
        this.ownedTitles = new HashSet<>();
        this.ownedItems = new HashSet<>();

        this.apiKey = UUID.randomUUID().toString();
    }

    //생성자(SecurityUser용)
    public Member(int id, String email) {
        setId(id);
        this.email = email;
    }

    //칭호 획득
    public void addTitle(Title title) {
        this.ownedTitles.add(title);
    }

    //아이템 획득
    public void addItem(Item item) {
        this.ownedItems.add(item);
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