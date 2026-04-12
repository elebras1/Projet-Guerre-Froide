package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Pipeline;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.system.*;

public class EconomyService {
    private final GameContext gameContext;
    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final DemographicsPopulationSpreadSystem demographicsPopulationSpreadSystem;
    private final DemographicsProvinceSpreadSystem demographicsProvinceSpreadSystem;
    private final DemographicsLocalMarketSpreadSystem demographicsLocalMarketSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private final RGOHireInitializationSystem rgoHireInitializationSystem;
    private final EconomyBuildingHireInitializationSystem economyBuildingHireInitializationSystem;
    private final RGOProduceSystem rgoProduceSystem;
    private final Pipeline initPipeline;
    private final Pipeline mainPipeline;

    public EconomyService(GameContext gameContext) {
        this.gameContext = gameContext;

        World ecsWorld = gameContext.getEcsWorld();

        long phaseReset = ecsWorld.entity("PhaseReset");
        long phaseSpread = ecsWorld.entity("PhaseSpread");
        long phaseInit = ecsWorld.entity("PhaseInit");
        long phaseProduce = ecsWorld.entity("PhaseProduce");

        this.initPipeline = ecsWorld.pipeline("InitEconomyPipeline")
            .with(phaseReset)
            .with(phaseSpread)
            .with(phaseInit)
            .build();

        this.mainPipeline = ecsWorld.pipeline("MainEconomyPipeline")
            .with(phaseReset)
            .with(phaseSpread)
            .with(phaseProduce)
            .build();

        this.demographicsResetSystem = new DemographicsResetSystem(this.gameContext.getEcsWorld(), phaseReset);
        this.countryDemographicsResetSystem = new CountryDemographicsResetSystem(this.gameContext.getEcsWorld(), phaseReset);
        this.demographicsPopulationSpreadSystem = new DemographicsPopulationSpreadSystem(this.gameContext.getEcsWorld(), phaseSpread);
        this.demographicsProvinceSpreadSystem = new DemographicsProvinceSpreadSystem(this.gameContext.getEcsWorld(), phaseSpread);
        this.demographicsLocalMarketSpreadSystem = new DemographicsLocalMarketSpreadSystem(this.gameContext.getEcsWorld(), phaseSpread);
        this.rgoSizeSystem = new RGOSizeSystem(this.gameContext.getEcsWorld(), phaseInit);
        this.rgoHireInitializationSystem = new RGOHireInitializationSystem(this.gameContext.getEcsWorld(), phaseInit);
        this.economyBuildingHireInitializationSystem = new EconomyBuildingHireInitializationSystem(this.gameContext.getEcsWorld(), phaseInit);
        this.rgoProduceSystem = new RGOProduceSystem(this.gameContext.getEcsWorld(), phaseProduce);
    }

    public Pipeline getInitPipeline() {
        return this.initPipeline;
    }

    public Pipeline getMainPipeline() {
        return this.mainPipeline;
    }
}
