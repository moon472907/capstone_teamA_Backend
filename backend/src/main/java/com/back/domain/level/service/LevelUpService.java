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

    @Transactional
    public void checkLevelDown(Integer memberId, int xpBeforeRevoke, int revokedXp) {
        log.info("### [LEVELDOWN] START checkLevelDown for Member ID: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "Member not found with id: " + memberId));

        log.info("    [Current Status] Initial Level: {}, XP Before Revoke: {}, Revoked XP: {}",
                member.getLevel(), xpBeforeRevoke, revokedXp);

        checkAndProcessLevelDown(member, xpBeforeRevoke, revokedXp);

        memberRepository.save(member);

        log.info("### [LEVELDOWN] END checkLevelDown. Final Level: {}, Final XP: {}, Final XP Req: {}",
                member.getLevel(), member.getXp(), member.getXpReq());
    }

    private void checkAndProcessLevelDown(Member member, int xpBeforeRevoke, int revokedXp) {
        int currentLevel = member.getLevel();

        // 레벨이 1보다 크고, XP가 0이 되어 롤백이 필요한 상황을 가정
        // (XP가 0이 아닌 다른 값이라면 레벨 다운이 필요 없다고 가정)
        while (currentLevel > 1 && member.getXp() == 0) {

            int previousLevel = currentLevel - 1;

            Optional<LevelXP> previousLevelXP = levelXPRepository.findById(previousLevel);

            if (previousLevelXP.isEmpty()) {
                log.warn("    [LEVEL DOWN FAILED] Previous Level XP requirement not found for Level {}. Aborting rollback.", previousLevel);
                break;
            }

            int xpToNextOfPreviousLevel = previousLevelXP.get().getXpToNext();

            // 1. XP 복원 계산 (요청된 4990 XP로 복원되는 핵심 로직)
            // 복원 XP = (이전 레벨 요구 XP) + (회수 전 잔여 XP) - (회수된 XP)
            // 시나리오: 5000 + 390 - 400 = 4990
            int restoredXp = xpToNextOfPreviousLevel + xpBeforeRevoke - revokedXp;

            // 2. 레벨 다운 처리
            currentLevel = previousLevel;
            member.setLevel(currentLevel);

            // XP 복원 값 설정
            member.setXp(restoredXp);

            log.info("    [LEVEL DOWN!] Member Leveled Down to {}. Restored XP: {}", currentLevel, restoredXp);

            // 3. 다음 레벨 요구량(xpReq) 업데이트
            member.setXpReq(xpToNextOfPreviousLevel);
            log.info("    [XP Req Update] Next XP Required: {}", xpToNextOfPreviousLevel);

            // 4. 레벨 다운 보상 회수 (필요하다면 로직 추가)
            // levelUpRewardService.revokeReward(member.getId(), previousLevel + 1);

            // 단일 레벨 롤백만 처리하고 루프 종료
            break;
        }
    }


}