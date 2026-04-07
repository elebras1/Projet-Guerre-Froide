package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Pipeline;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.system.CountryDemographicsResetSystem;
import com.populaire.projetguerrefroide.system.DemographicsResetSystem;
import com.populaire.projetguerrefroide.system.DemographicsSpreadSystem;
import com.populaire.projetguerrefroide.system.RGOSizeSystem;

public class EconomyService {
    private final GameContext gameContext;
    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final DemographicsSpreadSystem demographicsSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private Pipeline initPipeline;
    private Pipeline mainPipeline;

    public EconomyService(GameContext gameContext) {
        this.gameContext = gameContext;

        World ecsWorld = gameContext.getEcsWorld();

        long phaseReset = ecsWorld.entity("PhaseReset");
        long phaseSpread = ecsWorld.entity("PhaseSpread");
        long phaseInitRGO = ecsWorld.entity("PhaseInitRGO");

        this.initPipeline = ecsWorld.pipeline("InitEconomyPipeline")
            .with(phaseReset)
            .with(phaseSpread)
            .with(phaseInitRGO)
            .build();

        this.mainPipeline = ecsWorld.pipeline("MainEconomyPipeline")
            .with(phaseReset)
            .with(phaseSpread)
            .build();

        this.demographicsResetSystem = new DemographicsResetSystem(this.gameContext.getEcsWorld(), phaseReset);
        this.countryDemographicsResetSystem = new CountryDemographicsResetSystem(this.gameContext.getEcsWorld(), phaseReset);
        this.demographicsSpreadSystem = new DemographicsSpreadSystem(this.gameContext.getEcsWorld(), phaseSpread);
        this.rgoSizeSystem = new RGOSizeSystem(this.gameContext.getEcsWorld(), phaseInitRGO);
    }

    public Pipeline getInitPipeline() {
        return this.initPipeline;
    }

    public Pipeline getMainPipeline() {
        return this.mainPipeline;
    }
}
