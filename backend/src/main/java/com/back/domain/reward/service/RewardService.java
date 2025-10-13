package com.back.domain.reward.service;

import com.back.domain.level.service.LevelUpService;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.reward.entity.ContentType;
import com.back.domain.reward.entity.Reward;
import com.back.domain.reward.entity.RewardContent;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {
    private final RewardRepository rewardRepository;
    private final MemberService  memberService;

    @Lazy
    @Autowired
    private LevelUpService levelUpService;

  public void createReward (RewardType rewardType, List<RewardContent> rewardContents, int requiredValue )

    {
        rewardRepository.save(new Reward(rewardType,rewardContents,requiredValue));
    }

    public List<Reward> findReward() {

        return rewardRepository.findAll();
    }

    public void updateReward(int id, RewardType rewardType, List<RewardContent> rewardContents, int requiredValue)
    {
        Reward reward = rewardRepository.findById(id).get();
        reward.setRewardType(rewardType);
        reward.setRewards(rewardContents);
        reward.setRequireValue(requiredValue);
        rewardRepository.save(reward);
    }
    public void deleteReward(int id)
    {
        rewardRepository.deleteById(id);
    }

    public List<Reward> findByRewardType(RewardType rewardType)
    {
       return rewardRepository.findByRewardType(rewardType);
    }


    public List<Reward> findByRewardTypeAndRequireValue(RewardType rewardType,int requireValue)
    {
        return rewardRepository.findByRewardTypeAndRequireValue(rewardType,requireValue);
    }

    public void giveReward(int memberId, int value, int rewardId)
    {
        Reward reward = rewardRepository.findById(rewardId).get();
        Member member = memberService.findById(memberId).get();

        if(value >= reward.getRequireValue())
        {
            for (RewardContent rewardContent : reward.getRewards())
            {
                if(rewardContent.getContentType() == ContentType.XP)
                {
                    member.setXp(member.getXp() + rewardContent.getRewardValue());
                }
                else if(rewardContent.getContentType() == ContentType.MONEY)
                {

                    memberService.modifyStatus(member,member.getLevel(),member.getXp(),member.getMoney()+rewardContent.getRewardValue());
                }
                else if(rewardContent.getContentType() == ContentType.ITEM)
                {
                    memberService.addItem(member,rewardContent.getRewardValue());
                }
                else if(rewardContent.getContentType() == ContentType.TITLE)
                {
                    memberService.addTitle(member,rewardContent.getRewardValue());
                }
            }
        }
    }

    // 데일리/주차/미션 완료 시 즉시 보상 지급
    // DAILYCLEAR, WEEKLYCLEAR, CHALLENGECLEAR 타입 전용

    public void giveRewardByType(Integer memberId, RewardType rewardType) {
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // requireValue=1인 즉시 보상만 조회
        List<Reward> rewards = rewardRepository.findByRewardTypeAndRequireValue(rewardType, 1);

        for (Reward reward : rewards) {
            for (RewardContent content : reward.getRewards()) {
                applyRewardContent(member, content);
            }
        }
    }

    // 완료 취소 시 즉시 보상 회수
    public void revokeRewardByType(Integer memberId, RewardType rewardType) {
        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Reward> rewards = rewardRepository.findByRewardTypeAndRequireValue(rewardType, 1);

        for (Reward reward : rewards) {
            for (RewardContent content : reward.getRewards()) {
                revokeRewardContent(member, content);
            }
        }
    }


    // 통계 업데이트 시 누적 달성 보상 지급
    // DAILY, WEEKLY, CHALLENGE 타입 전용
    public void giveAccumulatedRewards(Integer memberId, int previousValue, int currentValue, RewardType rewardType) {
        if (currentValue <= previousValue) return;

        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Reward> rewards = rewardRepository.findByRewardType(rewardType);

        // 이전값과 현재값 사이의 달성 보상만 지급
        for (Reward reward : rewards) {
            if (reward.getRequireValue() > previousValue &&
                    reward.getRequireValue() <= currentValue) {

                for (RewardContent content : reward.getRewards()) {
                    applyRewardContent(member, content);
                }
            }
        }
    }

    // 통계 감소 시 누적 달성 보상 회수
    public void revokeAccumulatedRewards(Integer memberId, int previousValue, int currentValue, RewardType rewardType) {
        if (currentValue >= previousValue) return;

        Member member = memberService.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<Reward> rewards = rewardRepository.findByRewardType(rewardType);

        // 현재값과 이전값 사이의 달성 보상만 회수
        for (Reward reward : rewards) {
            if (reward.getRequireValue() > currentValue &&
                    reward.getRequireValue() <= previousValue) {

                for (RewardContent content : reward.getRewards()) {
                    revokeRewardContent(member, content);
                }
            }
        }
    }

    //  헬퍼 메서
    private void applyRewardContent(Member member, RewardContent content) {
        switch (content.getContentType()) {
            case XP:
                member.setXp(member.getXp() + content.getRewardValue());
                levelUpService.checkLevelUp(member.getId());
                break;

            case MONEY:
                memberService.modifyStatus(member, member.getLevel(), member.getXp(),
                        member.getMoney() + content.getRewardValue());
                break;

            case ITEM:
                memberService.addItem(member, content.getRewardValue());
                break;

            case TITLE:
                memberService.addTitle(member, content.getRewardValue());
                break;
        }
    }

    private void revokeRewardContent(Member member, RewardContent content) {
        switch (content.getContentType()) {
            case XP:
                member.setXp(Math.max(0, member.getXp() - content.getRewardValue()));
                break;

            case MONEY:
                int newMoney = Math.max(0, member.getMoney() - content.getRewardValue());
                memberService.modifyStatus(member, member.getLevel(), member.getXp(), newMoney);
                break;

            case ITEM:
                break;

            case TITLE:
                break;
        }
    }
}
