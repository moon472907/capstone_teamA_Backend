package com.back.domain.reward.repository;

import com.back.domain.reward.entity.Reward;
import com.back.domain.reward.entity.RewardType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward,Integer> {

    List<Reward> findByRewardType(RewardType rewardType);

}
