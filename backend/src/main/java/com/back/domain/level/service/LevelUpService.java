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


    private void checkAndProcessLevelUp(Member member) {
        int currentLevel = member.getLevel();
        long currentTotalXp = member.getXp();

        while (true) {
            int nextLevel = currentLevel + 1;
            Long requiredXpForNextLevel; // 다음 레벨 달성에 필요한 총 누적 XP

            // 30레벨 미만: LevelXP 테이블 조회 (변동 구간)
            if (currentLevel < LevelXP.MAX_VARIABLE_LEVEL) {
                Optional<LevelXP> nextLevelInfo = levelXPRepository.findById(nextLevel);
                if (nextLevelInfo.isEmpty()) break;

                requiredXpForNextLevel = nextLevelInfo.get().getCumulativeXP();

            }
            // 30레벨 이상: 고정 경험치 계산 (고정 구간)
            else {
                // 현재 레벨 시작 누적 XP를 계산 (DB 조회 필요)
                Long currentLevelStartXp = getCurrentLevelStartCumulativeXp(currentLevel);
                requiredXpForNextLevel = currentLevelStartXp + LevelXP.FIXED_XP_REQUIREMENT;
            }

            if (currentTotalXp >= requiredXpForNextLevel) {
                // 레벨업 성공
                currentLevel = nextLevel;
                member.setLevel(currentLevel);

                // 보상 지급
                List<Reward> rewards =
                        rewardService.findByRewardTypeAndRequireValue(RewardType.LEVELUP, currentLevel);

                if (!rewards.isEmpty()) { // 보상이 존재하는 경우에만 처리
                    // 레벨업 보상은 해당 레벨에 하나만 있다고 가정하고 첫 번째 요소를 사용
                    Reward reward = rewards.getFirst();

                    rewardService.giveReward(member.getId(), currentLevel, reward.getId());
                }

            } else {
                break; // 레벨업 실패, 반복 종료
            }
        }
    }

    // 현재 레벨의 시작 누적 XP를 계산하는 헬퍼 메서드
    private Long getCurrentLevelStartCumulativeXp(int level) {
        if (level == 1) return 0L;

        // 30레벨 이하: DB에서 해당 레벨의 누적 XP 조회
        if (level <= LevelXP.MAX_VARIABLE_LEVEL) {
            return levelXPRepository.findById(level)
                    // CustomException 사용
                    .orElseThrow(() -> new CustomException(ErrorCode.LEVEL_DATA_NOT_FOUND, "Level XP data for level " + level + " not found"))
                    .getCumulativeXP();
        }

        // 31레벨 이상: 고정 요구량으로 계산
        // 1. Level 30 달성 시점의 누적 XP를 DB에서 조회합니다.
        Long baseCumulativeXp = levelXPRepository.findById(LevelXP.MAX_VARIABLE_LEVEL)
                // CustomException 사용
                .orElseThrow(() -> new CustomException(ErrorCode.LEVEL_DATA_NOT_FOUND, "Base Level XP data (Level 30) not found"))
                .getCumulativeXP();

        // 2. 30레벨을 초과한 레벨 차이만큼 고정 경험치를 더합니다.
        int levelDifference = level - LevelXP.MAX_VARIABLE_LEVEL;

        return baseCumulativeXp + (long)levelDifference * LevelXP.FIXED_XP_REQUIREMENT;
    }
}