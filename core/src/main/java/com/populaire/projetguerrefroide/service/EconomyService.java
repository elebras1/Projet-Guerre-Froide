package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Pipeline;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.system.*;

public class EconomyService {
    private final GameContext gameContext;

    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final PopulationInitializationSystem populationInitializationSystem;
    private final DemographicsPopulationSpreadSystem demographicsPopulationSpreadSystem;
    private final DemographicsProvinceSpreadSystem demographicsProvinceSpreadSystem;
    private final DemographicsRegionInstanceSpreadSystem demographicsRegionInstanceSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private final RGOHireInitializationSystem rgoHireInitializationSystem;
    private final EconomyBuildingHireInitializationSystem economyBuildingHireInitializationSystem;
    private final CountryMarketInitializeSystem countryMarketInitializeSystem;
    private final PopulationEmploymentSynchronizationSystem populationEmploymentSynchronizationSystem;

    private final CountryMarketResetSystem countryMarketResetSystem;
    private final NeedsCostsResetSystem needsCostsResetSystem;
    private final NeedsCostsCalculationSystem needsCostsCalculationSystem;
    private final PopulationConsumptionSystem populationConsumptionSystem;
    private final EconomyBuildingConsumptionSystem economyBuildingConsumptionSystem;
    private final RGOProductionSystem rgoProduceSystem;
    private final RGOSpreadProductionSystem rgoSpreadProductionSystem;
    private final EconomyBuildingProductionSystem economyBuildingProductionSystem;
    private final EconomyBuildingSpreadProductionSystem economyBuildingSpreadProductionSystem;
    private final CountryMarketResolveSystem countryMarketResolveSystem;

    private final Pipeline initPipeline;
    private final Pipeline mainPipeline;

    public EconomyService(GameContext gameContext) {
        this.gameContext = gameContext;
        World ecsWorld = gameContext.getEcsWorld();

        long phaseDemographicsReset = ecsWorld.entity("PhaseDemographicsReset");
        long phasePopInit = ecsWorld.entity("PhasePopInit");
        long phaseSpread = ecsWorld.entity("PhaseSpread");
        long phaseInit = ecsWorld.entity("PhaseInit");
        long phaseSync = ecsWorld.entity("PhaseSync");

        long phaseMarketReset = ecsWorld.entity("PhaseMarketReset");
        long phaseNeedsCosts = ecsWorld.entity("PhaseNeedsCosts");
        long phaseConsumption = ecsWorld.entity("PhaseConsumption");
        long phaseProduction = ecsWorld.entity("PhaseProduction");
        long phaseToMarket = ecsWorld.entity("PhaseToMarket");
        long phaseMarket = ecsWorld.entity("PhaseMarket");

        this.initPipeline = ecsWorld.pipeline("InitEconomyPipeline")
            .with(phasePopInit)
            .with(phaseSpread)
            .with(phaseInit)
            .with(phaseSync)
            .build();

        this.mainPipeline = ecsWorld.pipeline("MainEconomyPipeline")
            .with(phaseDemographicsReset)
            .with(phaseSpread)
            .with(phaseSync)
            .with(phaseMarketReset)
            .with(phaseNeedsCosts)
            .with(phaseConsumption)
            .with(phaseProduction)
            .with(phaseToMarket)
            .with(phaseMarket)
            .build();

        this.demographicsResetSystem = new DemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.countryDemographicsResetSystem = new CountryDemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.populationInitializationSystem = new PopulationInitializationSystem(ecsWorld, phasePopInit);
        this.demographicsPopulationSpreadSystem = new DemographicsPopulationSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsProvinceSpreadSystem = new DemographicsProvinceSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsRegionInstanceSpreadSystem = new DemographicsRegionInstanceSpreadSystem(ecsWorld, phaseSpread);
        this.rgoSizeSystem = new RGOSizeSystem(ecsWorld, phaseInit);
        this.rgoHireInitializationSystem = new RGOHireInitializationSystem(ecsWorld, phaseInit);
        this.economyBuildingHireInitializationSystem = new EconomyBuildingHireInitializationSystem(ecsWorld, phaseInit);
        this.populationEmploymentSynchronizationSystem = new PopulationEmploymentSynchronizationSystem(ecsWorld, phaseSync);
        this.countryMarketInitializeSystem = new CountryMarketInitializeSystem(ecsWorld, phaseInit);

        this.countryMarketResetSystem = new CountryMarketResetSystem(ecsWorld, phaseMarketReset);
        this.needsCostsResetSystem = new NeedsCostsResetSystem(ecsWorld, phaseNeedsCosts);
        this.needsCostsCalculationSystem = new NeedsCostsCalculationSystem(ecsWorld, phaseNeedsCosts);
        this.populationConsumptionSystem = new PopulationConsumptionSystem(ecsWorld, phaseConsumption);
        this.economyBuildingConsumptionSystem = new EconomyBuildingConsumptionSystem(ecsWorld, phaseConsumption);
        this.rgoProduceSystem = new RGOProductionSystem(ecsWorld, phaseProduction);
        this.economyBuildingProductionSystem = new EconomyBuildingProductionSystem(ecsWorld, phaseProduction);
        this.rgoSpreadProductionSystem = new RGOSpreadProductionSystem(ecsWorld, phaseToMarket);
        this.economyBuildingSpreadProductionSystem = new EconomyBuildingSpreadProductionSystem(ecsWorld, phaseToMarket);
        this.countryMarketResolveSystem = new CountryMarketResolveSystem(ecsWorld, phaseMarket);
    }

    public Pipeline getInitPipeline() {
        return initPipeline;
    }

    public Pipeline getMainPipeline() {
        return mainPipeline;
    }
}
