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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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


    // 테스트용 상수
    private final int INITIAL_LEVEL = 1;
    private final int INITIAL_XP = 1000;
    private final int NEXT_LEVEL = 2;


    @BeforeEach
    void setupData() {
        // 테스트 전 LevelXP 데이터 초기화 (Level 1, Level 2)
        // Level 1 데이터 (시작 누적 XP: 0L, 다음 레벨 필요 XP: 1500)
        levelXPRepository.save(new LevelXP(INITIAL_LEVEL, 500));
        // Level 2 데이터 (시작 누적 XP: 1500L, 다음 레벨 필요 XP: 1500 + X)
        long REQUIRED_XP_FOR_LEVEL2 = 1500L;
        levelXPRepository.save(new LevelXP(NEXT_LEVEL, 1000));
    }


    @Test
    @DisplayName("XP 요구치 충족 시 레벨업이 발생하고, DB에 저장된 보상을 성공적으로 지급한다")
    void checkLevelUp_ShouldLevelUpAndGiveReward_IntegrationSuccess() {
        // GIVEN

        // 1. 테스트 멤버 저장 (DB가 ID를 자동 생성합니다!)
        Member member = Member.builder()
                .level(INITIAL_LEVEL)
                .xp(INITIAL_XP)
                .money(0)
                .build();
        member = memberRepository.save(member);
        Integer memberId = member.getId();

        // 2. 레벨업 보상 데이터 저장 (DB가 ID를 자동 생성합니다!)
        int MONEY_REWARD_AMOUNT = 500;
        Reward level2Reward = new Reward(
                RewardType.LEVELUP,
                List.of(new RewardContent(ContentType.MONEY, MONEY_REWARD_AMOUNT)),
                NEXT_LEVEL // Level 2 달성 시 지급
        );
        rewardRepository.save(level2Reward);

        // 3. 레벨업에 충분한 XP를 수동으로 설정 (Level 1000 -> 1501)
        int xpToLevelUp = 1501;
        member.setXp(xpToLevelUp);
        memberRepository.save(member); // XP 변경 사항 저장

        // WHEN
        // LevelUpService가 DB에서 member를 로드하고 로직을 수행합니다.
        levelUpService.checkLevelUp(memberId);

        // THEN
        // 1. DB에서 변경된 멤버 정보를 다시 조회합니다.
        Optional<Member> updatedMemberOpt = memberRepository.findById(memberId);
        assertThat(updatedMemberOpt).isPresent();
        Member updatedMember = updatedMemberOpt.get();

        // 2. 레벨 및 XP 변경 확인
        assertThat(updatedMember.getLevel()).isEqualTo(NEXT_LEVEL);
        assertThat(updatedMember.getXp()).isEqualTo(xpToLevelUp);

        // 3. 보상 지급 확인 (MONEY가 500 증가했는지)
        assertThat(updatedMember.getMoney()).isEqualTo(MONEY_REWARD_AMOUNT); // 0 + 500
    }


    @Test
    @DisplayName("XP가 레벨업 요구치에 미달하면 레벨 및 보상 변화가 없다")
    void checkLevelUp_ShouldNotLevelUp_NoChange() {
        // GIVEN

        // 1. 테스트 멤버 저장
        Member member = Member.builder()
                .level(INITIAL_LEVEL)
                .xp(INITIAL_XP)
                .money(0)
                .build();
        member = memberRepository.save(member);
        Integer memberId = member.getId();

        // 2. 레벨업에 부족한 XP를 설정 (Level 1000 -> 1499)
        int xpBelowLevelUp = 1499; // 요구치 1500 미만
        member.setXp(xpBelowLevelUp);
        memberRepository.save(member);

        // WHEN
        levelUpService.checkLevelUp(memberId);

        // THEN
        // 1. DB에서 변경된 멤버 정보를 다시 조회합니다.
        Member updatedMember = memberRepository.findById(memberId).get();

        // 2. 레벨 및 XP 확인
        assertThat(updatedMember.getLevel()).isEqualTo(INITIAL_LEVEL);
        assertThat(updatedMember.getXp()).isEqualTo(xpBelowLevelUp);

        // 3. 머니 보상 미지급 확인
        assertThat(updatedMember.getMoney()).isEqualTo(0);
    }
}