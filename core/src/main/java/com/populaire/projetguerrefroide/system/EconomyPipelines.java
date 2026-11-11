package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.artisan.*;
import com.populaire.projetguerrefroide.system.building.*;
import com.populaire.projetguerrefroide.system.construction.*;
import com.populaire.projetguerrefroide.system.market.*;
import com.populaire.projetguerrefroide.system.military.*;
import com.populaire.projetguerrefroide.system.population.*;
import com.populaire.projetguerrefroide.system.region.*;
import com.populaire.projetguerrefroide.system.rgo.*;
import com.populaire.projetguerrefroide.system.state.*;
import com.populaire.projetguerrefroide.system.technology.*;

import static com.populaire.projetguerrefroide.simulation.Constants.INITIAL_SAVINGS_PASSES;

public class EconomyPipelines {
    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final PopulationInitializationSystem populationInitializationSystem;
    private final PopulationRegionInitializationSystem populationRegionInitializationSystem;
    private final DemographicsPopulationSpreadSystem demographicsPopulationSpreadSystem;
    private final DemographicsProvinceSpreadSystem demographicsProvinceSpreadSystem;
    private final DemographicsRegionInstanceSpreadSystem demographicsRegionInstanceSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private final LawEffectInitializationSystem lawEffectInitializationSystem;
    private final TechnologyEffectInitializationSystem technologyEffectInitializationSystem;
    private final DevelopmentIndexSystem developmentIndexSystem;
    private final ResearchSystem researchSystem;
    private final RGOMechanizationDemandSystem rgoMechanizationDemandSystem;
    private final RGOMechanizationInitializationSystem rgoMechanizationInitializationSystem;
    private final RGOMechanizationUpkeepInitializationSystem rgoMechanizationUpkeepInitializationSystem;
    private final RGOMechanizationPurchaseSystem rgoMechanizationPurchaseSystem;
    private final EconomyBuildingRecipeSystem economyBuildingRecipeSystem;
    private final EconomyBuildingOwnerInitializationSystem economyBuildingOwnerInitializationSystem;
    private final CountryMarketInitializationSystem countryMarketInitializationSystem;
    private final GlobalMarketInitializationSystem globalMarketInitializationSystem;
    private final RGOEmploymentInitializationSystem rgoEmploymentInitializationSystem;
    private final RGOEmploymentSystem rgoEmploymentSystem;
    private final EconomyBuildingWorkerDemandSystem economyBuildingWorkerDemandSystem;
    private final EconomyBuildingEmploymentSystem economyBuildingEmploymentSystem;
    private final PopulationEmploymentSynchronizationSystem populationEmploymentSynchronizationSystem;

