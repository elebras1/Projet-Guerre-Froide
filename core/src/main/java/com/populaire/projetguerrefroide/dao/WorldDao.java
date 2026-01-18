package com.populaire.projetguerrefroide.dao;

import com.populaire.projetguerrefroide.service.MapService;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.service.GameContext;

public interface WorldDao {
    MapService createWorld(GameContext gameContext, QueryRepository queryRepository);
}
