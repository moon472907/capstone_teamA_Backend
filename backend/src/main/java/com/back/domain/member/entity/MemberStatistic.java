package com.back.domain.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatistic extends BaseEntity {
    private int countDaily = 0;
    private int countWeekly = 0;
    private int countChallenge = 0;

    public void clearDaily() {
        this.countDaily++;
    }

    public void clearWeekly() {
        this.countWeekly++;
    }

    public void clearChallenge() {
        this.countChallenge++;
    }
}