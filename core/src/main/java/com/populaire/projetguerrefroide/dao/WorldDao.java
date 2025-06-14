package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.map.World;
import com.populaire.projetguerrefroide.service.GameContext;

public interface WorldDao {
    World createWorldThreadSafe(GameContext gameContext);
}
