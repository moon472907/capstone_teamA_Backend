package com.back.domain.reward.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class RewardContent {
    @Enumerated(EnumType.STRING)
    ContentType contentType;
    int rewardValue;

    public RewardContent(ContentType contentType, int value) {
        this.contentType = contentType;
        this.rewardValue = value;
    }

    public RewardContent() {

    }

}
