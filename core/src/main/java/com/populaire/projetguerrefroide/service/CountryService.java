package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class CountryService {
    private final GameContext gameContext;

    public CountryService(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public String getColonizerId(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Entity country = ecsWorld.obtainEntity(countryId);
        long countryColonizerId = country.target(ecsConstants.isColonyOf());
        if(countryColonizerId != 0) {
            Entity colony = ecsWorld.obtainEntity(countryColonizerId);
            return colony.getName();
        }

        return null;
    }
}
