package com.back.domain.level.service;

import com.back.domain.level.entity.LevelXP;
import com.back.domain.level.repository.LevelXPRepository;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.reward.entity.Reward;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.service.RewardService;
import com.back.global.exception.CustomException;
import com.back.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LevelUpService {

    private final MemberRepository memberRepository;
    private final LevelXPRepository levelXPRepository;
    private final RewardService rewardService;


    // 레벨업 보상 서비스
    @Transactional
    public void checkLevelUp(Integer memberId) {
        log.info("### [LEVELUP] START checkLevelUp for Member ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Member not found with id: " + memberId));

        log.info("    [Current Status] Initial Level: {}, Initial XP: {}", member.getLevel(), member.getXp());

        // 1. 레벨업 체크 및 처리
        checkAndProcessLevelUp(member);

        // 2. 변경된 Member 저장
        memberRepository.save(member);

        log.info("### [LEVELUP] END checkLevelUp. Final Level: {}, Final XP: {}", member.getLevel(), member.getXp());
    }


    public void checkAndProcessLevelUp(Member member) {
        int currentLevel = member.getLevel();
        int currentXp = member.getXp();

        Optional<LevelXP> currentLevelXP = levelXPRepository.findById(currentLevel);

        // 반복 레벨업 체크 루프
        // 루프 조건: 현재 레벨 XP 엔티티가 존재하고, 현재 XP가 해당 레벨의 요구 XP 이상인 경우
        while (currentLevelXP.isPresent() && currentXp >= currentLevelXP.get().getXpToNext()) {

            log.info("    [Level Check] Current Level: {}, Current XP: {}, Required XP for Level {}: {}",
                    currentLevel, currentXp, currentLevel + 1, currentLevelXP.get().getXpToNext());

            // 1. 레벨업 처리
            int requiredXp = currentLevelXP.get().getXpToNext(); // 현재 레벨의 정확한 요구 XP 사용
            int excessXp = currentXp - requiredXp;

            currentLevel++;
            currentXp = excessXp; // XP 리셋 후 초과분 적용

            member.setLevel(currentLevel);
            member.setXp(currentXp);

            log.info("    [LEVEL UP!] Member Leveled Up to {}. Remaining XP: {}", currentLevel, currentXp);

            // 2. 다음 레벨 요구량(xpReq) 업데이트 및 다음 루프 조건 업데이트

            // 레벨이 증가했으므로, 새로운 currentLevel(예: 2)의 LevelXP를 조회합니다.
            Optional<LevelXP> newCurrentLevelXP = levelXPRepository.findById(currentLevel);

            if (newCurrentLevelXP.isPresent()) {
                member.setXpReq(newCurrentLevelXP.get().getXpToNext());
                log.info("    [XP Req Update] Next XP Required: {}", newCurrentLevelXP.get().getXpToNext());
            } else {
                // Level 30+와 같은 고정 요구량 처리
                member.setXpReq(LevelXP.FIXED_XP_REQUIREMENT);
                log.info("    [XP Req Update] Fixed XP Required: {}", LevelXP.FIXED_XP_REQUIREMENT);
            }

            // 다음 반복을 위해 현재 레벨 XP 참조를 업데이트합니다.
            currentLevelXP = newCurrentLevelXP;

            // 3. 레벨업 보상 지급
            List<Reward> rewards = rewardService.findByRewardTypeAndRequireValue(RewardType.LEVELUP, currentLevel);
            if (!rewards.isEmpty()) {
                for (Reward reward : rewards) {
                    // 이 부분에서 예외가 발생하면 롤백되므로, try-catch로 감싸는 것이 안전합니다.
                    rewardService.giveReward(member.getId(), currentLevel, reward.getId());
                    log.info("    [Reward Given] Given Reward ID {} for reaching Level {}", reward.getId(), currentLevel);
                }
            }
        }
    }


}