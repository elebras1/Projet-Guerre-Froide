package com.populaire.projetguerrefroide.service;

import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Pipeline;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.system.*;

import static com.populaire.projetguerrefroide.util.Constants.INITIAL_SAVINGS_PASSES;

public class EconomyService {
    private final DemographicsResetSystem demographicsResetSystem;
    private final CountryDemographicsResetSystem countryDemographicsResetSystem;
    private final PopulationInitializationSystem populationInitializationSystem;
    private final PopulationRegionInitializationSystem populationRegionInitializationSystem;
    private final DemographicsPopulationSpreadSystem demographicsPopulationSpreadSystem;
    private final DemographicsProvinceSpreadSystem demographicsProvinceSpreadSystem;
    private final DemographicsRegionInstanceSpreadSystem demographicsRegionInstanceSpreadSystem;
    private final RGOSizeSystem rgoSizeSystem;
    private final LawEffectInitializationSystem lawEffectInitializationSystem;
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
    private final LabourMarketSystem labourMarketSystem;
    private final PopulationApplicationSystem populationApplicationSystem;
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
    private final RegionIncomeResidualSystem regionIncomeResidualSystem;
    private final CountryBudgetSystem countryBudgetSystem;
    private final CountryBankSystem countryBankSystem;
    private final CountryInvestmentResetSystem countryInvestmentResetSystem;
    private final ConstructionDemandSystem constructionDemandSystem;
    private final ConstructionPurchaseSystem constructionPurchaseSystem;
    private final ConstructionCompletionSystem constructionCompletionSystem;
    private final ConstructionClosureSystem constructionClosureSystem;
    private final InvestmentDecisionSystem investmentDecisionSystem;

    private final World ecsWorld;
    private final Pipeline initPipeline;
    private final Pipeline initBudgetPipeline;
    private final Pipeline initSavingsPipeline;
    private final Pipeline initTreasuryPipeline;
    private final Pipeline mainPipeline;

