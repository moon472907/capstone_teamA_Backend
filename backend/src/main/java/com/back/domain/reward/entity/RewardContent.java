package com.back.domain.reward.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
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
