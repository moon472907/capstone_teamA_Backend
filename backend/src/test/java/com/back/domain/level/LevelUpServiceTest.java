package com.back.domain.level;

import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.level.service.LevelUpService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.reward.repository.RewardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class LevelUpServiceTest {

    @Autowired private LevelUpService levelUpService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private LevelXPRepository levelXPRepository;
    @Autowired private RewardRepository rewardRepository;


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


    @BeforeEach
    void setupData() {
        // LevelXP 데이터를 현재 레벨 XP 로직에 맞게 설정
        levelXPRepository.deleteAll();

        // Level 1 데이터: Level 2로 가는데 1500 필요
        levelXPRepository.save(new LevelXP(INITIAL_LEVEL, XP_TO_LEVEL2));
        // Level 2 데이터: Level 3으로 가는데 2000 필요
        levelXPRepository.save(new LevelXP(NEXT_LEVEL, XP_TO_LEVEL3));

        // 레벨업 보상 지급 로직을 회피하기 위해 Reward 데이터를 삭제합니다.
        rewardRepository.deleteAll();
    }


    @Test
    @DisplayName("XP 요구치 충족 시 레벨업이 발생하고, 초과 경험치가 다음 레벨로 이월되며, 다음 xpReq가 업데이트된다")
    void checkLevelUp_ShouldLevelUp_WithExcessXP() {
        // GIVEN
        Member member = Member.builder()
                .level(INITIAL_LEVEL)
                .xp(INITIAL_XP) // INITIAL_XP = 0
                .xpReq(XP_TO_LEVEL2) // Level 2 요구 XP
                .money(0) // 돈은 검증 대상이 아님
                .build();
        member = memberRepository.save(member);
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
    @DisplayName("XP가 레벨업 요구치에 미달하면 레벨 및 XP 변화가 없다")
    void checkLevelUp_ShouldNotLevelUp_NoChange() {
        // GIVEN
        Member member = Member.builder()
                .level(INITIAL_LEVEL)
                .xp(INITIAL_XP)
                .xpReq(XP_TO_LEVEL2)
                .money(0)
                .build();
        member = memberRepository.save(member);
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

        Member member = Member.builder()
                .level(INITIAL_LEVEL)
                .xp(INITIAL_XP)
                .xpReq(XP_TO_LEVEL2)
                .money(0)
                .build();
        member = memberRepository.save(member);
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