    public EconomyService(GameContext gameContext) {
        World ecsWorld = gameContext.getEcsWorld();
        this.ecsWorld = ecsWorld;

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
        this.economyBuildingOwnerInitializationSystem = new EconomyBuildingOwnerInitializationSystem(ecsWorld, gameContext.getEcsConstants(), phaseInit);
        this.rgoSizeSystem = new RGOSizeSystem(ecsWorld, gameContext.getEcsConstants(), phaseInit);
        this.rgoEmploymentInitializationSystem = new RGOEmploymentInitializationSystem(ecsWorld, gameContext.getEcsConstants(), phaseInit);
        this.countryMarketInitializationSystem = new CountryMarketInitializationSystem(ecsWorld, phaseInit);
        this.globalMarketInitializationSystem = new GlobalMarketInitializationSystem(ecsWorld, phaseInit);
        this.rgoEmploymentSystem = new RGOEmploymentSystem(ecsWorld, gameContext.getEcsConstants(), phaseRgoEmployment);
        this.economyBuildingWorkerDemandSystem = new EconomyBuildingWorkerDemandSystem(ecsWorld, phaseEmployment);
        this.economyBuildingEmploymentSystem = new EconomyBuildingEmploymentSystem(ecsWorld, phaseEmployment);
        this.labourMarketSystem = new LabourMarketSystem(ecsWorld, phaseLabourMarket);
        this.populationEmploymentSynchronizationSystem = new PopulationEmploymentSynchronizationSystem(ecsWorld, gameContext.getEcsConstants(), phaseSync);

        this.globalMarketResetSystem = new GlobalMarketResetSystem(ecsWorld, phaseReset);
        this.countryMarketResetSystem = new CountryMarketResetSystem(ecsWorld, phaseReset);
        this.needsCostsResetSystem = new NeedsCostsResetSystem(ecsWorld, phaseReset);
        this.countryInvestmentResetSystem = new CountryInvestmentResetSystem(ecsWorld, phaseReset);
        this.regionIncomeResetSystem = new RegionIncomeResetSystem(ecsWorld, phaseReset);
        this.countryBudgetPriceSystem = new CountryBudgetPriceSystem(ecsWorld, phaseNeedsCosts);
        this.needsCostsCalculationSystem = new NeedsCostsCalculationSystem(ecsWorld, phaseNeedsCosts);
        this.economyBuildingTreasuryInitializationSystem = new EconomyBuildingTreasuryInitializationSystem(ecsWorld, phaseInitTreasury);
        this.economyBuildingInputStockInitializationSystem = new EconomyBuildingInputStockInitializationSystem(ecsWorld, phaseInitTreasury);
        this.rgoTreasuryInitializationSystem = new RGOTreasuryInitializationSystem(ecsWorld, gameContext.getEcsConstants(), phaseInitTreasury);
        this.countryTreasuryInitializationSystem = new CountryTreasuryInitializationSystem(ecsWorld, phaseInitBudget);
        this.populationConsumptionSystem = new PopulationConsumptionSystem(ecsWorld, gameContext.getEcsConstants(), phaseConsumption);
        this.countryPopulationDemandSystem = new CountryPopulationDemandSystem(ecsWorld, phaseConsumption);
        this.economyBuildingConsumptionSystem = new EconomyBuildingConsumptionSystem(ecsWorld, phaseConsumption);
        this.constructionDemandSystem = new ConstructionDemandSystem(ecsWorld, gameContext.getEcsConstants(), phaseConsumption);
        this.worldMarketTotalsSystem = new WorldMarketTotalsSystem(ecsWorld, phaseMarket);
        this.globalTradeRatioSystem = new GlobalTradeRatioSystem(ecsWorld, phaseMarket);
        this.countryMarketResolveSystem = new CountryMarketResolveSystem(ecsWorld, phaseMarket);
        this.countryMarketSpreadSystem = new CountryMarketSpreadSystem(ecsWorld, phaseMarket);
        this.countryPriceSystem = new CountryPriceSystem(ecsWorld, phaseMarket);
        this.countryPurchaseSystem = new CountryPurchaseSystem(ecsWorld, phaseSettlement);
        this.economyBuildingPurchaseSystem = new EconomyBuildingPurchaseSystem(ecsWorld, phaseSettlement);
        this.constructionPurchaseSystem = new ConstructionPurchaseSystem(ecsWorld, gameContext.getEcsConstants(), phaseSettlement);
        this.countryImportPaymentSystem = new CountryImportPaymentSystem(ecsWorld, phaseSettlement);
        this.countryExportPayoutSystem = new CountryExportPayoutSystem(ecsWorld, phaseSettlement);
        this.worldTradeMoneySettleSystem = new WorldTradeMoneySettleSystem(ecsWorld, phaseSettlement);
        this.rgoProduceSystem = new RGOProductionSystem(ecsWorld, gameContext.getEcsConstants(), phaseProduction);
        this.economyBuildingProductionSystem = new EconomyBuildingProductionSystem(ecsWorld, phaseProduction);
        this.rgoSpreadProductionSystem = new RGOSpreadProductionSystem(ecsWorld, phaseToStock);
        this.economyBuildingSpreadProductionSystem = new EconomyBuildingSpreadProductionSystem(ecsWorld, phaseToStock);
        this.countryStockSystem = new CountryStockSystem(ecsWorld, phaseToStock);
        this.countryProductionSpreadSystem = new CountryProductionSpreadSystem(ecsWorld, phaseToStock);
        this.goodAvailabilityInitializationSystem = new GoodAvailabilityInitializationSystem(ecsWorld, phaseInitAvailability);
        this.countryStockInitializationSystem = new CountryStockInitializationSystem(ecsWorld, phaseInitStock);
        this.globalMarketResolveSystem = new GlobalMarketResolveSystem(ecsWorld, phasePrice);
        this.countryProductionValueSystem = new CountryProductionValueSystem(ecsWorld, phaseRevenue);
        this.rgoRevenueSystem = new RGORevenueSystem(ecsWorld, gameContext.getEcsConstants(), phaseRevenue);
        this.economyBuildingRevenueSystem = new EconomyBuildingRevenueSystem(ecsWorld, phaseRevenue);
        this.countryRevenueSettleSystem = new CountryRevenueSettleSystem(ecsWorld, phaseRevenue);
        this.economyBuildingUtilizationSystem = new EconomyBuildingUtilizationSystem(ecsWorld, phaseUtilization);
        this.economyBuildingWorkingCapitalSystem = new EconomyBuildingWorkingCapitalSystem(ecsWorld, gameContext.getEcsConstants(), phaseUtilization);
        this.rgoProfitSharingSystem = new RGOProfitSharingSystem(ecsWorld, gameContext.getEcsConstants(), phaseIncome);
        this.economyBuildingProfitSharingSystem = new EconomyBuildingProfitSharingSystem(ecsWorld, gameContext.getEcsConstants(), phaseIncome);
        this.countryIncomeDistributionSystem = new CountryIncomeDistributionSystem(ecsWorld, phaseIncome);
        this.populationIncomeDistributionSystem = new PopulationIncomeDistributionSystem(ecsWorld, gameContext.getEcsConstants(), phaseIncome);
        this.regionIncomeResidualSystem = new RegionIncomeResidualSystem(ecsWorld, phaseIncome);
        this.populationSavingsInitializationSystem = new PopulationSavingsInitializationSystem(ecsWorld, phaseInitSavings);
        this.populationApplicationSystem = new PopulationApplicationSystem(ecsWorld, gameContext.getEcsConstants(), phasePromotion);
        this.populationPromotionSystem = new PopulationPromotionSystem(ecsWorld, gameContext.getEcsConstants(), phasePromotion);
        this.populationArrivalSystem = new PopulationArrivalSystem(ecsWorld, phasePromotion);
        this.countryBudgetSystem = new CountryBudgetSystem(ecsWorld, phaseState);
        this.countryBankSystem = new CountryBankSystem(ecsWorld, phaseState);
        this.constructionCompletionSystem = new ConstructionCompletionSystem(ecsWorld, gameContext.getEcsConstants(), phaseState);
        this.constructionClosureSystem = new ConstructionClosureSystem(ecsWorld, gameContext.getEcsConstants(), phaseState);
        this.investmentDecisionSystem = new InvestmentDecisionSystem(ecsWorld, gameContext.getEcsConstants(), phaseState);
        this.economyCalendarSystem = new EconomyCalendarSystem(ecsWorld, phaseCalendar);
    }

    public void initialize() {
        this.ecsWorld.runPipeline(this.initPipeline.id(), 1f);
        this.ecsWorld.runPipeline(this.initBudgetPipeline.id(), 1f);
        for (int pass = 0; pass < INITIAL_SAVINGS_PASSES; pass++) {
            this.ecsWorld.runPipeline(this.initSavingsPipeline.id(), 1f);
        }
        this.ecsWorld.runPipeline(this.initTreasuryPipeline.id(), 1f);
    }

    public Pipeline getMainPipeline() {
        return mainPipeline;
    }
}
