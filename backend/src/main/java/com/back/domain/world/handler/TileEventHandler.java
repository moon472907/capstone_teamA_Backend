package com.back.domain.world.handler;

import com.back.domain.game.redis.GameSession;
import com.back.domain.game.redis.PlayerSession;
import com.back.domain.world.entity.Node;
import com.back.domain.world.entity.TileType;

public interface TileEventHandler {

    TileType getSupportedType();

    TileEventResult handle(PlayerSession player, GameSession session, Node tile);
}
