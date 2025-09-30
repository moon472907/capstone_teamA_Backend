package com.back.domain.reward.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Reward extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private RewardType rewardType;

    @ElementCollection
    @CollectionTable(name = "reward_contents", joinColumns = @JoinColumn(name = "reward_id"))
    private List<RewardContent> rewards = new ArrayList<>();

    private int requireValue;

    public Reward(RewardType rewardType, List<RewardContent> rewards, int requireValue) {

        this.rewardType = rewardType;
        this.rewards = rewards;
        this.requireValue = requireValue;
    }
}
