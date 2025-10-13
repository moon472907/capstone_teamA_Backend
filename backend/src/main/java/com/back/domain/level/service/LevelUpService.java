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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LevelUpService {

    private final MemberRepository memberRepository;
    private final LevelXPRepository levelXPRepository;
    private final RewardService rewardService;


    // 레벨업 보상 서비스
    @Transactional
    public void checkLevelUp(Integer memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Member not found with id: " + memberId));

        // 1. 레벨업 체크 및 처리
        checkAndProcessLevelUp(member);

        // 2. 변경된 Member 저장
        memberRepository.save(member);
    }


    public void checkAndProcessLevelUp(Member member) {
        int currentLevel = member.getLevel();
        int currentXp = member.getXp();

        Optional<LevelXP> nextLevelXP = levelXPRepository.findById(currentLevel + 1);

        while (nextLevelXP.isPresent() && currentXp >= nextLevelXP.get().getXpToNext()) {

            // 1. 레벨업 처리
            int requiredXp = nextLevelXP.get().getXpToNext();
            int excessXp = currentXp - requiredXp;

            currentLevel++;
            currentXp = excessXp; // XP 리셋 후 초과분 적용

            member.setLevel(currentLevel);
            member.setXp(currentXp);

            // 2. 다음 레벨 요구량(xpReq) 업데이트
            Optional<LevelXP> newNextLevelXP = levelXPRepository.findById(currentLevel + 1);
            if (newNextLevelXP.isPresent()) {
                member.setXpReq(newNextLevelXP.get().getXpToNext());
            } else {
                // Level 30+와 같은 고정 요구량 처리 (LevelXP.FIXED_XP_REQUIREMENT)
                member.setXpReq(LevelXP.FIXED_XP_REQUIREMENT);
            }

            // 3. 레벨업 보상 지급
            List<Reward> rewards = rewardService.findByRewardTypeAndRequireValue(RewardType.LEVELUP, currentLevel);
            if (!rewards.isEmpty()) {
                rewardService.giveReward(member.getId(), currentLevel, rewards.getFirst().getId());
            }

            // 다음 레벨 정보 업데이트를 위한 반복
            nextLevelXP = newNextLevelXP;
        }
    }

}