package com.back.domain.party.party.entity;

public enum PartyMemberStatus {
    PENDING,    // 파티 가입 신청 대기 (멤버 -> 리더)
    INVITED,    // 파티 초대 대기 (리더 -> 멤버)
    ACCEPTED,   // 신청/초대 수락
    REJECTED,   // 신청/초대 거절
    COMPLETED,  // 미션 완료
    LEFT        // 도중에 나감 또는 중도 포기
}