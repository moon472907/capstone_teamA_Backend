package com.back.global.initData;

import com.back.domain.item.dto.ItemDto;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.service.ItemService;
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
    private final ItemService itemService;

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
            createItem();
        } catch (Exception e) {
            throw new RuntimeException("initData 생성 실패", e);
        }
    }

    private void createMember() {
        if(memberService.findByEmail("user1@user.com").isEmpty()) {
            memberService.signup(
                    "user1@user.com",
                    "user123",
                    "유저1"
            );
        }
        if(memberService.findByEmail("user2@user.com").isEmpty()) {
            memberService.signup(
                    "user2@user.com",
                    "user123",
                    "유저2"
            );
        }
    }
    private void createItem() {
        itemService.createItem(new ItemDto("다람쥐", "localhost:8080/images/squirrel.jpg", ItemType.AVATAR) );
        itemService.createItem(new ItemDto("뛰는다람쥐", "localhost:8080/images/jumping_squirrel.jpg", ItemType.AVATAR) );
        itemService.createItem(new ItemDto("먹는다람쥐", "localhost:8080/images/eating_squirrel.jpg", ItemType.AVATAR) );
        itemService.createItem(new ItemDto("다람쥐먹는중", "localhost:8080/images/squirrel_eating.jpg", ItemType.FURNITURE) );
        itemService.createItem(new ItemDto("다람쥐그림", "localhost:8080/images/squirrel_ed.jpg", ItemType.FURNITURE) );
    }
}
