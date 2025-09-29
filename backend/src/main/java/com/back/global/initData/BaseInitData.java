package com.back.global.initData;

import com.back.domain.item.dto.CreateItemDto;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.service.ItemService;
import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Profile("!prod")
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;
    private final ItemService itemService;
    private final LevelXPRepository levelXPRepository;

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
            createLevelXP();
        } catch (Exception e) {
            throw new RuntimeException("[initData] Fail: 초기 데이터 생성 실패", e);
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
        itemService.createItem(new CreateItemDto("다람쥐", ItemType.AVATAR) );
        itemService.createItem(new CreateItemDto("뛰는다람쥐", ItemType.AVATAR) );
        itemService.createItem(new CreateItemDto("먹는다람쥐", ItemType.AVATAR) );
        itemService.createItem(new CreateItemDto("다람쥐먹는중", ItemType.FURNITURE) );
        itemService.createItem(new CreateItemDto("다람쥐그림", ItemType.FURNITURE) );
    }

    private void createLevelXP() {
        if(levelXPRepository.count() > 0) {
            return;
        }

        List<LevelXP> xpList = new ArrayList<>();

        xpList.add(new LevelXP(2, 5332, 5000L));
        xpList.add(new LevelXP(3, 5685, 10332L));
        xpList.add(new LevelXP(4, 6060, 16017L));
        xpList.add(new LevelXP(5, 6461, 22077L));
        xpList.add(new LevelXP(6, 6887, 28538L));
        xpList.add(new LevelXP(7, 7341, 35425L));
        xpList.add(new LevelXP(8, 7823, 42766L));
        xpList.add(new LevelXP(9, 8336, 50589L));
        xpList.add(new LevelXP(10, 8881, 58925L));
        xpList.add(new LevelXP(11, 9461, 67806L));
        xpList.add(new LevelXP(12, 10077, 77267L));
        xpList.add(new LevelXP(13, 10732, 87344L));
        xpList.add(new LevelXP(14, 11429, 98076L));
        xpList.add(new LevelXP(15, 12170, 109505L));
        xpList.add(new LevelXP(16, 12958, 121675L));
        xpList.add(new LevelXP(17, 13795, 134633L));
        xpList.add(new LevelXP(18, 14685, 148428L));
        xpList.add(new LevelXP(19, 15630, 163113L));
        xpList.add(new LevelXP(20, 16634, 178743L));
        xpList.add(new LevelXP(21, 17699, 195377L));
        xpList.add(new LevelXP(22, 18830, 213076L));
        xpList.add(new LevelXP(23, 20031, 231906L));
        xpList.add(new LevelXP(24, 21305, 251937L));
        xpList.add(new LevelXP(25, 22657, 273242L));
        xpList.add(new LevelXP(26, 24091, 295899L));
        xpList.add(new LevelXP(27, 25612, 319990L));
        xpList.add(new LevelXP(28, 27225, 345602L));
        xpList.add(new LevelXP(29, 28935, 372827L));
        xpList.add(new LevelXP(30, 30000, 401762L));

        levelXPRepository.saveAll(xpList);
    }
}
