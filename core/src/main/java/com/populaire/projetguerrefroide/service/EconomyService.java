package com.populaire.projetguerrefroide.service;

import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.system.*;

public class EconomyService {
    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final PopulationInitializationSystem populationInitializationSystem;
    private final DemographicsPopulationSpreadSystem demographicsPopulationSpreadSystem;
    private final DemographicsProvinceSpreadSystem demographicsProvinceSpreadSystem;
    private final DemographicsRegionInstanceSpreadSystem demographicsRegionInstanceSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private final LawEffectInitializationSystem lawEffectInitializationSystem;
    private final EconomyBuildingOwnerInitializationSystem economyBuildingOwnerInitializationSystem;
    private final CountryMarketInitializationSystem countryMarketInitializationSystem;
    private final GlobalMarketInitializationSystem globalMarketInitializationSystem;
    private final RGOEmploymentSystem rgoEmploymentSystem;
    private final EconomyBuildingEmploymentSystem economyBuildingEmploymentSystem;
    private final PopulationEmploymentSynchronizationSystem populationEmploymentSynchronizationSystem;

    private final GlobalMarketResetSystem globalMarketResetSystem;
    private final CountryMarketResetSystem countryMarketResetSystem;
    private final NeedsCostsResetSystem needsCostsResetSystem;
    private final NeedsCostsCalculationSystem needsCostsCalculationSystem;
    private final PopulationConsumptionSystem populationConsumptionSystem;
    private final EconomyBuildingConsumptionSystem economyBuildingConsumptionSystem;
    private final StockpileDemandSystem stockpileDemandSystem;
    private final RGOProductionSystem rgoProduceSystem;
    private final RGOSpreadProductionSystem rgoSpreadProductionSystem;
    private final EconomyBuildingProductionSystem economyBuildingProductionSystem;
    private final EconomyBuildingSpreadProductionSystem economyBuildingSpreadProductionSystem;
    private final EconomyBuildingScaleSystem economyBuildingScaleSystem;
    private final CountryProductionSpreadSystem countryProductionSpreadSystem;
    private final CountryMarketResolveSystem countryMarketResolveSystem;
    private final CountryMarketSpreadSystem countryMarketSpreadSystem;
    private final GlobalMarketResolveSystem globalMarketResolveSystem;
    private final RegionIncomeResetSystem regionIncomeResetSystem;
    private final RGOProfitSharingSystem rgoProfitSharingSystem;
    private final EconomyBuildingProfitSharingSystem economyBuildingProfitSharingSystem;
    private final CountryIncomeDistributionSystem countryIncomeDistributionSystem;
    private final PopulationIncomeDistributionSystem populationIncomeDistributionSystem;

    private final Pipeline initPipeline;
    private final Pipeline mainPipeline;

