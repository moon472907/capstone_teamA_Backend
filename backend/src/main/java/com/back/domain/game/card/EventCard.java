package com.back.domain.game.card;

/**
 * 모두의 강대 이벤트 카드 한 종류의 정의.
 *
 * @param key         식별 키
 * @param type        카드 분류 (공격/방어/장학금)
 * @param target      효과 대상 (자신/상대/없음)
 * @param title       카드 제목
 * @param description 카드 설명
 * @param stars       스타 증감 (+획득, -차감, 0 효과없음)
 * @param skipTurn    true면 효과 대신 다음 턴 1회 스킵
 * @param count       덱 내 장수 (확률 가중)
 */
public record EventCard(
        String key,
        CardType type,
        CardTarget target,
        String title,
        String description,
        int stars,
        boolean skipTurn,
        int count
) {}
