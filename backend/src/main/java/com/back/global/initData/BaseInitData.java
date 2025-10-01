package com.back.global.initData;

import com.back.domain.item.dto.CreateItemDto;
import com.back.domain.item.entity.ItemType;
import com.back.domain.item.service.ItemService;
import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.reward.entity.ContentType;
import com.back.domain.reward.entity.RewardContent;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.service.RewardService;
import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.service.TitleService;
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
    private final TitleService titleService;
    private final RewardService rewardService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.initAllData();
        };
    }

    @Transactional
    public void initAllData() {
        try {
            createItem();
            createTitle();
            createMember();
            createLevelXP();
            createReward();
        } catch (Exception e) {
            throw new RuntimeException("[initData] Fail: 초기 데이터 생성 실패", e);
        }
    }

    private void createItem() {
        if(itemService.count() > 0) return;
        //TODO 아이템 데이터 수정,추가 필요
        itemService.createItem(new CreateItemDto("아바타1", ItemType.AVATAR));
        itemService.createItem(new CreateItemDto("아바타2", ItemType.AVATAR));
        itemService.createItem(new CreateItemDto("아바타3", ItemType.AVATAR));
        itemService.createItem(new CreateItemDto("가구1", ItemType.FURNITURE));
        itemService.createItem(new CreateItemDto("가구2", ItemType.FURNITURE));
        itemService.createItem(new CreateItemDto("가구3", ItemType.FURNITURE));
        itemService.createItem(new CreateItemDto("의상1", ItemType.CLOTHE));
        itemService.createItem(new CreateItemDto("의상2", ItemType.CLOTHE));
        itemService.createItem(new CreateItemDto("의상3", ItemType.CLOTHE));
        itemService.createItem(new CreateItemDto("배경1", ItemType.BACKGROUND));
        itemService.createItem(new CreateItemDto("배경2", ItemType.BACKGROUND));
        itemService.createItem(new CreateItemDto("배경3", ItemType.BACKGROUND));
    }

    private void createTitle() {
        if(titleService.count() > 0) return;
        //TODO 칭호 데이터 수정 필요
        titleService.createTitle(new CreateTitleDto("칭호 1"));
        titleService.createTitle(new CreateTitleDto("칭호 2"));
        titleService.createTitle(new CreateTitleDto("칭호 3"));
    }

    private void createMember() {
        if(memberService.findByEmail("user1@user.com").isEmpty()) {
            Member user1 = memberService.signup(
                    "user1@user.com",
                    "user123",
                    "유저1"
            );
            memberService.addItem(user1, 1);
            memberService.addItem(user1, 2);
            memberService.addTitle(user1, 1);
            memberService.addTitle(user1, 2);
        }
        if(memberService.findByEmail("user2@user.com").isEmpty()) {
            memberService.signup(
                    "user2@user.com",
                    "user123",
                    "유저2"
            );
        }
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

    private void createReward() {
        //TODO 미션 데이터 수정 필요
        //데일리 미션
        List<RewardContent> dailyRewardContents = new ArrayList<>();
        dailyRewardContents.add(new RewardContent(ContentType.XP, 100));
        dailyRewardContents.add(new RewardContent(ContentType.MONEY, 100));
        rewardService.createReward(RewardType.DAILY, dailyRewardContents, 1);
     // 위클리 미션

        List<RewardContent>  weeklyRewardContents = new ArrayList<>();
        weeklyRewardContents.add(new RewardContent(ContentType.XP ,100));
        weeklyRewardContents.add(new RewardContent(ContentType.MONEY,100));
        rewardService.createReward(RewardType.WEEKLY,  weeklyRewardContents,1);
    // 챌린지 클리어

        List<RewardContent>  challengeRewardContents = new ArrayList<>();
        challengeRewardContents.add(new RewardContent(ContentType.XP ,100));
        challengeRewardContents.add(new RewardContent(ContentType.MONEY,100));
        rewardService.createReward(RewardType.CHALLENGE,  challengeRewardContents,1);
     // 레벨업

        List<RewardContent>  levelup1RewardContents = new ArrayList<>();
        levelup1RewardContents.add(new RewardContent(ContentType.TITLE,1));
        rewardService.createReward(RewardType.LEVELUP,  levelup1RewardContents,1);

        List<RewardContent>  levelup10RewardContents = new ArrayList<>();
        levelup10RewardContents.add(new RewardContent(ContentType.TITLE,2));
        rewardService.createReward(RewardType.LEVELUP,  levelup10RewardContents,10);

        List<RewardContent>  levelup30RewardContents = new ArrayList<>();
        levelup30RewardContents.add(new RewardContent(ContentType.TITLE,3));
        rewardService.createReward(RewardType.LEVELUP,  levelup30RewardContents,30);

        List<RewardContent>  levelup50RewardContents = new ArrayList<>();
        levelup50RewardContents.add(new RewardContent(ContentType.TITLE,4));
        rewardService.createReward(RewardType.LEVELUP,  levelup50RewardContents,50);
    }

}
