package com.back.domain.reward.service;

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

    public void crearteReward (RewardType rewardType, List<RewardContent> rewardContents, int requiredValue )
    {
        rewardRepository.save(new Reward(rewardType,rewardContents,requiredValue));
    }
    public void findReward()
    {
        rewardRepository.findAll();
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
}
