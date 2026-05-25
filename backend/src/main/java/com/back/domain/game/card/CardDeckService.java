package com.back.domain.game.card;

import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * 이벤트 카드 한 장을 확률 가중 덱에서 무작위로 뽑는다.
 * (덱은 무한 — 매번 가중 덱에서 독립적으로 추출, 소진/셔플 개념 없음)
 */
@Service
public class CardDeckService {

    private final Random random = new Random();

    public EventCard draw() {
        var deck = CardCatalog.WEIGHTED_DECK;
        return deck.get(random.nextInt(deck.size()));
    }

    public EventCard byKey(String key) {
        return CardCatalog.BY_KEY.get(key);
    }
}
