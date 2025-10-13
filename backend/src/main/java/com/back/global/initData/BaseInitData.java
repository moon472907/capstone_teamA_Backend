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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


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
//todo 가격 추가
        if (itemService.count() > 0) return;
        itemService.createItem(new CreateItemDto("기본 너츠", ItemType.DEFAULT,100));
        itemService.createItem(new CreateItemDto("가을 옷", ItemType.NATURE,150));
        itemService.createItem(new CreateItemDto("스노쿨링 옷", ItemType.NATURE,150));
        itemService.createItem(new CreateItemDto("겨울 옷", ItemType.NATURE,150));
        itemService.createItem(new CreateItemDto("산타 옷", ItemType.FESTIVAL,170));
        itemService.createItem(new CreateItemDto("할로윈 옷", ItemType.FESTIVAL,170));
        itemService.createItem(new CreateItemDto("수영복", ItemType.SPORTS,190));
        itemService.createItem(new CreateItemDto("복싱 트렁크", ItemType.SPORTS,190));
        itemService.createItem(new CreateItemDto("야구 유니폼", ItemType.SPORTS,190));
        itemService.createItem(new CreateItemDto("농구 유니폼", ItemType.SPORTS,190));
        itemService.createItem(new CreateItemDto("배트맨 옷", ItemType.CHARACTER,200));
        itemService.createItem(new CreateItemDto("슈퍼맨 옷", ItemType.CHARACTER,200));
        itemService.createItem(new CreateItemDto("토끼 후드", ItemType.CHARACTER,200));
        itemService.createItem(new CreateItemDto("왕관 너츠", ItemType.SPECIAL,300));
        itemService.createItem(new CreateItemDto("꼬마 너츠", ItemType.SPECIAL,300));

    }

    private void createTitle() {

        if (titleService.count() > 0) return;

        titleService.createTitle(new CreateTitleDto("새싹 다람쥐 \uD83C\uDF31", "오늘의 미션 1회 달성", "작은 새싹이 숲을 바꾸듯, 당신의 첫 걸음이 시작됐어요."));
        titleService.createTitle(new CreateTitleDto("티끌모아 태산 \uD83C\uDFD4\uFE0F", "오늘의 미션 10회 달성", "작은 목표들이 모여 큰 성취를 이룬다."));
        titleService.createTitle(new CreateTitleDto("근성 다람쥐 \uD83D\uDCAA", "주차별 미션 5회 달성", "한 주 한 주 쌓인 근성이, 결국 당신을 더 강하게 만든다."));
        titleService.createTitle(new CreateTitleDto("람쥐 썬더 ⚡", "미션 목표 3회 클리어", "짧고 강한 도전, 번개처럼 완수해버리는 당신!"));
        titleService.createTitle(new CreateTitleDto("미션 마스터 \uD83C\uDFAF", "미션 목표 10회 클리어", "목표는 깨기 위해 있는 것! 당신은 이미 달성 머신."));
        titleService.createTitle(new CreateTitleDto("열정 다람쥐 \uD83D\uDD25", "레벨 10 달성", "숲을 가득 채운 당신의 열정, 이제 막 불이 붙었어요!"));
        titleService.createTitle(new CreateTitleDto("에이스 다람쥐 \uD83C\uDFC5", "레벨 30 달성", "누가 봐도 인정할 만한 실력! 당신은 이미 숲의 에이스예요."));
        titleService.createTitle(new CreateTitleDto("전설의 다람쥐 \uD83D\uDC51", "레벨 50 달성", "숲의 다람쥐들이 모두 존경하는 이름, 바로 당신이에요."));
    }

    private void createMember() {

        if (memberService.findByEmail("user1@user.com").isEmpty()) {
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
        if (memberService.findByEmail("user2@user.com").isEmpty()) {
            memberService.signup(
                    "user2@user.com",
                    "user123",
                    "유저2"
            );
        }
    }

    private void createLevelXP() {

        if (levelXPRepository.count() > 0) {
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
        dailyRewardContents.add(new RewardContent(ContentType.XP, 400));
        dailyRewardContents.add(new RewardContent(ContentType.MONEY, 10));
        rewardService.createReward(RewardType.DAILYCLEAR, dailyRewardContents, 1);
        // 위클리 미션

        List<RewardContent> weeklyRewardContents = new ArrayList<>();
        weeklyRewardContents.add(new RewardContent(ContentType.XP, 4000));
        weeklyRewardContents.add(new RewardContent(ContentType.MONEY, 80));
        rewardService.createReward(RewardType.WEEKLYCLEAR, weeklyRewardContents, 1);

        // 챌린지 클리어
        List<RewardContent> challengeRewardContents = new ArrayList<>();
        challengeRewardContents.add(new RewardContent(ContentType.XP, 10000));
        challengeRewardContents.add(new RewardContent(ContentType.MONEY, 200));
        rewardService.createReward(RewardType.CHALLENGECLEAR, challengeRewardContents, 1);

        // 칭호 보상

        List<RewardContent> title1RewardContents = new ArrayList<>();
        title1RewardContents.add(new RewardContent(ContentType.TITLE, 1));
        rewardService.createReward(RewardType.DAILY, title1RewardContents, 1);

        List<RewardContent> title2RewardContents = new ArrayList<>();
        title2RewardContents.add(new RewardContent(ContentType.TITLE, 2));
        rewardService.createReward(RewardType.DAILY, title2RewardContents, 10);

        List<RewardContent> title3RewardContents = new ArrayList<>();
        title3RewardContents.add(new RewardContent(ContentType.TITLE, 3));
        rewardService.createReward(RewardType.WEEKLY, title3RewardContents, 5);

        List<RewardContent> title4RewardContents = new ArrayList<>();
        title4RewardContents.add(new RewardContent(ContentType.TITLE, 4));
        rewardService.createReward(RewardType.CHALLENGE, title4RewardContents, 3);

        List<RewardContent> title5RewardContents = new ArrayList<>();
        title5RewardContents.add(new RewardContent(ContentType.TITLE, 5));
        rewardService.createReward(RewardType.CHALLENGE, title5RewardContents, 10);

        List<RewardContent> title6RewardContents = new ArrayList<>();
        title6RewardContents.add(new RewardContent(ContentType.TITLE, 6));
        rewardService.createReward(RewardType.LEVELUP, title6RewardContents, 10);

        List<RewardContent> title7RewardContents = new ArrayList<>();
        title7RewardContents.add(new RewardContent(ContentType.TITLE, 7));
        rewardService.createReward(RewardType.LEVELUP, title7RewardContents, 30);

        List<RewardContent> title8RewardContents = new ArrayList<>();
        title8RewardContents.add(new RewardContent(ContentType.TITLE, 8));
        rewardService.createReward(RewardType.LEVELUP, title8RewardContents, 50);

        }

}
