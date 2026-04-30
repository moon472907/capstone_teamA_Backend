package com.back.domain.world.handler;

import com.back.domain.world.entity.TileType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TileEventHandlerFactory {

    private final Map<TileType, TileEventHandler> handlers;

    public TileEventHandlerFactory(List<TileEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(TileEventHandler::getSupportedType, h -> h));
    }

    public TileEventHandler getHandler(TileType type) {
        return handlers.getOrDefault(type, handlers.get(TileType.NORMAL));
    }
}
