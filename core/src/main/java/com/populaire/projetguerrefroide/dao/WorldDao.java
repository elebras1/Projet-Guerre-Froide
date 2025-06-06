package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.entity.GameEntities;
import com.populaire.projetguerrefroide.map.World;
import com.populaire.projetguerrefroide.service.GameContext;

public interface WorldDao {
    GameEntities createGameEntities();
    World createWorldThreadSafe(GameEntities gameEntities, GameContext gameContext);
}
