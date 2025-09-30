package com.back.domain.reward.service;

import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.domain.reward.entity.ContentType;
import com.back.domain.reward.entity.Reward;
import com.back.domain.reward.entity.RewardContent;
import com.back.domain.reward.entity.RewardType;
import com.back.domain.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {
    private  final RewardRepository rewardRepository;
    private final MemberService  memberService;

  public void createReward (RewardType rewardType, List<RewardContent> rewardContents, int requiredValue )

    {
        rewardRepository.save(new Reward(rewardType,rewardContents,requiredValue));
    }

    public List<Reward> findReward() {

        return rewardRepository.findAll();


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
                    memberService.modifyStatus(member,0,0,0);

                    //TODO: XP 보상 로직 추가
                }
                else if(rewardContent.getContentType() == ContentType.MONEY)
                {

                    memberService.modifyStatus(member,member.getLevel(),member.getXp(),member.getMoney()+rewardContent.getValue());
                }
                else if(rewardContent.getContentType() == ContentType.ITEM)
                {
                    memberService.modifyItem(member,rewardContent.getValue());
                }
                else if(rewardContent.getContentType() == ContentType.TITLE)
                {
                    memberService.modifyTitle(member,rewardContent.getValue());
                }
            }
        }
    }

}