    public EconomyService(GameContext gameContext) {
        World ecsWorld = gameContext.getEcsWorld();

        long phaseDemographicsReset = ecsWorld.entity("PhaseDemographicsReset");
        long phasePopInit = ecsWorld.entity("PhasePopInit");
        long phaseSpread = ecsWorld.entity("PhaseSpread");
        long phaseInit = ecsWorld.entity("PhaseInit");
        long phaseEmployment = ecsWorld.entity("PhaseEmployment");
        long phaseSync = ecsWorld.entity("PhaseSync");

        long phaseReset = ecsWorld.entity("PhaseReset");
        long phaseNeedsCosts = ecsWorld.entity("PhaseNeedsCosts");
        long phaseConsumption = ecsWorld.entity("PhaseConsumption");
        long phaseProduction = ecsWorld.entity("PhaseProduction");
        long phaseToMarket = ecsWorld.entity("PhaseToMarket");
        long phaseMarket = ecsWorld.entity("PhaseMarket");
        long phaseIncome = ecsWorld.entity("PhaseIncome");

        this.initPipeline = ecsWorld.pipeline("InitEconomyPipeline")
            .with(phasePopInit)
            .with(phaseSpread)
            .with(phaseInit)
            .with(phaseEmployment)
            .with(phaseSync)
            .build();

        this.mainPipeline = ecsWorld.pipeline("MainEconomyPipeline")
            .with(phaseDemographicsReset)
            .with(phaseSpread)
            .with(phaseEmployment)
            .with(phaseSync)
            .with(phaseReset)
            .with(phaseNeedsCosts)
            .with(phaseConsumption)
            .with(phaseProduction)
            .with(phaseToMarket)
            .with(phaseMarket)
            .with(phaseIncome)
            .build();

        this.demographicsResetSystem = new DemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.countryDemographicsResetSystem = new CountryDemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.populationInitializationSystem = new PopulationInitializationSystem(ecsWorld, phasePopInit);
        this.demographicsPopulationSpreadSystem = new DemographicsPopulationSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsProvinceSpreadSystem = new DemographicsProvinceSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsRegionInstanceSpreadSystem = new DemographicsRegionInstanceSpreadSystem(ecsWorld, phaseSpread);
        this.lawEffectInitializationSystem = new LawEffectInitializationSystem(ecsWorld, phaseInit);
        this.economyBuildingOwnerInitializationSystem = new EconomyBuildingOwnerInitializationSystem(ecsWorld, gameContext.getEcsConstants(), phaseInit);
        this.rgoSizeSystem = new RGOSizeSystem(ecsWorld, phaseInit);
        this.countryMarketInitializationSystem = new CountryMarketInitializationSystem(ecsWorld, phaseInit);
        this.globalMarketInitializationSystem = new GlobalMarketInitializationSystem(ecsWorld, phaseInit);
        this.rgoEmploymentSystem = new RGOEmploymentSystem(ecsWorld, phaseEmployment);
        this.economyBuildingEmploymentSystem = new EconomyBuildingEmploymentSystem(ecsWorld, phaseEmployment);
        this.populationEmploymentSynchronizationSystem = new PopulationEmploymentSynchronizationSystem(ecsWorld, phaseSync);

        this.globalMarketResetSystem = new GlobalMarketResetSystem(ecsWorld, phaseReset);
        this.countryMarketResetSystem = new CountryMarketResetSystem(ecsWorld, phaseReset);
        this.needsCostsResetSystem = new NeedsCostsResetSystem(ecsWorld, phaseReset);
        this.regionIncomeResetSystem = new RegionIncomeResetSystem(ecsWorld, phaseReset);
        this.needsCostsCalculationSystem = new NeedsCostsCalculationSystem(ecsWorld, phaseNeedsCosts);
        this.populationConsumptionSystem = new PopulationConsumptionSystem(ecsWorld, phaseConsumption);
        this.economyBuildingConsumptionSystem = new EconomyBuildingConsumptionSystem(ecsWorld, phaseConsumption);
        this.stockpileDemandSystem = new StockpileDemandSystem(ecsWorld, phaseConsumption);
        this.rgoProduceSystem = new RGOProductionSystem(ecsWorld, phaseProduction);
        this.economyBuildingProductionSystem = new EconomyBuildingProductionSystem(ecsWorld, phaseProduction);
        this.economyBuildingScaleSystem = new EconomyBuildingScaleSystem(ecsWorld, phaseProduction);
        this.rgoSpreadProductionSystem = new RGOSpreadProductionSystem(ecsWorld, phaseToMarket);
        this.economyBuildingSpreadProductionSystem = new EconomyBuildingSpreadProductionSystem(ecsWorld, phaseToMarket);
        this.countryProductionSpreadSystem = new CountryProductionSpreadSystem(ecsWorld, phaseToMarket);
        this.countryMarketResolveSystem = new CountryMarketResolveSystem(ecsWorld, phaseMarket);
        this.countryMarketSpreadSystem = new CountryMarketSpreadSystem(ecsWorld, phaseMarket);
        this.globalMarketResolveSystem = new GlobalMarketResolveSystem(ecsWorld, phaseMarket);
        this.rgoProfitSharingSystem = new RGOProfitSharingSystem(ecsWorld, phaseIncome);
        this.economyBuildingProfitSharingSystem = new EconomyBuildingProfitSharingSystem(ecsWorld, gameContext.getEcsConstants(), phaseIncome);
        this.countryIncomeDistributionSystem = new CountryIncomeDistributionSystem(ecsWorld, phaseIncome);
        this.populationIncomeDistributionSystem = new PopulationIncomeDistributionSystem(ecsWorld, gameContext.getEcsConstants(), phaseIncome);
    }

    public Pipeline getInitPipeline() {
        return initPipeline;
    }

    public Pipeline getMainPipeline() {
        return mainPipeline;
    }
}