    private final GlobalMarketResetSystem globalMarketResetSystem;
    private final CountryMarketResetSystem countryMarketResetSystem;
    private final NeedsCostsResetSystem needsCostsResetSystem;
    private final CountryBudgetPriceSystem countryBudgetPriceSystem;
    private final NeedsCostsCalculationSystem needsCostsCalculationSystem;
    private final PopulationConsumptionSystem populationConsumptionSystem;
    private final CountryPopulationDemandSystem countryPopulationDemandSystem;
    private final EconomyBuildingConsumptionSystem economyBuildingConsumptionSystem;
    private final RGOProductionSystem rgoProduceSystem;
    private final RGOSpreadProductionSystem rgoSpreadProductionSystem;
    private final EconomyBuildingProductionSystem economyBuildingProductionSystem;
    private final EconomyBuildingSpreadProductionSystem economyBuildingSpreadProductionSystem;
    private final EconomyBuildingUtilizationSystem economyBuildingUtilizationSystem;
    private final SpecialBuildingPostSystem specialBuildingPostSystem;
    private final StatePostSystem statePostSystem;
    private final MilitaryPostSystem militaryPostSystem;
    private final ArtisanEmploymentSystem artisanEmploymentSystem;
    private final ArtisanAllocationSystem artisanAllocationSystem;
    private final ArtisanDemandSystem artisanDemandSystem;
    private final ArtisanPurchaseSystem artisanPurchaseSystem;
    private final ArtisanProductionSystem artisanProductionSystem;
    private final ArtisanRevenueSystem artisanRevenueSystem;
    private final ArtisanIncomeSystem artisanIncomeSystem;
    private final ArtisanTreasuryInitializationSystem artisanTreasuryInitializationSystem;
    private final ArmyDisbandSystem armyDisbandSystem;
    private final InfrastructureInitializationSystem infrastructureInitializationSystem;
    private final RegionProvinceCountSystem regionProvinceCountSystem;
    private final StateConsumptionDemandSystem stateConsumptionDemandSystem;
    private final StateConsumptionPurchaseSystem stateConsumptionPurchaseSystem;
    private final PublicWorksSystem publicWorksSystem;
    private final CountryCentralBankSystem countryCentralBankSystem;
    private final MaintenanceSystem maintenanceSystem;
    private final StrategicStockDemandSystem strategicStockDemandSystem;
    private final StrategicStockPurchaseSystem strategicStockPurchaseSystem;
    private final StrategicStockReleaseSystem strategicStockReleaseSystem;
    private final StrategicStockPlanSystem strategicStockPlanSystem;
    private final MaintenanceDemandSystem maintenanceDemandSystem;
    private final MaintenancePurchaseSystem maintenancePurchaseSystem;
    private final MilitarySupplyResetSystem militarySupplyResetSystem;
    private final BankSystem bankSystem;
    private final MilitaryBaseCapacitySystem militaryBaseCapacitySystem;
    private final MilitarySupplyNeedSystem militarySupplyNeedSystem;
    private final MilitarySupplyDemandSystem militarySupplyDemandSystem;
    private final MilitarySupplyPurchaseSystem militarySupplyPurchaseSystem;
    private final MilitaryOrganisationSystem militaryOrganisationSystem;
    private final LabourMarketSystem labourMarketSystem;
    private final PopulationApplicationSystem populationApplicationSystem;
    private final ProvinceDemographySystem provinceDemographySystem;
    private final PopulationGrowthSystem populationGrowthSystem;
    private final PopulationPromotionSystem populationPromotionSystem;
    private final PopulationArrivalSystem populationArrivalSystem;
    private final EconomyCalendarSystem economyCalendarSystem;
    private final EconomyBuildingWorkingCapitalSystem economyBuildingWorkingCapitalSystem;
    private final EconomyBuildingInputStockInitializationSystem economyBuildingInputStockInitializationSystem;
    private final CountryStockSystem countryStockSystem;
    private final CountryProductionSpreadSystem countryProductionSpreadSystem;
    private final EconomyBuildingTreasuryInitializationSystem economyBuildingTreasuryInitializationSystem;
    private final RGOTreasuryInitializationSystem rgoTreasuryInitializationSystem;
    private final CountryTreasuryInitializationSystem countryTreasuryInitializationSystem;
    private final GoodAvailabilityInitializationSystem goodAvailabilityInitializationSystem;
    private final CountryStockInitializationSystem countryStockInitializationSystem;
    private final WorldMarketTotalsSystem worldMarketTotalsSystem;
    private final GlobalTradeRatioSystem globalTradeRatioSystem;
    private final CountryMarketResolveSystem countryMarketResolveSystem;
    private final CountryMarketSpreadSystem countryMarketSpreadSystem;
    private final CountryPurchaseSystem countryPurchaseSystem;
    private final EconomyBuildingPurchaseSystem economyBuildingPurchaseSystem;
    private final CountryImportPaymentSystem countryImportPaymentSystem;
    private final CountryExportPayoutSystem countryExportPayoutSystem;
    private final WorldTradeMoneySettleSystem worldTradeMoneySettleSystem;
    private final CountryPriceSystem countryPriceSystem;
    private final GlobalMarketResolveSystem globalMarketResolveSystem;
    private final CountryProductionValueSystem countryProductionValueSystem;
    private final RGORevenueSystem rgoRevenueSystem;
    private final EconomyBuildingRevenueSystem economyBuildingRevenueSystem;
    private final CountryRevenueSettleSystem countryRevenueSettleSystem;
    private final RegionIncomeResetSystem regionIncomeResetSystem;
    private final RegionIncomeResetSystem regionIncomePreEmploymentResetSystem;
    private final RGOProfitSharingSystem rgoProfitSharingSystem;
    private final EconomyBuildingProfitSharingSystem economyBuildingProfitSharingSystem;
    private final CountryIncomeDistributionSystem countryIncomeDistributionSystem;
    private final PopulationIncomeDistributionSystem populationIncomeDistributionSystem;
    private final PopulationSavingsInitializationSystem populationSavingsInitializationSystem;
    private final ReligiousDonationSystem religiousDonationSystem;
    private final RegionIncomeResidualSystem regionIncomeResidualSystem;
    private final CountryBudgetSystem countryBudgetSystem;
    private final CountryInvestmentResetSystem countryInvestmentResetSystem;
    private final ConstructionDemandSystem constructionDemandSystem;
    private final ConstructionPurchaseSystem constructionPurchaseSystem;
    private final ConstructionCompletionSystem constructionCompletionSystem;
    private final ConstructionClosureSystem constructionClosureSystem;
    private final InvestmentDecisionSystem investmentDecisionSystem;
    private final EconomyBuildingClosureSystem economyBuildingClosureSystem;

