package com.back.domain.game.card;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 이벤트 카드 덱 정의 (총 30장 = 공격 12 + 방어 8 + 장학금 10).
 * 스타 수치는 게임 디자인 확정값(2026-05-25). "점수 = 스타 개수".
 */
public final class CardCatalog {

    private CardCatalog() {}

    public static final List<EventCard> ALL = List.of(
            // ── 공격 카드 (12장) ─────────────────────────────────────
            new EventCard("police", CardType.ATTACK, CardTarget.OPPONENT,
                    "캠퍼스 폴리스에 적발!",
                    "개인형 이동장치 안전 수칙 미이행으로 단속에 딱 걸렸습니다.",
                    -2, false, 3),
            new EventCard("freeloader", CardType.ATTACK, CardTarget.SELF,
                    "팀프로젝트 무임승차 빌런",
                    "조원이 잠수 모드에 들어갔습니다.",
                    -1, false, 2),
            new EventCard("course_fail", CardType.ATTACK, CardTarget.SELF,
                    "수강신청 대 실패",
                    "수강신청날 늦게 일어난 당신, 무한한 우주 공간에 갇혔습니다.",
                    -2, false, 2),
            new EventCard("drinking", CardType.ATTACK, CardTarget.OPPONENT,
                    "회식의 저주",
                    "시험 끝난 기념으로 밤새 달리다가 다음 날 1교시 전공 수업을 못 갔습니다.",
                    -2, false, 2),
            new EventCard("skipper", CardType.ATTACK, CardTarget.OPPONENT,
                    "출튀한 사람",
                    "수업시간에 출튀한 사람을 발견하여 교수님에게 말씀드렸습니다.",
                    -3, false, 1),
            new EventCard("breakup", CardType.ATTACK, CardTarget.SELF,
                    "그렇게 과CC를...",
                    "과CC를 하다가 헤어진 당신, 무인도로 가서 쉬십시오. (다음 턴 스킵)",
                    0, true, 2),

            // ── 방어 카드 (8장) ──────────────────────────────────────
            new EventCard("guardian", CardType.DEFENSE, CardTarget.NONE,
                    "백령 곰두리의 수호",
                    "강원대학교의 마스코트 곰두리가 당신을 보호합니다! 상대의 공격을 1회 방어합니다.",
                    0, false, 8),

            // ── 장학금 카드 (10장) ───────────────────────────────────
            new EventCard("top", CardType.SCHOLARSHIP, CardTarget.SELF,
                    "성적우수 장학금",
                    "학점 관리에 성공하여 전공 수석을 차지하고 등록금 전액을 면제받았습니다.",
                    3, false, 2),
            new EventCard("global", CardType.SCHOLARSHIP, CardTarget.SELF,
                    "KNU미래글로벌인재 장학금",
                    "우수한 수능 및 직전 학기 성적을 인정받아 미래 글로벌 인재로 선발되었습니다.",
                    2, false, 2),
            new EventCard("work", CardType.SCHOLARSHIP, CardTarget.SELF,
                    "학과사랑 근로장학금",
                    "학과장님의 은밀한 호출! 든든한 일꾼으로 발탁되었습니다.",
                    1, false, 2),
            new EventCard("veteran", CardType.SCHOLARSHIP, CardTarget.SELF,
                    "국가유공자 및 자녀장학금",
                    "국가를 위해 헌신하신 숭고한 뜻을 이어받습니다!",
                    2, false, 2),
            new EventCard("capstone", CardType.SCHOLARSHIP, CardTarget.SELF,
                    "캡스톤 디자인 A+",
                    "더 이상 이 학교에서 가르칠 게 없다! 떠나도 좋다는 인정을 받았습니다.",
                    3, false, 2)
    );

    public static final Map<String, EventCard> BY_KEY =
            ALL.stream().collect(Collectors.toMap(EventCard::key, Function.identity()));

    /** count 만큼 반복한 확률 가중 덱. */
    public static final List<EventCard> WEIGHTED_DECK =
            ALL.stream()
                    .flatMap(c -> java.util.Collections.nCopies(c.count(), c).stream())
                    .toList();
}
