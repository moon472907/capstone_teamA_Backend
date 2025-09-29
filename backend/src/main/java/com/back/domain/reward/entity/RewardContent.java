package com.back.domain.reward.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class RewardContent {
    ContentType contentType;
    int value;

    public RewardContent(ContentType contentType, int value) {
        this.contentType = contentType;
        this.value = value;
    }

    public RewardContent() {

    }

}
