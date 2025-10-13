package com.back.domain.level;

import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.level.service.LevelUpService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.reward.entity.ContentType;
import com.back.domain.reward.entity.Reward;
import com.back.domain.reward.entity.RewardContent;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.repository.RewardRepository;
import com.back.domain.title.dto.CreateTitleDto;
import com.back.domain.title.dto.TitleDto;
import com.back.domain.title.service.TitleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LevelUpServiceTest {

    @Autowired private LevelUpService levelUpService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private LevelXPRepository levelXPRepository;
    @Autowired private RewardRepository rewardRepository;
    @Autowired private TitleService titleService;

    // 테스트용 상수 정의
    private final int INITIAL_LEVEL = 1;
    private final int NEXT_LEVEL = 2;
    private final int INITIAL_XP = 0;

    // Level 1 -> Level 2 요구 경험치
    private final int XP_TO_LEVEL2 = 1500;
    // Level 2 -> Level 3 요구 경험치
    private final int XP_TO_LEVEL3 = 2000;

    // 초과 경험치 (레벨업 후 남는 경험치)
    private final int EXCESS_XP = 500;

    // Level 2 달성에 필요한 총 XP (요구치 1500 + 초과분 500 = 2000)
    private final int XP_FOR_LEVELUP_AND_EXCESS = XP_TO_LEVEL2 + EXCESS_XP;

    // 테스트용 칭호 ID
    private int testTitleId;

    @BeforeEach
    void setupData() {
        // 모든 데이터 초기화
        levelXPRepository.deleteAll();
        rewardRepository.deleteAll();

        // 1. LevelXP 데이터 설정
        levelXPRepository.save(new LevelXP(INITIAL_LEVEL, XP_TO_LEVEL2));
        levelXPRepository.save(new LevelXP(NEXT_LEVEL, XP_TO_LEVEL3));

        // 2. 테스트용 칭호 생성 및 ID 저장
        TitleDto createdTitle = titleService.createTitle(new CreateTitleDto("테스트 칭호", "레벨 2 달성", "테스트용 캡션"));
        testTitleId = createdTitle.id();

        // 3. LevelUp Reward 데이터 설정
        List<RewardContent> titleRewardContents = new ArrayList<>();
        titleRewardContents.add(new RewardContent(ContentType.TITLE, testTitleId));

        // Level 2 달성을 요구하는 LEVELUP 보상 생성 (requireValue: 2)
        Reward reward = new Reward(RewardType.LEVELUP, titleRewardContents, NEXT_LEVEL);
        rewardRepository.save(reward);
    }

    private Member createTestMember(int level, int xp, int xpReq) {
        Member member = Member.builder()
                .level(level)
                .xp(xp)
                .xpReq(xpReq)
                .money(0)
                .build();
        if (member.getOwnedTitles() == null) {
            member.setOwnedTitles(new HashSet<>());
        }
        if (member.getOwnedItems() == null) {
            member.setOwnedItems(new HashSet<>());
        }
        return memberRepository.save(member);
    }


    @Test
    @DisplayName("XP 요구치 충족 시 레벨업이 발생하고, 초과 경험치가 다음 레벨로 이월되며, 다음 xpReq가 업데이트된다")
    void checkLevelUp_ShouldLevelUp_WithExcessXP() {
        // GIVEN
        Member member = createTestMember(INITIAL_LEVEL, INITIAL_XP, XP_TO_LEVEL2);
        Integer memberId = member.getId();

        // 레벨업에 충분한 XP를 수동으로 설정
        member.setXp(XP_FOR_LEVELUP_AND_EXCESS); // member.xp = 2000
        memberRepository.save(member);

        // WHEN
        levelUpService.checkLevelUp(memberId);

        // THEN
        Optional<Member> updatedMemberOpt = memberRepository.findById(memberId);
        assertThat(updatedMemberOpt).isPresent();
        Member updatedMember = updatedMemberOpt.get();

        // 1. 레벨 확인
        assertThat(updatedMember.getLevel()).isEqualTo(NEXT_LEVEL); // Level 2로 레벨업
        // 2. XP 이월 확인
        assertThat(updatedMember.getXp()).isEqualTo(EXCESS_XP); // 초과 경험치 500만 남음
        // 3. 다음 요구 XP 확인
        assertThat(updatedMember.getXpReq()).isEqualTo(XP_TO_LEVEL3);
    }


    @Test
    @DisplayName("XP 요구치 충족 시 레벨업이 발생하고, 레벨업 보상으로 칭호가 제대로 지급된다")
    void checkLevelUp_ShouldLevelUp_AndGiveTitleReward() {
        // GIVEN
        Member member = createTestMember(INITIAL_LEVEL, INITIAL_XP, XP_TO_LEVEL2);
        Integer memberId = member.getId();

        // 초기에는 칭호를 가지고 있지 않아야 합니다.
        assertThat(member.getOwnedTitles()).isEmpty();

        // 레벨업에 충분한 XP를 수동으로 설정
        member.setXp(XP_FOR_LEVELUP_AND_EXCESS); // member.xp = 2000
        memberRepository.save(member);

        // WHEN
        levelUpService.checkLevelUp(memberId);

        // THEN
        Optional<Member> updatedMemberOpt = memberRepository.findById(memberId);
        assertThat(updatedMemberOpt).isPresent();
        Member updatedMember = updatedMemberOpt.get();

        // 1. 레벨업 확인
        assertThat(updatedMember.getLevel()).isEqualTo(NEXT_LEVEL); // Level 2로 레벨업

        // 2. 칭호 지급 확인 (핵심 검증)
        // 획득한 칭호 Set이 비어있지 않고, 그 크기가 1인지 확인
        assertThat(updatedMember.getOwnedTitles()).hasSize(1);

        // 획득한 칭호의 ID가 테스트용으로 생성한 칭호 ID와 일치하는지 확인
        assertThat(updatedMember.getOwnedTitles().stream().anyMatch(title -> title.getId() == testTitleId)).isTrue();

        // 3. XP 이월 확인
        assertThat(updatedMember.getXp()).isEqualTo(EXCESS_XP);
    }

    @Test
    @DisplayName("XP가 레벨업 요구치에 미달하면 레벨 및 XP 변화가 없다")
    void checkLevelUp_ShouldNotLevelUp_NoChange() {
        // GIVEN
        Member member = createTestMember(INITIAL_LEVEL, INITIAL_XP, XP_TO_LEVEL2);
        Integer memberId = member.getId();

        // 레벨업에 부족한 XP를 설정 (요구치 1500 미만인 1499 설정)
        int xpBelowLevelUp = XP_TO_LEVEL2 - 1;
        member.setXp(xpBelowLevelUp);
        memberRepository.save(member);

        // WHEN
        levelUpService.checkLevelUp(memberId);

        // THEN
        Member updatedMember = memberRepository.findById(memberId).get();

        // 1. 레벨 확인
        assertThat(updatedMember.getLevel()).isEqualTo(INITIAL_LEVEL); // 레벨 변화 없음
        // 2. XP 확인
        assertThat(updatedMember.getXp()).isEqualTo(xpBelowLevelUp); // XP 변화 없음
        // 3. 다음 요구 XP 확인
        assertThat(updatedMember.getXpReq()).isEqualTo(XP_TO_LEVEL2); // 요구량 변화 없음
    }

    @Test
    @DisplayName("충분한 XP를 부여하여 다중 레벨업이 연속적으로 발생하고, 최종 XP가 이월된다")
    void checkLevelUp_ShouldHandleMultipleLevelUps() {
        // GIVEN
        // Level 1 -> 2 -> 3 연속 레벨업을 위한 충분한 XP 계산
        int totalXpForMultipleLevelUp = XP_TO_LEVEL2 + XP_TO_LEVEL3 + EXCESS_XP; // 1500 + 2000 + 500 = 4000

        Member member = createTestMember(INITIAL_LEVEL, INITIAL_XP, XP_TO_LEVEL2);
        Integer memberId = member.getId();

        member.setXp(totalXpForMultipleLevelUp); // member.xp = 4000
        memberRepository.save(member);

        // Level 3 -> 4 요구 XP 데이터 추가
        final int LEVEL_3 = 3;
        final int XP_TO_LEVEL4 = 2500;
        levelXPRepository.save(new LevelXP(LEVEL_3, XP_TO_LEVEL4));

        // WHEN
        levelUpService.checkLevelUp(memberId);

        // THEN
        Optional<Member> updatedMemberOpt = memberRepository.findById(memberId);
        assertThat(updatedMemberOpt).isPresent();
        Member updatedMember = updatedMemberOpt.get();

        // 1. 레벨 확인
        assertThat(updatedMember.getLevel()).isEqualTo(LEVEL_3); // Level 3으로 레벨업
        // 2. 최종 XP 이월 확인
        assertThat(updatedMember.getXp()).isEqualTo(EXCESS_XP); // 최종 초과 경험치 500만 남음
        // 3. 다음 요구 XP 확인
        assertThat(updatedMember.getXpReq()).isEqualTo(XP_TO_LEVEL4); // Level 4 요구량
    }
}