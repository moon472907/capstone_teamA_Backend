package com.back.domain.member.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatistic extends BaseEntity {
    @OneToOne
    private Member member;

    private int countDaily = 0;
    private int countWeekly = 0;
    private int countChallenge = 0;

    public MemberStatistic(Member member) {
        this.member = member;
    }

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