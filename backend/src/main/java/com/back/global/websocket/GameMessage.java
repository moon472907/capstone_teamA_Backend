package com.back.global.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMessage<T> {
    private MessageType type;
    private Integer gameId;
    private T payload;
    private long timestamp;

    public static <T> GameMessage<T> of(MessageType type, Integer gameId, T payload) {
        return GameMessage.<T>builder()
                .type(type)
                .gameId(gameId)
                .payload(payload)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
