package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.map.WorldManager;
import com.populaire.projetguerrefroide.service.GameContext;

public interface WorldDao {
    WorldManager createWorld(GameContext gameContext);
}
