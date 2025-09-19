package com.back.global.initData;

import com.back.domain.member.entity.MemberGender;
import com.back.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Profile("!prod")
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.initAllData();
        };
    }

    @Transactional
    public void initAllData() {
        try {
            createMember();

        } catch (Exception e) {
            throw new RuntimeException("initData 생성 실패", e);
        }
    }

    private void createMember() {
        if(memberService.findByEmail("user1@user.com").isEmpty()) {
            memberService.signup(
                    "user1@user.com",
                    "user123",
                    "유저1",
                    1,
                    MemberGender.MALE
            );
        }
        if(memberService.findByEmail("user2@user.com").isEmpty()) {
            memberService.signup(
                    "user2@user.com",
                    "user123",
                    "유저2",
                    1,
                    MemberGender.FEMALE
            );
        }
    }
}
