package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.pojo.WorldData;
import com.populaire.projetguerrefroide.service.GameContext;

public interface WorldDao {
    WorldData createWorld(GameContext gameContext);
}
