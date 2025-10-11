package com.back.domain.party.party.entity;

public enum PartyMemberStatus {
    PENDING,    // 초대 대기
    ACCEPTED,   // 파티원
    COMPLETED,  // 미션 완료
    LEFT        // 도중에 나감 또는 중도 포기
}