    private final World ecsWorld;
    private final EcsConstants ecsConstants;
    private final Pipeline initPipeline;
    private final Pipeline initBudgetPipeline;
    private final Pipeline initSavingsPipeline;
    private final Pipeline initTreasuryPipeline;
    private final Pipeline mainPipeline;

    public EconomyPipelines(SimulationContext simulationContext) {
        World ecsWorld = simulationContext.getEcsWorld();
        this.ecsWorld = ecsWorld;
        this.ecsConstants = simulationContext.getEcsConstants();

        long phaseDemographicsReset = ecsWorld.entity("PhaseDemographicsReset");
        long phasePopInit = ecsWorld.entity("PhasePopInit");
        long phaseSpread = ecsWorld.entity("PhaseSpread");
        long phaseInit = ecsWorld.entity("PhaseInit");
        long phaseRgoEmployment = ecsWorld.entity("PhaseRgoEmployment");
        long phaseEmployment = ecsWorld.entity("PhaseEmployment");
        long phaseSync = ecsWorld.entity("PhaseSync");

        long phaseReset = ecsWorld.entity("PhaseReset");
        long phaseNeedsCosts = ecsWorld.entity("PhaseNeedsCosts");
        long phaseConsumption = ecsWorld.entity("PhaseConsumption");
        long phaseMarket = ecsWorld.entity("PhaseMarket");
        long phaseSettlement = ecsWorld.entity("PhaseSettlement");
        long phaseProduction = ecsWorld.entity("PhaseProduction");
        long phaseToStock = ecsWorld.entity("PhaseToStock");
        long phasePrice = ecsWorld.entity("PhasePrice");
        long phaseRevenue = ecsWorld.entity("PhaseRevenue");
        long phaseIncome = ecsWorld.entity("PhaseIncome");
        long phaseState = ecsWorld.entity("PhaseState");
        long phaseInitTreasury = ecsWorld.entity("PhaseInitTreasury");
        long phaseInitAvailability = ecsWorld.entity("PhaseInitAvailability");
        long phaseInitStock = ecsWorld.entity("PhaseInitStock");
        long phaseInitBudget = ecsWorld.entity("PhaseInitBudget");
        long phaseInitSavings = ecsWorld.entity("PhaseInitSavings");
        long phaseUtilization = ecsWorld.entity("PhaseUtilization");
        long phaseLabourMarket = ecsWorld.entity("PhaseLabourMarket");
        long phasePromotion = ecsWorld.entity("PhasePromotion");
        long phaseCalendar = ecsWorld.entity("PhaseCalendar");

        this.initPipeline = ecsWorld.pipeline("InitEconomyPipeline")
            .with(Flecs.System)
            .with(phasePopInit).or()
            .with(phaseSpread).or()
            .with(phaseInit).or()
            .with(phaseEmployment).or()
            .with(phaseSync).or()
            .with(phaseNeedsCosts).or()
            .with(phaseInitTreasury).or()
            .with(phaseConsumption).or()
            .with(phaseProduction).or()
            .with(phaseToStock).or()
            .with(phaseInitAvailability).or()
            .with(phaseInitStock)
            .build();

        this.initBudgetPipeline = ecsWorld.pipeline("InitBudgetPipeline")
            .with(Flecs.System)
            .with(phaseDemographicsReset).or()
            .with(phaseSpread).or()
            .with(phaseInitBudget).or()
            .with(phaseState)
            .build();

        this.initSavingsPipeline = ecsWorld.pipeline("InitSavingsPipeline")
            .with(Flecs.System)
            .with(phaseDemographicsReset).or()
            .with(phaseSpread).or()
            .with(phaseReset).or()
            .with(phaseNeedsCosts).or()
            .with(phaseInitTreasury).or()
            .with(phaseConsumption).or()
            .with(phaseMarket).or()
            .with(phaseSettlement).or()
            .with(phaseToStock).or()
            .with(phaseInitStock).or()
            .with(phaseRevenue).or()
            .with(phaseIncome).or()
            .with(phaseInitSavings)
            .build();

        this.initTreasuryPipeline = ecsWorld.pipeline("InitTreasuryPipeline")
            .with(Flecs.System)
            .with(phaseInitTreasury)
            .build();

        this.mainPipeline = ecsWorld.pipeline("MainEconomyPipeline")
            .with(Flecs.System)
            .with(phaseDemographicsReset).or()
            .with(phaseSpread).or()
            .with(phaseRgoEmployment).or()
            .with(phaseEmployment).or()
            .with(phaseLabourMarket).or()
            .with(phaseSync).or()
            .with(phaseReset).or()
            .with(phaseNeedsCosts).or()
            .with(phaseConsumption).or()
            .with(phaseMarket).or()
            .with(phaseSettlement).or()
            .with(phaseProduction).or()
            .with(phaseToStock).or()
            .with(phasePrice).or()
            .with(phaseRevenue).or()
            .with(phaseUtilization).or()
            .with(phaseIncome).or()
            .with(phasePromotion).or()
            .with(phaseState).or()
            .with(phaseCalendar)
            .build();

        this.demographicsResetSystem = new DemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.countryDemographicsResetSystem = new CountryDemographicsResetSystem(ecsWorld, phaseDemographicsReset);
        this.regionIncomePreEmploymentResetSystem = new RegionIncomeResetSystem(ecsWorld, phaseDemographicsReset);
        this.populationInitializationSystem = new PopulationInitializationSystem(ecsWorld, phasePopInit);
        this.populationRegionInitializationSystem = new PopulationRegionInitializationSystem(ecsWorld, phasePopInit);
        this.demographicsPopulationSpreadSystem = new DemographicsPopulationSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsProvinceSpreadSystem = new DemographicsProvinceSpreadSystem(ecsWorld, phaseSpread);
        this.demographicsRegionInstanceSpreadSystem = new DemographicsRegionInstanceSpreadSystem(ecsWorld, phaseSpread);
        this.lawEffectInitializationSystem = new LawEffectInitializationSystem(ecsWorld, phaseInit);
        this.technologyEffectInitializationSystem = new TechnologyEffectInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.rgoMechanizationInitializationSystem = new RGOMechanizationInitializationSystem(ecsWorld, phaseInit);
        this.economyBuildingOwnerInitializationSystem = new EconomyBuildingOwnerInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.rgoSizeSystem = new RGOSizeSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.infrastructureInitializationSystem = new InfrastructureInitializationSystem(ecsWorld, phaseInit);
        this.regionProvinceCountSystem = new RegionProvinceCountSystem(ecsWorld, phaseInit);
        this.rgoEmploymentInitializationSystem = new RGOEmploymentInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.countryMarketInitializationSystem = new CountryMarketInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.globalMarketInitializationSystem = new GlobalMarketInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInit);
        this.rgoEmploymentSystem = new RGOEmploymentSystem(ecsWorld, simulationContext.getEcsConstants(), phaseRgoEmployment);
        this.economyBuildingWorkerDemandSystem = new EconomyBuildingWorkerDemandSystem(ecsWorld, phaseEmployment);
        this.economyBuildingEmploymentSystem = new EconomyBuildingEmploymentSystem(ecsWorld, simulationContext.getEcsConstants(), phaseEmployment);
        this.specialBuildingPostSystem = new SpecialBuildingPostSystem(ecsWorld, simulationContext.getEcsConstants(), phaseEmployment);
        this.statePostSystem = new StatePostSystem(ecsWorld, simulationContext.getEcsConstants(), phaseEmployment);
        this.militaryPostSystem = new MilitaryPostSystem(ecsWorld, simulationContext.getEcsConstants(), phaseEmployment);
        this.artisanEmploymentSystem = new ArtisanEmploymentSystem(ecsWorld, simulationContext.getEcsConstants(), phaseEmployment);
        this.labourMarketSystem = new LabourMarketSystem(ecsWorld, simulationContext.getEcsConstants(), phaseLabourMarket);
        this.populationEmploymentSynchronizationSystem = new PopulationEmploymentSynchronizationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSync);

        this.globalMarketResetSystem = new GlobalMarketResetSystem(ecsWorld, phaseReset);
        this.countryMarketResetSystem = new CountryMarketResetSystem(ecsWorld, phaseReset);
        this.needsCostsResetSystem = new NeedsCostsResetSystem(ecsWorld, phaseReset);
        this.countryInvestmentResetSystem = new CountryInvestmentResetSystem(ecsWorld, phaseReset);
        this.militarySupplyResetSystem = new MilitarySupplyResetSystem(ecsWorld, phaseReset);
        this.regionIncomeResetSystem = new RegionIncomeResetSystem(ecsWorld, phaseReset);
        this.countryBudgetPriceSystem = new CountryBudgetPriceSystem(ecsWorld, simulationContext.getEcsConstants(), phaseNeedsCosts);
        this.needsCostsCalculationSystem = new NeedsCostsCalculationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseNeedsCosts);
        this.militaryBaseCapacitySystem = new MilitaryBaseCapacitySystem(ecsWorld, simulationContext.getEcsConstants(), phaseNeedsCosts);
        this.militarySupplyNeedSystem = new MilitarySupplyNeedSystem(ecsWorld, simulationContext.getEcsConstants(), phaseNeedsCosts);
        this.maintenanceSystem = new MaintenanceSystem(ecsWorld, simulationContext.getEcsConstants(), phaseNeedsCosts);
        this.economyBuildingTreasuryInitializationSystem = new EconomyBuildingTreasuryInitializationSystem(ecsWorld, phaseInitTreasury);
        this.artisanTreasuryInitializationSystem = new ArtisanTreasuryInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInitTreasury);
        this.economyBuildingInputStockInitializationSystem = new EconomyBuildingInputStockInitializationSystem(ecsWorld, phaseInitTreasury);
        this.rgoTreasuryInitializationSystem = new RGOTreasuryInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInitTreasury);
        this.countryTreasuryInitializationSystem = new CountryTreasuryInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInitBudget);
        this.populationConsumptionSystem = new PopulationConsumptionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.countryPopulationDemandSystem = new CountryPopulationDemandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.economyBuildingConsumptionSystem = new EconomyBuildingConsumptionSystem(ecsWorld, phaseConsumption);
        this.artisanDemandSystem = new ArtisanDemandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.constructionDemandSystem = new ConstructionDemandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.rgoMechanizationDemandSystem = new RGOMechanizationDemandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.stateConsumptionDemandSystem = new StateConsumptionDemandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseConsumption);
        this.maintenanceDemandSystem = new MaintenanceDemandSystem(ecsWorld, phaseConsumption);
        this.strategicStockDemandSystem = new StrategicStockDemandSystem(ecsWorld, phaseConsumption);
        this.militarySupplyDemandSystem = new MilitarySupplyDemandSystem(ecsWorld, phaseConsumption);
        this.worldMarketTotalsSystem = new WorldMarketTotalsSystem(ecsWorld, simulationContext.getEcsConstants(), phaseMarket);
        this.globalTradeRatioSystem = new GlobalTradeRatioSystem(ecsWorld, phaseMarket);
        this.countryMarketResolveSystem = new CountryMarketResolveSystem(ecsWorld, simulationContext.getEcsConstants(), phaseMarket);
        this.countryMarketSpreadSystem = new CountryMarketSpreadSystem(ecsWorld, simulationContext.getEcsConstants(), phaseMarket);
        this.countryPriceSystem = new CountryPriceSystem(ecsWorld, simulationContext.getEcsConstants(), phaseMarket);
        this.countryPurchaseSystem = new CountryPurchaseSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.economyBuildingPurchaseSystem = new EconomyBuildingPurchaseSystem(ecsWorld, phaseSettlement);
        this.artisanPurchaseSystem = new ArtisanPurchaseSystem(ecsWorld, phaseSettlement);
        this.constructionPurchaseSystem = new ConstructionPurchaseSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.rgoMechanizationPurchaseSystem = new RGOMechanizationPurchaseSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.stateConsumptionPurchaseSystem = new StateConsumptionPurchaseSystem(ecsWorld, phaseSettlement);
        this.maintenancePurchaseSystem = new MaintenancePurchaseSystem(ecsWorld, phaseSettlement);
        this.strategicStockPurchaseSystem = new StrategicStockPurchaseSystem(ecsWorld, phaseSettlement);
        this.militarySupplyPurchaseSystem = new MilitarySupplyPurchaseSystem(ecsWorld, phaseSettlement);
        this.militaryOrganisationSystem = new MilitaryOrganisationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.countryImportPaymentSystem = new CountryImportPaymentSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.countryExportPayoutSystem = new CountryExportPayoutSystem(ecsWorld, simulationContext.getEcsConstants(), phaseSettlement);
        this.worldTradeMoneySettleSystem = new WorldTradeMoneySettleSystem(ecsWorld, phaseSettlement);
        this.rgoProduceSystem = new RGOProductionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseProduction);
        this.economyBuildingProductionSystem = new EconomyBuildingProductionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseProduction);
        this.artisanProductionSystem = new ArtisanProductionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseProduction);
        this.rgoSpreadProductionSystem = new RGOSpreadProductionSystem(ecsWorld, phaseToStock);
        this.economyBuildingSpreadProductionSystem = new EconomyBuildingSpreadProductionSystem(ecsWorld, phaseToStock);
        this.strategicStockReleaseSystem = new StrategicStockReleaseSystem(ecsWorld, phaseToStock);
        this.countryStockSystem = new CountryStockSystem(ecsWorld, simulationContext.getEcsConstants(), phaseToStock);
        this.countryProductionSpreadSystem = new CountryProductionSpreadSystem(ecsWorld, simulationContext.getEcsConstants(), phaseToStock);
        this.goodAvailabilityInitializationSystem = new GoodAvailabilityInitializationSystem(ecsWorld, phaseInitAvailability);
        this.countryStockInitializationSystem = new CountryStockInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInitStock);
        this.rgoMechanizationUpkeepInitializationSystem = new RGOMechanizationUpkeepInitializationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseInitStock);
        this.globalMarketResolveSystem = new GlobalMarketResolveSystem(ecsWorld, simulationContext.getEcsConstants(), phasePrice);
        this.countryProductionValueSystem = new CountryProductionValueSystem(ecsWorld, phaseRevenue);
        this.rgoRevenueSystem = new RGORevenueSystem(ecsWorld, simulationContext.getEcsConstants(), phaseRevenue);
        this.economyBuildingRevenueSystem = new EconomyBuildingRevenueSystem(ecsWorld, simulationContext.getEcsConstants(), phaseRevenue);
        this.artisanRevenueSystem = new ArtisanRevenueSystem(ecsWorld, simulationContext.getEcsConstants(), phaseRevenue);
        this.countryRevenueSettleSystem = new CountryRevenueSettleSystem(ecsWorld, phaseRevenue);
        this.economyBuildingUtilizationSystem = new EconomyBuildingUtilizationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseUtilization);
        this.economyBuildingWorkingCapitalSystem = new EconomyBuildingWorkingCapitalSystem(ecsWorld, simulationContext.getEcsConstants(), phaseUtilization);
        this.rgoProfitSharingSystem = new RGOProfitSharingSystem(ecsWorld, simulationContext.getEcsConstants(), phaseIncome);
        this.economyBuildingProfitSharingSystem = new EconomyBuildingProfitSharingSystem(ecsWorld, simulationContext.getEcsConstants(), phaseIncome);
        this.artisanIncomeSystem = new ArtisanIncomeSystem(ecsWorld, simulationContext.getEcsConstants(), phaseIncome);
        this.countryIncomeDistributionSystem = new CountryIncomeDistributionSystem(ecsWorld, phaseIncome);
        this.populationIncomeDistributionSystem = new PopulationIncomeDistributionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseIncome);
        this.religiousDonationSystem = new ReligiousDonationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseIncome);
        this.regionIncomeResidualSystem = new RegionIncomeResidualSystem(ecsWorld, phaseIncome);
        this.populationSavingsInitializationSystem = new PopulationSavingsInitializationSystem(ecsWorld, phaseInitSavings);
        this.provinceDemographySystem = new ProvinceDemographySystem(ecsWorld, simulationContext.getEcsConstants(), phasePromotion);
        this.populationGrowthSystem = new PopulationGrowthSystem(ecsWorld, simulationContext.getEcsConstants(), phasePromotion);
        this.populationApplicationSystem = new PopulationApplicationSystem(ecsWorld, simulationContext.getEcsConstants(), phasePromotion);
        this.populationPromotionSystem = new PopulationPromotionSystem(ecsWorld, simulationContext.getEcsConstants(), phasePromotion);
        this.populationArrivalSystem = new PopulationArrivalSystem(ecsWorld, simulationContext.getEcsConstants(), phasePromotion);
        this.countryCentralBankSystem = new CountryCentralBankSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.countryBudgetSystem = new CountryBudgetSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.armyDisbandSystem = new ArmyDisbandSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.artisanAllocationSystem = new ArtisanAllocationSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.bankSystem = new BankSystem(ecsWorld, phaseState);
        this.publicWorksSystem = new PublicWorksSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.strategicStockPlanSystem = new StrategicStockPlanSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.constructionCompletionSystem = new ConstructionCompletionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.constructionClosureSystem = new ConstructionClosureSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.economyBuildingClosureSystem = new EconomyBuildingClosureSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.investmentDecisionSystem = new InvestmentDecisionSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.developmentIndexSystem = new DevelopmentIndexSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.researchSystem = new ResearchSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.economyBuildingRecipeSystem = new EconomyBuildingRecipeSystem(ecsWorld, simulationContext.getEcsConstants(), phaseState);
        this.economyCalendarSystem = new EconomyCalendarSystem(ecsWorld, phaseCalendar);
    }

    public void initialize() {
        this.ecsConstants.resolveGlobals(this.ecsWorld);
        this.ecsWorld.runPipeline(this.initPipeline.id(), 1f);
        this.ecsWorld.runPipeline(this.initBudgetPipeline.id(), 1f);
        for (int pass = 0; pass < INITIAL_SAVINGS_PASSES; pass++) {
            this.ecsWorld.runPipeline(this.initSavingsPipeline.id(), 1f);
        }
        this.ecsWorld.runPipeline(this.initTreasuryPipeline.id(), 1f);
    }

    public void advanceDay() {
        this.ecsWorld.runPipeline(this.mainPipeline.id(), 1f);
        this.ecsWorld.progress(1f);
    }

    public Pipeline getMainPipeline() {
        return mainPipeline;
    }
}
