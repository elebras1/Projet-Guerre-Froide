package com.populaire.projetguerrefroide.simulation;

import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.BuildingView;
import com.populaire.projetguerrefroide.component.DevelopmentBuilding;
import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryBank;
import com.populaire.projetguerrefroide.component.CountryCentralBank;
import com.populaire.projetguerrefroide.component.CountryCentralBankView;
import com.populaire.projetguerrefroide.component.CountryBankView;
import com.populaire.projetguerrefroide.component.CountryBudget;
import com.populaire.projetguerrefroide.component.CountryInvestment;
import com.populaire.projetguerrefroide.component.CountryInvestmentView;
import com.populaire.projetguerrefroide.component.CountryBudgetPolicy;
import com.populaire.projetguerrefroide.component.CountryBudgetPolicyView;
import com.populaire.projetguerrefroide.component.CountryBudgetView;
import com.populaire.projetguerrefroide.component.CountryCulturePolicy;
import com.populaire.projetguerrefroide.component.CountryCulturePolicyView;
import com.populaire.projetguerrefroide.component.CountryDemographics;
import com.populaire.projetguerrefroide.component.CountryDemographicsView;
import com.populaire.projetguerrefroide.component.CountryEducationPolicy;
import com.populaire.projetguerrefroide.component.CountryEducationPolicyView;
import com.populaire.projetguerrefroide.component.CountryLaborPolicy;
import com.populaire.projetguerrefroide.component.CountryLaborPolicyView;
import com.populaire.projetguerrefroide.component.CountryMarket;
import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.CountryMaintenance;
import com.populaire.projetguerrefroide.component.CountryMaintenanceView;
import com.populaire.projetguerrefroide.component.CountryMilitary;
import com.populaire.projetguerrefroide.component.CountryMilitaryView;
import com.populaire.projetguerrefroide.component.CountryPublicWorks;
import com.populaire.projetguerrefroide.component.CountryPublicWorksView;
import com.populaire.projetguerrefroide.component.CountryStateConsumption;
import com.populaire.projetguerrefroide.component.CountryStateConsumptionView;
import com.populaire.projetguerrefroide.component.CountryPoliticalPolicy;
import com.populaire.projetguerrefroide.component.CountryPoliticalPolicyView;
import com.populaire.projetguerrefroide.component.CountryPopulationPolicy;
import com.populaire.projetguerrefroide.component.CountryPopulationPolicyView;
import com.populaire.projetguerrefroide.component.CountryProductionPolicy;
import com.populaire.projetguerrefroide.component.CountryProductionPolicyView;
import com.populaire.projetguerrefroide.component.CountryProfitDistributionPolicy;
import com.populaire.projetguerrefroide.component.CountryProfitDistributionPolicyView;
import com.populaire.projetguerrefroide.component.CountryTaxPolicy;
import com.populaire.projetguerrefroide.component.CountryTaxPolicyView;
import com.populaire.projetguerrefroide.component.CountryTradePolicy;
import com.populaire.projetguerrefroide.component.CountryTradePolicyView;
import com.populaire.projetguerrefroide.component.Demographics;
import com.populaire.projetguerrefroide.component.DemographicsView;
import com.populaire.projetguerrefroide.component.EconomyBuilding;
import com.populaire.projetguerrefroide.component.EconomyBuildingType;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;
import com.populaire.projetguerrefroide.component.EconomyBuildingView;
import com.populaire.projetguerrefroide.component.GlobalGood;
import com.populaire.projetguerrefroide.component.GlobalGoodView;
import com.populaire.projetguerrefroide.component.GlobalMarket;
import com.populaire.projetguerrefroide.component.GlobalMarketView;
import com.populaire.projetguerrefroide.component.GlobalPopulationType;
import com.populaire.projetguerrefroide.component.GlobalPopulationTypeView;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.GoodView;
import com.populaire.projetguerrefroide.component.Population;
import com.populaire.projetguerrefroide.component.PopulationType;
import com.populaire.projetguerrefroide.component.PopulationTypeView;
import com.populaire.projetguerrefroide.component.PopulationView;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.component.RegionDemographics;
import com.populaire.projetguerrefroide.component.RegionDemographicsView;
import com.populaire.projetguerrefroide.component.GlobalArtisanCraftsView;
import com.populaire.projetguerrefroide.component.RegionArtisans;
import com.populaire.projetguerrefroide.component.RegionArtisansView;
import com.populaire.projetguerrefroide.component.RegionInstance;
import com.populaire.projetguerrefroide.component.RegionInstanceIncome;
import com.populaire.projetguerrefroide.component.RegionInstanceIncomeView;
import com.populaire.projetguerrefroide.component.RegionInstanceView;
import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringType;
import com.populaire.projetguerrefroide.component.ResourceGatheringTypeView;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.populaire.projetguerrefroide.util.StateBudgetUtils;
import com.populaire.projetguerrefroide.util.EmploymentUtils;
import com.populaire.projetguerrefroide.util.ArtisanUtils;
import static com.populaire.projetguerrefroide.util.Constants.CAPITALIST_OWNER;
import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.SPENDING_RATES_BY_STRATA;
import static com.populaire.projetguerrefroide.util.Constants.STATE_OWNER;
import static com.populaire.projetguerrefroide.util.Constants.WORKERS_OWNER;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.ADMINISTRATION_INCOME_TYPE;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.EDUCATION_INCOME_TYPE;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.MILITARY_INCOME_TYPE;
import static com.populaire.projetguerrefroide.util.IncomeTypeUtils.RELIGIOUS_INCOME_TYPE;
import static com.populaire.projetguerrefroide.util.StrataUtils.MIDDLE_STRATA;
import static com.populaire.projetguerrefroide.util.StrataUtils.POOR_STRATA;
import static com.populaire.projetguerrefroide.util.StrataUtils.RICH_STRATA;

public class SimulationRecorder implements AutoCloseable {

    private static final int DAYS_PER_YEAR = 365;
    private static final int STRATA_COUNT = 3;
    private static final float CPI_EVERYDAY_WEIGHT = 0.3f;
    private static final float PRICE_BAND_LOW = 0.25f;
    private static final float PRICE_BAND_HIGH = 3.0f;
    private static final float LARGE_COUNTRY_ADULTS = 1_000_000f;
    private static final float STOPPED_UTILIZATION = 0.01f;
    private static final int POP_INCOME = 0;
    private static final int POP_SAVINGS = 1;
    private static final int POP_LIFE = 2;
    private static final int POP_EVERYDAY = 3;
    private static final int POP_LUXURY = 4;
    private static final int POP_UNSPENT = 5;
    private static final int POP_DEPOSIT = 6;
    private static final int POP_ACCOUNT_COUNT = 7;
    private static final int ACCOUNT_POPULATION = 0;
    private static final int ACCOUNT_EMPLOYED = 1;
    private static final int ACCOUNT_INCOME = 2;
    private static final int ACCOUNT_WAGES = 3;
    private static final int ACCOUNT_PROFIT_SHARES = 4;
    private static final int ACCOUNT_TAXES = 5;
    private static final int ACCOUNT_STATE = 6;
    private static final int ACCOUNT_LIFE = 7;
    private static final int ACCOUNT_EVERYDAY = 8;
    private static final int ACCOUNT_LUXURY = 9;
    private static final int ACCOUNT_SAVINGS = 10;
    private static final int ACCOUNT_DEPOSITS = 11;
    private static final int ACCOUNT_WAGE_PREMIUM = 12;
    private static final int ACCOUNT_VACANCIES = 13;
    private static final int ACCOUNT_UNCOVERED_VACANCIES = 14;
    private static final int ACCOUNT_STATE_POSTS = 15;
    private static final int ACCOUNT_DONATIONS = 16;
    private static final int ACCOUNT_COUNT = 17;

    private final World ecsWorld;
    private final LocalDate startDate;
    private final int detailEvery;
    private boolean detail;

    private final float[] baseCosts = new float[GOOD_COUNT];
    private final boolean[] militaryGoods = new boolean[GOOD_COUNT];
    private final float[] cpiWeights = new float[GOOD_COUNT];
    private final int[] popTypeStrata = new int[POP_TYPE_COUNT];

    private final double[] strataAmount = new double[STRATA_COUNT];
    private final double[] strataLife = new double[STRATA_COUNT];
    private final double[] strataEveryday = new double[STRATA_COUNT];
    private final double[] strataLuxury = new double[STRATA_COUNT];
    private double tickMoney;
    private double tickRealOutput;
    private double tickCpi;

    private double firstRealOutput = Double.NaN;
    private double firstRealOutputPerAdult = Double.NaN;
    private double lastRealOutputPerAdult;
    private double minRealOutputRatio = Double.MAX_VALUE;
    private double startMinRatio = Double.MAX_VALUE;
    private double startMaxRatio = 0.0;
    private double firstMoney = Double.NaN;
    private double maxMoneyDrift = 0.0;
    private double minCpi = Double.MAX_VALUE;
    private double maxCpi = 0.0;
    private float minBalance = Float.MAX_VALUE;
    private String minBalanceSource = "";
    private final int[] priceOutOfBandStreak = new int[GOOD_COUNT];
    private final int[] priceOutOfBandMaxStreak = new int[GOOD_COUNT];

    private final double[] countryAdults;
    private final double[] countryPoorAmount;
    private final double[] countryPoorLife;
    private final double[] yearCountryPoorLife;
    private final double[] yearCountryAdults;
    private final double[] countryPopSpending;
    private final double[] yearCountryStrataSavings;
    private final double[] yearCountryStrataIncome;
    private final double[] yearCountryStrataLifeCost;
    private double lastYearCashHoldings;
    private String lastYearCashHoldingsSource = "";
    private int yearTicks;
    private double yearRealOutput;
    private double yearCpi;
    private final double[] yearStrataLife = new double[STRATA_COUNT];
    private final double[] yearStrataEveryday = new double[STRATA_COUNT];
    private final double[] yearStrataLuxury = new double[STRATA_COUNT];
    private double minYearlyPoorLifeWorld = Double.MAX_VALUE;
    private double yearMilitarySupply;
    private double yearMilitaryNeeded;
    private double minYearlyArmySupply = Double.MAX_VALUE;
    private double minYearlyPoorLifeCountry = Double.MAX_VALUE;
    private String worstPoorLifeCountry = "";

    private double tickNanosSum;
    private long tickNanosMax;
    private int ticksTimed;
    private double yearTickNanosSum;
    private long yearTickNanosMax;

    private final String[] goodNames = new String[GOOD_COUNT];
    private final List<String> countryNames = new ArrayList<>();
    private final Map<Long, Integer> countryIndexByEntity = new HashMap<>();

    private final String[] popTypeNames = new String[POP_TYPE_COUNT];

    private final List<String> buildingTypeNames = new ArrayList<>();
    private final Map<Long, Integer> buildingTypeIndexByEntity = new HashMap<>();
    private final Map<Long, Integer> buildingTypeOutputGoodIndex = new HashMap<>();
    private final Map<Long, int[]> buildingTypeWorkerPopTypeIndexes = new HashMap<>();

    private final Map<String, int[]> rgoWorkerPopTypeIndexes = new HashMap<>();

    private final Query countryQuery;
    private final Query rgoQuery;
    private final Query buildingQuery;
    private final Query developmentQuery;
    private final Query artisanQuery;
    private final Query regionIncomeQuery;
    private final Query populationQuery;

    private final float[] worldProductionByGood = new float[GOOD_COUNT];
    private final float[] worldDemandByGood = new float[GOOD_COUNT];
    private final float[] countryProductionByGood = new float[GOOD_COUNT];
    private final float[] countryDemandByGood = new float[GOOD_COUNT];
    private final double[] worldStockByGood = new double[GOOD_COUNT];
    private final double[] previousWorldStockByGood = new double[GOOD_COUNT];
    private final double[] worldStockFlowByGood = new double[GOOD_COUNT];
    private final double[] worldStockInflowByGood = new double[GOOD_COUNT];
    private final double[] worldImportedByGood = new double[GOOD_COUNT];
    private final double[] worldExportedByGood = new double[GOOD_COUNT];
    private double maxGoodsResidual = 0.0;
    private String maxGoodsResidualSource = "";

    private final float[] buildingTypeProduction;
    private final float[] buildingTypePrimaryWorkers;
    private final float[] buildingTypeSecondaryWorkers;
    private final float[] buildingTypeProfit;
    private final float[] buildingTypeScale;
    private final float[] buildingTypeMargin;
    private final int[] buildingTypeStoppedProfitable;
    private final float[] buildingTypeStateProduction;
    private final long stateOwnerTag;
    private final int[] buildingTypeCount;

    private final float[] countryPrimaryWorkers;
    private final float[] countrySecondaryWorkers;
    private final float[] countryPopEmployment;
    private final float[] countryWorkers;
    private final int[] countryPopEntities;
    private final int[] countryPopTypeTotal;
    private final int[] countryPopTypeEmployment;
    private final double[] countryPopTypeAccounts;
    private final double[] tickAccounts = new double[POP_TYPE_COUNT * ACCOUNT_COUNT];
    private final double[] yearAccounts = new double[POP_TYPE_COUNT * ACCOUNT_COUNT];
    private final double[] tickPremiumWorkers = new double[POP_TYPE_COUNT];
    private final double[] tickWorkersByPopType = new double[POP_TYPE_COUNT];
    private final double[] tickVacancies = new double[POP_TYPE_COUNT];
    private final double[] tickUncoveredVacancies = new double[POP_TYPE_COUNT];
    private final double[] tickStatePosts = new double[POP_TYPE_COUNT];
    private final boolean[] ruralPopTypes = new boolean[POP_TYPE_COUNT];
    private int capitalistPopTypeIndex = -1;
    private int aristocratPopTypeIndex = -1;
    private int religiousPopTypeIndex = -1;

    private final Map<Long, float[]> countryRgoByGood = new HashMap<>();
    private final Map<Long, float[]> countryBuildingByGood = new HashMap<>();
    private final Map<Long, float[]> countryArtisanByGood = new HashMap<>();
    private final Map<Long, Float> buildingTypeInputDemand = new TreeMap<>();
    private final Map<Long, float[]> countryRegionIncome = new HashMap<>();

    private double worldProductionTotal = 0.0;
    private double buildingProductionTotal = 0.0;
    private long mismatchCount = 0;

    private float tickRevenueTotal;
    private float tickWagesPaidTotal;
    private float tickRgoWagesTotal;
    private float tickBuildingWagesTotal;
    private float tickRgoProfitTotal;
    private float tickBuildingProfitTotal;
    private float tickWorkerProfitShareTotal;
    private float tickCapitalistShareTotal;
    private float tickAristocratShareTotal;
    private float tickCountryShareTotal;
    private double tickTreasuryTotal;
    private double tickTaxTotal;
    private double tickBankReservesTotal;
    private double tickStateLoansTotal;
    private double tickPrivateLoansTotal;
    private double tickPopDepositsTotal;
    private double tickCreatedMoneyTotal;
    private final double[] tickLoanFlows = new double[8];
    private final double[] tickFundsTotal = new double[3];
    private double tickConstructionSpendingTotal;
    private double tickRgoTreasuryTotal;
    private double tickArtisanTreasuryTotal;
    private double tickArtisanRevenueTotal;
    private double tickArtisanInputTotal;
    private double tickArtisanWagesTotal;
    private int tickSitesInProgressTotal;
    private int tickSitesStartedTotal;
    private int tickSitesCompletedTotal;
    private double tickStateWagesTotal;
    private double tickStateSocialTotal;
    private double tickEnterpriseSupportTotal;
    private double tickStateConsumptionTotal;
    private double tickPublicWorksSpendingTotal;
    private double tickMaintenanceSpendingTotal;
    private double tickMaintenanceNeededTotal;
    private double tickMilitarySupplyTotal;
    private double tickMilitaryNeededTotal;
    private double tickTargetTreasuryTotal;
    private float tickSalesRevenueTotal;
    private double tickBuildingTreasuryTotal;
    private float tickTradeMoneyTotal;
    private float tickTariffTotal;
    private float tickSavingsTotal;
    private double tickPopulationTotal;
    private float tickEmploymentTotal;
    private float tickWorkersBookedTotal;
    private float minSurplusRatio = Float.MAX_VALUE;
    private float minInputsCost = Float.MAX_VALUE;

    private double firstTickWorldPopulation = -1.0;
    private double lastTickWorldPopulation;
    private float firstTickWorldTreasury = Float.NaN;
    private float lastTickWorldTreasury;
    private final float[] lastPrices = new float[GOOD_COUNT];
    private final float[] finalTotalByPopType = new float[POP_TYPE_COUNT];
    private final float[] finalEmploymentByPopType = new float[POP_TYPE_COUNT];
    private long employmentExceedsPopulationRows = 0;

    private final Map<String, CsvWriter> writers = new HashMap<>();
    private final File outputDir;

    public SimulationRecorder(World ecsWorld, File outputDir, LocalDate startDate, int detailEvery) {
        this.ecsWorld = ecsWorld;
        this.outputDir = outputDir;
        this.startDate = startDate;
        this.detailEvery = Math.max(1, detailEvery);

        this.clearOutputDirectory(outputDir);

        this.countryQuery = ecsWorld.query().with(Country.class).build();
        this.rgoQuery = ecsWorld.query()
            .with(Province.class)
            .with(ResourceGathering.class)
            .build();
        this.buildingQuery = ecsWorld.query()
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .build();
        this.artisanQuery = ecsWorld.query().with(RegionArtisans.class).with(RegionInstance.class).build();
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuilding.class)
            .build();
        this.regionIncomeQuery = ecsWorld.query()
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .build();
        this.populationQuery = ecsWorld.query()
            .with(Population.class)
            .with(PopulationType.class)
            .build();

        this.readGoodNames();
        this.readPopTypeNames();
        this.readCountries();
        this.readBuildingTypes();
        this.readRgoTypes();

        this.buildingTypeProduction = new float[this.buildingTypeNames.size()];
        this.buildingTypePrimaryWorkers = new float[this.buildingTypeNames.size()];
        this.buildingTypeSecondaryWorkers = new float[this.buildingTypeNames.size()];
        this.buildingTypeProfit = new float[this.buildingTypeNames.size()];
        this.buildingTypeScale = new float[this.buildingTypeNames.size()];
        this.buildingTypeCount = new int[this.buildingTypeNames.size()];
        this.buildingTypeMargin = new float[this.buildingTypeNames.size()];
        this.buildingTypeStoppedProfitable = new int[this.buildingTypeNames.size()];
        this.buildingTypeStateProduction = new float[this.buildingTypeNames.size()];
        this.stateOwnerTag = ecsWorld.lookup(EcsConstants.COUNTRY_TAG);

        this.countryPrimaryWorkers = new float[this.countryNames.size()];
        this.countrySecondaryWorkers = new float[this.countryNames.size()];
        this.countryPopEmployment = new float[this.countryNames.size()];
        this.countryWorkers = new float[this.countryNames.size()];
        this.countryPopEntities = new int[this.countryNames.size()];
        this.countryPopTypeTotal = new int[this.countryNames.size() * POP_TYPE_COUNT];
        this.countryPopTypeEmployment = new int[this.countryNames.size() * POP_TYPE_COUNT];
        this.countryPopTypeAccounts = new double[this.countryNames.size() * POP_TYPE_COUNT * POP_ACCOUNT_COUNT];

        this.countryAdults = new double[this.countryNames.size()];
        this.countryPoorAmount = new double[this.countryNames.size()];
        this.countryPoorLife = new double[this.countryNames.size()];
        this.yearCountryPoorLife = new double[this.countryNames.size()];
        this.yearCountryAdults = new double[this.countryNames.size()];
        this.countryPopSpending = new double[this.countryNames.size()];
        this.yearCountryStrataSavings = new double[this.countryNames.size() * STRATA_COUNT];
        this.yearCountryStrataIncome = new double[this.countryNames.size() * STRATA_COUNT];
        this.yearCountryStrataLifeCost = new double[this.countryNames.size() * STRATA_COUNT];

        this.readBaseCosts();
        this.readCpiWeights();
        this.readInitialStocks();

        this.openWriters();
    }

    private void readInitialStocks() {
        this.countryQuery.each((long countryId) -> {
            CountryMarketView cm = this.ecsWorld.obtainEntityView(countryId).getMutView(CountryMarket.class);
            for (int g = 0; g < GOOD_COUNT; g++) {
                this.previousWorldStockByGood[g] += cm.goodStocks(g);
            }
        });
    }

    private void readBaseCosts() {
        EntityView globalGood = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_good"));
        GlobalGoodView data = globalGood.getMutView(GlobalGood.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            GoodView good = this.ecsWorld.obtainEntityView(data.goodIds(g)).getMutView(Good.class);
            this.baseCosts[g] = good.cost();
            this.militaryGoods[g] = this.ecsWorld.obtainEntityView(data.goodIds(g)).has(this.ecsWorld.lookup(EcsConstants.MILITARY_GOOD_TAG));
        }
    }

    private void readCpiWeights() {
        EntityView globalPopType = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_population_type"));
        GlobalPopulationTypeView data = globalPopType.getMutView(GlobalPopulationType.class);
        float[][] baskets = new float[POP_TYPE_COUNT][GOOD_COUNT];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            PopulationTypeView popType = this.ecsWorld.obtainEntityView(data.popTypeIds(p)).getMutView(PopulationType.class);
            this.popTypeStrata[p] = popType.strata();
            for (int j = 0; j < popType.lifeNeedsGoodIndexesLength(); j++) {
                int goodIndex = popType.lifeNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                baskets[p][goodIndex] += popType.lifeNeedsGoodAmounts(j);
            }
            for (int j = 0; j < popType.everydayNeedsGoodIndexesLength(); j++) {
                int goodIndex = popType.everydayNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                baskets[p][goodIndex] += CPI_EVERYDAY_WEIGHT * popType.everydayNeedsGoodAmounts(j);
            }
        }

        double[] popTypeAmounts = new double[POP_TYPE_COUNT];
        this.populationQuery.iter(iter -> {
            int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                popTypeAmounts[popTypeIndex] += population.amount();
            }
        });

        double total = 0.0;
        double[] weights = new double[GOOD_COUNT];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            for (int g = 0; g < GOOD_COUNT; g++) {
                weights[g] += popTypeAmounts[p] / NEEDS_SCALING_FACTOR * baskets[p][g] * this.baseCosts[g];
            }
        }
        for (double weight : weights) {
            total += weight;
        }
        for (int g = 0; g < GOOD_COUNT; g++) {
            this.cpiWeights[g] = total > 0.0 ? (float) (weights[g] / total) : 0f;
        }
    }

    private void readGoodNames() {
        EntityView globalGood = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_good"));
        GlobalGoodView data = globalGood.getMutView(GlobalGood.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            this.goodNames[g] = this.ecsWorld.obtainEntity(data.goodIds(g)).name();
        }
    }

    private void readPopTypeNames() {
        EntityView globalPopType = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_population_type"));
        GlobalPopulationTypeView data = globalPopType.getMutView(GlobalPopulationType.class);
        long capitalistTag = this.ecsWorld.lookup(EcsConstants.CAPITALIST_TAG);
        long aristocratTag = this.ecsWorld.lookup(EcsConstants.ARISTOCRAT_TAG);
        long ruralTag = this.ecsWorld.lookup(EcsConstants.RURAL_TAG);
        long religiousTag = this.ecsWorld.lookup(EcsConstants.RELIGIOUS_TAG);
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            Entity popType = this.ecsWorld.obtainEntity(data.popTypeIds(p));
            this.popTypeNames[p] = popType.name();
            this.ruralPopTypes[p] = popType.has(ruralTag);
            if (popType.has(religiousTag)) {
                this.religiousPopTypeIndex = p;
            }
            if (popType.has(capitalistTag)) {
                this.capitalistPopTypeIndex = p;
            }
            if (popType.has(aristocratTag)) {
                this.aristocratPopTypeIndex = p;
            }
        }
    }

    private long developmentLevels() {
        long[] levels = new long[1];
        this.developmentQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                levels[0] += building.size();
            }
        });
        return levels[0];
    }

    private void readCountries() {
        List<Long> ids = new ArrayList<>();
        this.countryQuery.each((long id) -> ids.add(id));
        ids.sort(Long::compareTo);
        for (long id : ids) {
            String name = this.ecsWorld.obtainEntity(id).name();
            this.countryIndexByEntity.put(id, this.countryNames.size());
            this.countryNames.add(name);
        }
    }

    private void readBuildingTypes() {
        Query query = this.ecsWorld.query().with(EconomyBuildingType.class).self().with(Flecs.Prefab).build();
        List<Long> ids = new ArrayList<>();
        query.each((long id) -> ids.add(id));
        ids.sort(Long::compareTo);
        for (long id : ids) {
            EconomyBuildingTypeView data = this.ecsWorld.obtainEntityView(id).getMutView(EconomyBuildingType.class);
            this.buildingTypeIndexByEntity.put(id, this.buildingTypeNames.size());
            this.buildingTypeNames.add(this.ecsWorld.obtainEntity(id).name());
            this.buildingTypeOutputGoodIndex.put(id, data.goodOutputIndex());

            int[] workerIndexes = new int[2];
            workerIndexes[0] = data.primaryWorkerPopTypeIndex();
            workerIndexes[1] = data.secondaryWorkerPopTypeIndex();
            this.buildingTypeWorkerPopTypeIndexes.put(id, workerIndexes);
        }
    }

    private void readRgoTypes() {
        Query query = this.ecsWorld.query().with(ResourceGatheringType.class).build();
        List<Long> ids = new ArrayList<>();
        query.each((long id) -> ids.add(id));
        for (long id : ids) {
            ResourceGatheringTypeView data = this.ecsWorld.obtainEntityView(id).getMutView(ResourceGatheringType.class);
            String name = this.ecsWorld.obtainEntity(id).name();
            String good = name.startsWith("rgo_") ? name.substring(4) : name;
            this.rgoWorkerPopTypeIndexes.put(good, new int[] { data.workerPopTypeIndex(), data.slavePopTypeIndex() });
        }
    }

    private void clearOutputDirectory(File dir) {
        if (!dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    this.clearOutputDirectory(child);
                }
                child.delete();
            }
        }
    }

    private void openWriters() {
        this.writer("meta/goods.csv").header("good");
        this.writer("meta/pop_types.csv").header("pop_type");
        this.writer("meta/building_types.csv").header("building_type", "output_good", "primary_worker_pop_type", "secondary_worker_pop_type");
        this.writer("meta/rgo_types.csv").header("good", "worker_pop_type", "slave_pop_type");

        this.writer("world/market.csv").header("tick", "date", "good", "production", "demand", "price", "stock", "offer_total", "need_total", "trade_ratio");

        this.writer("country/market.csv").header("tick", "date", "country", "good", "price", "demand", "satisfaction", "stock", "produced", "consumed", "import_need", "imported", "exported", "lost");
        this.writer("country/poor_life.csv").header("year", "date", "country", "adults", "poor_life");
        this.writer("country/cash_holdings.csv").header("year", "date", "country", "strata", "savings", "income", "days", "limit");
        this.writer("country/treasury.csv").header("tick", "date", "country", "treasury", "private_investment", "sales_revenue", "production_value", "tariff_revenue",
            "tax_revenue", "target_treasury", "spending_ratio", "bank_reserves", "state_loans", "capitalist_loans", "workers_loans",
            "capitalist_fund", "state_fund", "workers_fund", "construction_spending",
            "capitalist_committed", "state_committed", "workers_committed", "military_supply", "military_needed", "military_supply_ratio", "credit_limit", "revenue_trend",
            "lending_rate", "bankruptcies", "public_works_loan", "public_works_return", "price_index", "created_money",
            "public_works_sites", "public_works_daily", "maintenance_spending", "maintenance_satisfaction");
        this.writer("country/budget.csv").header("tick", "date", "country", "needs", "military_pay", "administration_pay", "education_pay", "clergy_pay", "social",
            "state_consumption", "military_supply", "maintenance", "subsidies", "tax", "tariff", "profit", "spending_ratio", "state_loans", "credit_limit", "military_pops",
            "military_posts", "military_filled", "units");
        this.writer("country/employment_ratio.csv").header("tick", "date", "country", "employment", "workers", "ratio");
        this.writer("country/deposits.csv").header("tick", "date", "country", "pop_type", "total", "deposits", "savings", "unspent", "income");
        this.writer("country/demographics.csv").header("tick", "date", "country", "pop_type", "total", "employment", "consciousness", "militancy", "literacy", "savings", "life_satisfaction", "everyday_satisfaction", "luxury_satisfaction");
        this.writer("country/demographics_totals.csv").header("tick", "date", "country", "total_population", "total_employment", "consciousness", "militancy", "literacy", "savings", "life_satisfaction", "everyday_satisfaction", "luxury_satisfaction", "children", "adults", "seniors", "pop_entities", "life_satisfaction_avg", "total_population_scratch");
        this.writer("country/needs_costs.csv").header("tick", "date", "country", "pop_type", "life_cost", "everyday_cost", "luxury_cost");
        this.writer("country/effect_policy.csv").header("tick", "date", "country",
            "poor_tax_rate", "middle_tax_rate", "rich_tax_rate", "social_spending_rate", "military_spending_rate",
            "education_spending_rate", "administration_spending_rate", "tariff_rate", "capitalist_profit_share_rate",
            "worker_profit_share_rate", "aristocrat_profit_share_rate", "state_profit_share_rate", "min_wage_factor",
            "education_efficiency", "factory_output_modifier", "factory_input_modifier", "rgo_output_modifier",
            "construction_speed", "pop_growth_factor", "migration_pull", "pop_spending", "slavery_allowed",
            "political_consciousness", "political_radicalism", "suppression", "social_mobility", "class_rigidity",
            "administrative_efficiency", "religious_conversion_speed", "secularism", "assimilation_rate",
            "migration_push", "maximum_economy_scale_factor");
        this.writer("country/production.csv").header("tick", "date", "country", "good", "rgo_production", "building_production", "artisan_production", "total");
        this.writer("country/building_employment.csv").header("tick", "date", "country", "primary_workers", "secondary_workers");

        this.writer("building_types/production.csv").header("tick", "date", "building_type", "production", "primary_workers", "secondary_workers", "profit", "utilization", "count", "unit_margin", "stopped_profitable", "state_production");
        this.writer("building_types/input_demand.csv").header("tick", "date", "building_type", "good", "demand");

        this.writer("rgo/employment.csv").header("tick", "date", "country", "good", "production", "workers", "slaves", "profit", "wage");

        this.writer("region_income/income.csv").header("tick", "date", "country", "pop_type", "min_wage", "workers", "profit_share");
        this.writer("region_income/shares.csv").header("tick", "date", "country", "capitalist_share", "aristocrat_share", "country_share");

        this.writer("world/accounts.csv").header("year", "date", "pop_type", "strata", "population", "employed", "income", "wages", "profit_shares", "taxes",
            "state", "life_spending", "everyday_spending", "luxury_spending", "savings", "deposits", "wage_premium", "vacancies", "uncovered_vacancies", "state_posts", "donations");
        this.writer("world/yearly.csv").header("year", "date",
            "real_output", "real_output_ratio", "real_output_per_100k_adults", "adults", "cpi",
            "life_poor", "life_middle", "life_rich", "everyday_poor", "everyday_middle", "everyday_rich",
            "luxury_poor", "luxury_middle", "luxury_rich", "worst_country_life_poor", "worst_country",
            "money", "money_drift", "min_balance", "min_balance_source", "goods_out_of_band",
            "avg_tick_ms", "max_tick_ms", "army_supply", "development_levels");

        this.writer("money.csv").header("tick", "date",
            "wages_paid", "wages_buildings", "wages_rgo",
            "profit_rgo", "profit_buildings", "profit_workers", "profit_capitalists", "profit_aristocrats", "profit_state",
            "revenue", "treasury_total", "sales_revenue_total", "building_treasury_total", "trade_money_total", "tariff_total", "savings_total",
            "employment_total", "workers_booked", "ratio_emp_workers",
            "tax_total", "state_wages_total", "state_social_total", "enterprise_support_total", "state_consumption_total", "public_works_spending_total", "maintenance_spending_total", "maintenance_needed_total", "military_supply_total", "military_needed_total", "target_treasury_total",
            "bank_reserves_total", "state_loans_total", "private_loans_total", "pop_deposits_total",
            "created_money_total",
            "state_borrowed", "state_repaid", "state_interest_paid", "state_written_off",
            "private_borrowed", "private_repaid", "private_interest_paid", "private_written_off",
            "capitalist_fund_total", "state_fund_total", "workers_fund_total", "construction_spending_total", "rgo_treasury_total",
            "artisan_treasury_total", "artisan_revenue_total", "artisan_input_total", "artisan_wages_total",
            "sites_in_progress_total", "sites_started_total", "sites_completed_total");
    }

    private CsvWriter writer(String relativePath) {
        return this.writers.computeIfAbsent(relativePath, path -> {
            File file = new File(this.outputDir, path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            return new CsvWriter(file);
        });
    }

    private CsvWriter detailWriter(String relativePath) {
        return this.detail ? this.writer(relativePath) : CsvWriter.DISCARD;
    }

    private void writeMetaRows() {
        CsvWriter goods = this.writer("meta/goods.csv");
        for (String good : this.goodNames) {
            goods.row(good);
        }
        CsvWriter popTypes = this.writer("meta/pop_types.csv");
        for (String popType : this.popTypeNames) {
            popTypes.row(popType);
        }
        CsvWriter buildingTypes = this.writer("meta/building_types.csv");
        for (String buildingType : this.buildingTypeNames) {
            long id = this.ecsWorld.lookup(buildingType);
            int outputGood = this.buildingTypeOutputGoodIndex.getOrDefault(id, -1);
            int[] workers = this.buildingTypeWorkerPopTypeIndexes.getOrDefault(id, new int[] { -1, -1 });
            buildingTypes.row(buildingType,
                outputGood >= 0 ? this.goodNames[outputGood] : "",
                workers[0] >= 0 ? this.popTypeNames[workers[0]] : "",
                workers[1] >= 0 ? this.popTypeNames[workers[1]] : "");
        }
        CsvWriter rgoTypes = this.writer("meta/rgo_types.csv");
        for (Map.Entry<String, int[]> entry : this.rgoWorkerPopTypeIndexes.entrySet()) {
            int[] workerIndexes = entry.getValue();
            rgoTypes.row(entry.getKey(),
                workerIndexes[0] >= 0 ? this.popTypeNames[workerIndexes[0]] : "",
                workerIndexes[1] >= 0 ? this.popTypeNames[workerIndexes[1]] : "");
        }
    }

    public void recordTick(int tick, long tickNanos) {
        String date = this.startDate.plusDays(tick).toString();
        this.detail = tick % this.detailEvery == 0;

        this.resetScratch();
        this.sumCountryEmployment();
        this.recordWorldMarket(tick, date);
        this.recordCountryMarkets(tick, date);
        this.recordRgo(tick, date);
        this.recordBuildings(tick, date);
        this.recordArtisans();
        this.recordRegionIncome(tick, date);
        this.recordAccounts(tick, date);
        this.recordEmploymentRatio(tick, date);
        this.commitCountryProduction(tick, date);
        this.commitBuildingTypes(tick, date);
        this.recordMoney(tick, date);

        this.lastTickWorldPopulation = this.tickPopulationTotal;
        if (this.firstTickWorldPopulation < 0.0) {
            this.firstTickWorldPopulation = this.tickPopulationTotal;
        }
        if (!Float.isNaN(this.lastTickWorldTreasury)) {
        }
        this.lastTickWorldTreasury = (float) this.tickTreasuryTotal;
        if (Float.isNaN(this.firstTickWorldTreasury)) {
            this.firstTickWorldTreasury = (float) this.tickTreasuryTotal;
        }

        this.checkConsistency();
        this.trackGoodsConservation();
        this.trackInvariants(tick, tickNanos);
        if ((tick + 1) % DAYS_PER_YEAR == 0) {
            this.recordYear(tick, date);
        }
    }

    private void trackGoodsConservation() {
        for (int g = 0; g < GOOD_COUNT; g++) {
            double scale = this.previousWorldStockByGood[g] + this.worldStockInflowByGood[g] + this.worldImportedByGood[g] + 1.0;
            double stockResidual = Math.abs(this.worldStockByGood[g] - this.previousWorldStockByGood[g] - this.worldStockFlowByGood[g]) / scale;
            double tradeResidual = Math.abs(this.worldImportedByGood[g] - this.worldExportedByGood[g]) / scale;
            if (stockResidual > this.maxGoodsResidual) {
                this.maxGoodsResidual = stockResidual;
                this.maxGoodsResidualSource = "stock " + this.goodNames[g];
            }
            if (tradeResidual > this.maxGoodsResidual) {
                this.maxGoodsResidual = tradeResidual;
                this.maxGoodsResidualSource = "commerce " + this.goodNames[g];
            }
            this.previousWorldStockByGood[g] = this.worldStockByGood[g];
        }
    }

    private void trackInvariants(int tick, long tickNanos) {
        this.tickRealOutput = 0.0;
        this.tickCpi = 0.0;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.baseCosts[g] <= 0f) {
                continue;
            }
            if (!this.militaryGoods[g]) {
                this.tickRealOutput += (double) this.worldProductionByGood[g] * this.baseCosts[g];
            }
            float relativePrice = this.lastPrices[g] / this.baseCosts[g];
            this.tickCpi += this.cpiWeights[g] * relativePrice;
            boolean outOfBand = this.worldProductionByGood[g] + this.worldDemandByGood[g] > 0f
                && (relativePrice < PRICE_BAND_LOW || relativePrice > PRICE_BAND_HIGH);
            this.priceOutOfBandStreak[g] = outOfBand ? this.priceOutOfBandStreak[g] + 1 : 0;
            this.priceOutOfBandMaxStreak[g] = Math.max(this.priceOutOfBandMaxStreak[g], this.priceOutOfBandStreak[g]);
        }
        this.tickMoney += (double) this.tickTreasuryTotal + this.tickSalesRevenueTotal + this.tickBuildingTreasuryTotal + this.tickTradeMoneyTotal + this.tickBankReservesTotal
            + this.tickFundsTotal[0] + this.tickFundsTotal[1] + this.tickFundsTotal[2] + this.tickRgoTreasuryTotal + this.tickArtisanTreasuryTotal;

        double adults = 0.0;
        for (double countryAdult : this.countryAdults) {
            adults += countryAdult;
        }
        double realOutputPerAdult = adults > 0.0 ? this.tickRealOutput / adults : 0.0;

        if (Double.isNaN(this.firstRealOutput)) {
            this.firstRealOutput = this.tickRealOutput;
            this.firstRealOutputPerAdult = realOutputPerAdult;
            this.firstMoney = this.tickMoney - this.tickCreatedMoneyTotal;
        }
        this.lastRealOutputPerAdult = realOutputPerAdult;

        double ratio = this.firstRealOutput > 0.0 ? this.tickRealOutput / this.firstRealOutput : 0.0;
        this.minRealOutputRatio = Math.min(this.minRealOutputRatio, ratio);
        if (tick < DAYS_PER_YEAR) {
            this.startMinRatio = Math.min(this.startMinRatio, ratio);
            this.startMaxRatio = Math.max(this.startMaxRatio, ratio);
        }
        if (this.firstMoney != 0.0) {
            this.maxMoneyDrift = Math.max(this.maxMoneyDrift, Math.abs(this.tickMoney - this.tickCreatedMoneyTotal - this.firstMoney) / Math.abs(this.firstMoney));
        }
        this.minCpi = Math.min(this.minCpi, this.tickCpi);
        this.maxCpi = Math.max(this.maxCpi, this.tickCpi);

        this.tickNanosSum += tickNanos;
        this.tickNanosMax = Math.max(this.tickNanosMax, tickNanos);
        this.ticksTimed++;
        this.yearTickNanosSum += tickNanos;
        this.yearTickNanosMax = Math.max(this.yearTickNanosMax, tickNanos);

        this.yearTicks++;
        this.yearRealOutput += this.tickRealOutput;
        this.yearCpi += this.tickCpi;
        this.yearMilitarySupply += this.tickMilitarySupplyTotal;
        this.yearMilitaryNeeded += this.tickMilitaryNeededTotal;
        for (int s = 0; s < STRATA_COUNT; s++) {
            if (this.strataAmount[s] > 0.0) {
                this.yearStrataLife[s] += this.strataLife[s] / this.strataAmount[s];
                this.yearStrataEveryday[s] += this.strataEveryday[s] / this.strataAmount[s];
                this.yearStrataLuxury[s] += this.strataLuxury[s] / this.strataAmount[s];
            }
        }
        for (int c = 0; c < this.countryNames.size(); c++) {
            this.yearCountryAdults[c] += this.countryAdults[c];
            if (this.countryPoorAmount[c] > 0.0) {
                this.yearCountryPoorLife[c] += this.countryPoorLife[c] / this.countryPoorAmount[c];
            } else {
                this.yearCountryPoorLife[c] += 1.0;
            }
        }
    }

    private void recordYear(int tick, String date) {
        int year = (tick + 1) / DAYS_PER_YEAR;
        double ticks = Math.max(1, this.yearTicks);

        double worstCountryLife = Double.MAX_VALUE;
        String worstCountry = "";
        double adults = 0.0;
        for (int c = 0; c < this.countryNames.size(); c++) {
            double countryAdult = this.yearCountryAdults[c] / ticks;
            adults += countryAdult;
            double poorLife = this.yearCountryPoorLife[c] / ticks;
            this.writer("country/poor_life.csv").row(year, date, this.countryNames.get(c), countryAdult, poorLife);
            if (countryAdult >= LARGE_COUNTRY_ADULTS && poorLife < worstCountryLife) {
                worstCountryLife = poorLife;
                worstCountry = this.countryNames.get(c);
            }
        }
        double poorLifeWorld = this.yearStrataLife[POOR_STRATA] / ticks;
        double armySupply = this.yearMilitaryNeeded > 0.0 ? this.yearMilitarySupply / this.yearMilitaryNeeded : 1.0;
        this.minYearlyArmySupply = Math.min(this.minYearlyArmySupply, armySupply);
        if (poorLifeWorld < this.minYearlyPoorLifeWorld) {
            this.minYearlyPoorLifeWorld = poorLifeWorld;
        }
        if (worstCountryLife < this.minYearlyPoorLifeCountry) {
            this.minYearlyPoorLifeCountry = worstCountryLife;
            this.worstPoorLifeCountry = worstCountry + " (" + year + ")";
        }

        this.lastYearCashHoldings = 0.0;
        this.lastYearCashHoldingsSource = "";
        for (int c = 0; c < this.countryNames.size(); c++) {
            if (this.yearCountryAdults[c] / ticks < LARGE_COUNTRY_ADULTS) {
                continue;
            }
            for (int s = 0; s < STRATA_COUNT; s++) {
                double income = this.yearCountryStrataIncome[c * STRATA_COUNT + s] / ticks;
                if (income <= 0.0) {
                    continue;
                }
                double savings = this.yearCountryStrataSavings[c * STRATA_COUNT + s] / ticks;
                double lifeCost = this.yearCountryStrataLifeCost[c * STRATA_COUNT + s] / ticks;
                if (savings < lifeCost || income < lifeCost) {
                    continue;
                }
                double days = savings / income;
                double limit = 2.0 / (SPENDING_RATES_BY_STRATA[s] * (1.0 + this.countryPopSpending[c]));
                this.writer("country/cash_holdings.csv").row(year, date, this.countryNames.get(c), s, this.yearCountryStrataSavings[c * STRATA_COUNT + s] / ticks, income, days, limit);
                if (days / limit > this.lastYearCashHoldings) {
                    this.lastYearCashHoldings = days / limit;
                    this.lastYearCashHoldingsSource = String.format("%s strate %d : %.0f jours de revenu, limite %.0f", this.countryNames.get(c), s, days, limit);
                }
            }
        }
        Arrays.fill(this.yearCountryStrataSavings, 0.0);
        Arrays.fill(this.yearCountryStrataIncome, 0.0);
        Arrays.fill(this.yearCountryStrataLifeCost, 0.0);

        int goodsOutOfBand = 0;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.priceOutOfBandStreak[g] > 0) {
                goodsOutOfBand++;
            }
        }

        double realOutput = this.yearRealOutput / ticks;
        this.writer("world/yearly.csv").row(year, date,
            realOutput,
            this.firstRealOutput > 0.0 ? realOutput / this.firstRealOutput : 0.0,
            adults > 0.0 ? realOutput / adults * NEEDS_SCALING_FACTOR : 0.0,
            adults,
            this.yearCpi / ticks,
            this.yearStrataLife[POOR_STRATA] / ticks, this.yearStrataLife[MIDDLE_STRATA] / ticks, this.yearStrataLife[RICH_STRATA] / ticks,
            this.yearStrataEveryday[POOR_STRATA] / ticks, this.yearStrataEveryday[MIDDLE_STRATA] / ticks, this.yearStrataEveryday[RICH_STRATA] / ticks,
            this.yearStrataLuxury[POOR_STRATA] / ticks, this.yearStrataLuxury[MIDDLE_STRATA] / ticks, this.yearStrataLuxury[RICH_STRATA] / ticks,
            worstCountryLife, worstCountry,
            this.tickMoney,
            this.firstMoney != 0.0 ? (this.tickMoney - this.tickCreatedMoneyTotal - this.firstMoney) / this.firstMoney : 0.0,
            this.minBalance, this.minBalanceSource, goodsOutOfBand,
            this.yearTickNanosSum / ticks / 1_000_000.0, this.yearTickNanosMax / 1_000_000.0, armySupply, this.developmentLevels());

        this.writeAccounts(year, date, this.yearAccounts, ticks);
        Arrays.fill(this.yearAccounts, 0.0);

        this.yearTicks = 0;
        this.yearRealOutput = 0.0;
        this.yearCpi = 0.0;
        this.yearMilitarySupply = 0.0;
        this.yearMilitaryNeeded = 0.0;
        Arrays.fill(this.yearStrataLife, 0.0);
        Arrays.fill(this.yearStrataEveryday, 0.0);
        Arrays.fill(this.yearStrataLuxury, 0.0);
        Arrays.fill(this.yearCountryPoorLife, 0.0);
        Arrays.fill(this.yearCountryAdults, 0.0);
        this.yearTickNanosSum = 0.0;
        this.yearTickNanosMax = 0L;
    }

    private void trackBalance(float balance, String kind, String owner) {
        if (balance < this.minBalance) {
            this.minBalance = balance;
            this.minBalanceSource = kind + " " + owner;
        }
    }

    private void resetScratch() {
        Arrays.fill(this.worldProductionByGood, 0f);
        Arrays.fill(this.worldDemandByGood, 0f);
        Arrays.fill(this.countryProductionByGood, 0f);
        Arrays.fill(this.countryDemandByGood, 0f);
        Arrays.fill(this.worldStockByGood, 0.0);
        Arrays.fill(this.worldStockFlowByGood, 0.0);
        Arrays.fill(this.worldStockInflowByGood, 0.0);
        Arrays.fill(this.worldImportedByGood, 0.0);
        Arrays.fill(this.worldExportedByGood, 0.0);
        Arrays.fill(this.buildingTypeProduction, 0f);
        Arrays.fill(this.buildingTypePrimaryWorkers, 0f);
        Arrays.fill(this.buildingTypeSecondaryWorkers, 0f);
        Arrays.fill(this.buildingTypeProfit, 0f);
        Arrays.fill(this.buildingTypeScale, 0f);
        Arrays.fill(this.buildingTypeCount, 0);
        Arrays.fill(this.buildingTypeMargin, 0f);
        Arrays.fill(this.buildingTypeStoppedProfitable, 0);
        Arrays.fill(this.buildingTypeStateProduction, 0f);
        Arrays.fill(this.countryPrimaryWorkers, 0f);
        Arrays.fill(this.countrySecondaryWorkers, 0f);
        Arrays.fill(this.countryPopEmployment, 0f);
        Arrays.fill(this.countryWorkers, 0f);
        Arrays.fill(this.countryPopEntities, 0);
        Arrays.fill(this.countryPopTypeTotal, 0);
        Arrays.fill(this.countryPopTypeEmployment, 0);
        Arrays.fill(this.countryPopTypeAccounts, 0.0);
        Arrays.fill(this.tickPremiumWorkers, 0.0);
        Arrays.fill(this.tickWorkersByPopType, 0.0);
        Arrays.fill(this.tickVacancies, 0.0);
        Arrays.fill(this.tickUncoveredVacancies, 0.0);
        Arrays.fill(this.tickStatePosts, 0.0);
        this.countryRgoByGood.clear();
        this.countryBuildingByGood.clear();
        this.countryArtisanByGood.clear();
        this.buildingTypeInputDemand.clear();
        this.countryRegionIncome.clear();

        this.tickRevenueTotal = 0f;
        this.tickWagesPaidTotal = 0f;
        this.tickRgoWagesTotal = 0f;
        this.tickBuildingWagesTotal = 0f;
        this.tickRgoProfitTotal = 0f;
        this.tickBuildingProfitTotal = 0f;
        this.tickWorkerProfitShareTotal = 0f;
        this.tickCapitalistShareTotal = 0f;
        this.tickAristocratShareTotal = 0f;
        this.tickCountryShareTotal = 0f;
        this.tickTreasuryTotal = 0.0;
        this.tickTaxTotal = 0.0;
        this.tickBankReservesTotal = 0.0;
        this.tickStateLoansTotal = 0.0;
        this.tickPrivateLoansTotal = 0.0;
        this.tickPopDepositsTotal = 0.0;
        this.tickCreatedMoneyTotal = 0.0;
        Arrays.fill(this.tickLoanFlows, 0.0);
        Arrays.fill(this.tickFundsTotal, 0.0);
        this.tickConstructionSpendingTotal = 0.0;
        this.tickRgoTreasuryTotal = 0.0;
        this.tickArtisanTreasuryTotal = 0.0;
        this.tickArtisanRevenueTotal = 0.0;
        this.tickArtisanInputTotal = 0.0;
        this.tickArtisanWagesTotal = 0.0;
        this.tickSitesInProgressTotal = 0;
        this.tickSitesStartedTotal = 0;
        this.tickSitesCompletedTotal = 0;
        this.tickStateWagesTotal = 0.0;
        this.tickStateSocialTotal = 0.0;
        this.tickEnterpriseSupportTotal = 0.0;
        this.tickStateConsumptionTotal = 0.0;
        this.tickPublicWorksSpendingTotal = 0.0;
        this.tickMaintenanceSpendingTotal = 0.0;
        this.tickMaintenanceNeededTotal = 0.0;
        this.tickMilitarySupplyTotal = 0.0;
        this.tickMilitaryNeededTotal = 0.0;
        this.tickTargetTreasuryTotal = 0.0;
        this.tickSalesRevenueTotal = 0f;
        this.tickBuildingTreasuryTotal = 0.0;
        this.tickTradeMoneyTotal = 0f;
        this.tickTariffTotal = 0f;
        this.tickSavingsTotal = 0f;
        this.tickPopulationTotal = 0.0;
        this.tickEmploymentTotal = 0f;
        this.tickWorkersBookedTotal = 0f;
        Arrays.fill(this.finalTotalByPopType, 0f);
        Arrays.fill(this.finalEmploymentByPopType, 0f);

        Arrays.fill(this.strataAmount, 0.0);
        Arrays.fill(this.strataLife, 0.0);
        Arrays.fill(this.strataEveryday, 0.0);
        Arrays.fill(this.strataLuxury, 0.0);
        Arrays.fill(this.countryAdults, 0.0);
        Arrays.fill(this.countryPoorAmount, 0.0);
        Arrays.fill(this.countryPoorLife, 0.0);
        this.tickMoney = 0.0;
    }

    private void recordWorldMarket(int tick, String date) {
        EntityView globalMarket = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_market"));
        GlobalMarketView data = globalMarket.getMutView(GlobalMarket.class);

        CsvWriter writer = this.detailWriter("world/market.csv");
        for (int g = 0; g < GOOD_COUNT; g++) {
            float production = data.goodProductionAmounts(g);
            float demand = data.goodDemandAmounts(g);
            float price = data.goodPrices(g);
            this.worldProductionByGood[g] = production;
            this.worldDemandByGood[g] = demand;
            this.worldProductionTotal += production;
            this.tickRevenueTotal += production * price;
            this.lastPrices[g] = price;
            writer.row(tick, date, this.goodNames[g], production, demand, price, data.goodStockAmounts(g),
                data.goodOfferTotals(g), data.goodNeedTotals(g), data.goodTradeRatios(g));
            this.tickTradeMoneyTotal += data.goodTradeMoney(g);
        }
    }

    private void recordCountryMarkets(int tick, String date) {
        CsvWriter market = this.detailWriter("country/market.csv");
        CsvWriter treasury = this.detailWriter("country/treasury.csv");
        CsvWriter demographics = this.detailWriter("country/demographics.csv");
        CsvWriter demographicsTotals = this.detailWriter("country/demographics_totals.csv");
        CsvWriter needsCosts = this.detailWriter("country/needs_costs.csv");
        CsvWriter effectPolicy = this.detailWriter("country/effect_policy.csv");
        CsvWriter budgetWriter = this.detailWriter("country/budget.csv");
        int[] incomeTypes = new int[POP_TYPE_COUNT];
        boolean[] assistedTypes = new boolean[POP_TYPE_COUNT];
        StateBudgetUtils.readPopulationTypes(this.ecsWorld, incomeTypes, assistedTypes);
        int statePostIndex = StateBudgetUtils.statePostIndex(this.ecsWorld);
        float[] baseWorkforceShares = EmploymentUtils.baseWorkforceShares(this.ecsWorld);

        this.countryQuery.each((long countryId) -> {
            EntityView country = this.ecsWorld.obtainEntityView(countryId);
            CountryMarketView cm = country.getMutView(CountryMarket.class);
            CountryDemographicsView cd = country.getMutView(CountryDemographics.class);
            CountryBudgetView budget = country.getMutView(CountryBudget.class);
            CountryBankView bank = country.getMutView(CountryBank.class);
            CountryInvestmentView investment = country.getMutView(CountryInvestment.class);
            CountryTaxPolicyView taxPolicy = country.getMutView(CountryTaxPolicy.class);
            CountryBudgetPolicyView budgetPolicy = country.getMutView(CountryBudgetPolicy.class);
            CountryEducationPolicyView educationPolicy = country.getMutView(CountryEducationPolicy.class);
            CountryTradePolicyView tradePolicy = country.getMutView(CountryTradePolicy.class);
            CountryProductionPolicyView productionPolicy = country.getMutView(CountryProductionPolicy.class);
            CountryLaborPolicyView laborPolicy = country.getMutView(CountryLaborPolicy.class);
            CountryPopulationPolicyView populationPolicy = country.getMutView(CountryPopulationPolicy.class);
            CountryPoliticalPolicyView politicalPolicy = country.getMutView(CountryPoliticalPolicy.class);
            CountryCulturePolicyView culturePolicy = country.getMutView(CountryCulturePolicy.class);
            CountryProfitDistributionPolicyView profitDistributionPolicy = country.getMutView(CountryProfitDistributionPolicy.class);

            String name = country.name();

            Integer countryIndex = this.countryIndexByEntity.get(countryId);

            this.tickPopulationTotal += cd.totalPopulation();
            this.tickTreasuryTotal += cm.treasury();
            double goodRevenues = 0.0;
            double minGoodRevenue = 0.0;
            for (int g = 0; g < GOOD_COUNT; g++) {
                goodRevenues += cm.goodRevenues(g);
                minGoodRevenue = Math.min(minGoodRevenue, cm.goodRevenues(g));
            }
            this.tickSalesRevenueTotal += goodRevenues;
            this.tickTariffTotal += cm.tariffRevenue();
            this.tickTaxTotal += cm.taxRevenue();
            this.tickStateWagesTotal += budget.wagesBudget();
            this.tickStateSocialTotal += budget.socialBudget();
            this.tickEnterpriseSupportTotal += cm.enterpriseSupport();
            CountryStateConsumptionView stateConsumption = country.getView(CountryStateConsumption.class);
            this.tickStateConsumptionTotal += stateConsumption.spentToday();
            CountryPublicWorksView publicWorks = country.getView(CountryPublicWorks.class);
            this.tickPublicWorksSpendingTotal += publicWorks.dailyCommitment();
            CountryMaintenanceView maintenance = country.getView(CountryMaintenance.class);
            this.tickMaintenanceSpendingTotal += maintenance.spentToday();
            for (int g = 0; g < GOOD_COUNT; g++) {
                this.tickMaintenanceNeededTotal += (double) maintenance.needs(g) * cm.goodPrices(g);
            }
            CountryMilitaryView military = country.getView(CountryMilitary.class);
            double militaryNeeded = 0.0;
            for (int g = 0; g < GOOD_COUNT; g++) {
                militaryNeeded += (double) military.supplyNeeds(g) * cm.goodPrices(g);
            }
            this.tickMilitarySupplyTotal += military.spentToday();
            this.tickMilitaryNeededTotal += militaryNeeded;
            this.tickTargetTreasuryTotal += budget.targetTreasury();
            this.tickBankReservesTotal += bank.reserves() + bank.interestPool();
            CountryCentralBankView centralBank = country.getView(CountryCentralBank.class);
            this.tickCreatedMoneyTotal += centralBank.createdMoney();
            this.tickStateLoansTotal += bank.loans(STATE_OWNER);
            this.tickPrivateLoansTotal += bank.loans(CAPITALIST_OWNER) + bank.loans(WORKERS_OWNER);
            this.tickLoanFlows[0] += bank.borrowed(STATE_OWNER);
            this.tickLoanFlows[1] += bank.repaid(STATE_OWNER);
            this.tickLoanFlows[2] += bank.interestPaid(STATE_OWNER);
            this.tickLoanFlows[3] += bank.writtenOff(STATE_OWNER);
            this.tickLoanFlows[4] += bank.borrowed(CAPITALIST_OWNER) + bank.borrowed(WORKERS_OWNER);
            this.tickLoanFlows[5] += bank.repaid(CAPITALIST_OWNER) + bank.repaid(WORKERS_OWNER);
            this.tickLoanFlows[6] += bank.interestPaid(CAPITALIST_OWNER) + bank.interestPaid(WORKERS_OWNER);
            this.tickLoanFlows[7] += bank.writtenOff(CAPITALIST_OWNER) + bank.writtenOff(WORKERS_OWNER);
            this.trackBalance((float) bank.reserves(), "bank_reserves", name);
            double constructionSpending = 0.0;
            for (int o = 0; o < this.tickFundsTotal.length; o++) {
                this.tickFundsTotal[o] += investment.funds(o);
                constructionSpending += investment.spentToday(o);
                this.trackBalance((float) investment.funds(o), "investment_fund", name);
            }
            this.tickConstructionSpendingTotal += constructionSpending;
            this.tickSitesInProgressTotal += investment.sitesInProgress();
            this.tickSitesStartedTotal += investment.sitesStarted();
            this.tickSitesCompletedTotal += investment.sitesCompleted();
            if (countryIndex != null) {
                this.countryPopSpending[countryIndex] = budgetPolicy.popSpending();
            }
            this.trackBalance((float) cm.treasury(), "treasury", name);
            this.trackBalance((float) minGoodRevenue, "good_revenue", name);
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeTotal = countryIndex != null ? this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + p] : cd.totalByPopType(p);
                if (countryIndex != null) {
                    this.yearCountryStrataLifeCost[countryIndex * STRATA_COUNT + this.popTypeStrata[p]] += popTypeTotal / NEEDS_SCALING_FACTOR * cm.lifeCostsByPopType(p);
                }
                float popTypeEmployment = countryIndex != null ? this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + p] : cd.employmentByPopType(p);
                if (popTypeEmployment > popTypeTotal) {
                    this.employmentExceedsPopulationRows++;
                }
                this.finalTotalByPopType[p] += popTypeTotal;
                this.finalEmploymentByPopType[p] += popTypeEmployment;
            }

            for (int g = 0; g < GOOD_COUNT; g++) {
                float demand = cm.goodDemandAmounts(g);
                this.countryDemandByGood[g] += demand;
                this.worldStockByGood[g] += cm.goodStocks(g);
                this.worldStockInflowByGood[g] += cm.goodProducedAmounts(g);
                this.worldStockFlowByGood[g] += (double) cm.goodProducedAmounts(g) - (cm.goodConsumedAmounts(g) - cm.goodImportedAmounts(g))
                    - cm.goodExportedAmounts(g) - cm.goodLostAmounts(g);
                this.worldImportedByGood[g] += cm.goodImportedAmounts(g);
                this.worldExportedByGood[g] += cm.goodExportedAmounts(g);
                market.row(tick, date, name, this.goodNames[g],
                    cm.goodPrices(g), demand, cm.goodDemandSatisfactionRatios(g),
                    cm.goodStocks(g), cm.goodProducedAmounts(g), cm.goodConsumedAmounts(g), cm.goodImportNeeds(g),
                    cm.goodImportedAmounts(g), cm.goodExportedAmounts(g), cm.goodLostAmounts(g));
            }

            treasury.row(tick, date, name, cm.treasury(), cm.privateInvestmentAmount(), goodRevenues, cm.productionValue(), cm.tariffRevenue(),
                cm.taxRevenue(), budget.targetTreasury(), budget.spendingRatio(), bank.reserves(), bank.loans(STATE_OWNER), bank.loans(CAPITALIST_OWNER), bank.loans(WORKERS_OWNER),
                investment.funds(0), investment.funds(1), investment.funds(2), constructionSpending,
                investment.committed(0), investment.committed(1), investment.committed(2), military.spentToday(), militaryNeeded, budget.militarySupplyRatio(), bank.creditLimit(), budget.revenueTrend(),
                bank.lendingRate(), bank.bankruptcies(), budget.publicWorksLoan(), publicWorks.targetReturn(),
                country.<CountryCentralBankView>getView(CountryCentralBank.class).priceIndex(), country.<CountryCentralBankView>getView(CountryCentralBank.class).createdMoney(),
                publicWorks.sitesInProgress(), publicWorks.dailyCommitment(), maintenance.spentToday(), maintenance.satisfaction());

            if (this.detail) {
                double[] pay = new double[RELIGIOUS_INCOME_TYPE + 1];
                double social = 0.0;
                long militaryPops = 0;
                float socialSlider = StateBudgetUtils.slider(budgetPolicy.socialSpendingRate());
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    if (assistedTypes[p]) {
                        float workforceShare = EmploymentUtils.activeShare(baseWorkforceShares[p], populationPolicy.workforceShareModifier());
                        social += (double) StateBudgetUtils.scaledUnemployed(cd, p, workforceShare) * StateBudgetUtils.socialRate(cm, p, socialSlider);
                    } else {
                        float slider = StateBudgetUtils.wageSlider(incomeTypes[p], budgetPolicy, educationPolicy);
                        pay[incomeTypes[p]] += StateBudgetUtils.scaledWageBase(cd, p, statePostIndex, incomeTypes[p]) * StateBudgetUtils.wageRate(cm, p, slider);
                    }
                    if (incomeTypes[p] == MILITARY_INCOME_TYPE) {
                        militaryPops += cd.totalByPopType(p);
                    }
                }
                float spendingRatio = budget.spendingRatio() > 0f ? budget.spendingRatio() : 1f;
                double militaryPosts = 0.0;
                double militaryFilled = 0.0;
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    militaryPosts += military.posts(p);
                    militaryFilled += military.filledPosts(p);
                }
                budgetWriter.row(tick, date, name, budget.reservedNeeds(), pay[MILITARY_INCOME_TYPE], pay[ADMINISTRATION_INCOME_TYPE], pay[EDUCATION_INCOME_TYPE], pay[RELIGIOUS_INCOME_TYPE], social,
                    budget.stateConsumptionBudget() / spendingRatio, StateBudgetUtils.militarySupplyCost(military, cm), StateBudgetUtils.maintenanceCost(maintenance, cm),
                    budget.subsidyAllowance() / spendingRatio, cm.taxRevenue(), cm.tariffRevenue(), cm.profitRevenue(), budget.spendingRatio(),
                    bank.loans(STATE_OWNER), bank.creditLimit(), militaryPops, militaryPosts, militaryFilled, military.units());
            }

            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeEmployment = countryIndex != null ? this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + p] : cd.employmentByPopType(p);
                demographics.row(tick, date, name, this.popTypeNames[p],
                    cd.totalByPopType(p), popTypeEmployment,
                    cd.consciousnessByPopType(p), cd.militancyByPopType(p), cd.literacyByPopType(p), cd.savingsByPopType(p),
                    cd.lifeNeedsSatisfactionByPopType(p), cd.everydayNeedsSatisfactionByPopType(p), cd.luxuryNeedsSatisfactionByPopType(p));
                needsCosts.row(tick, date, name, this.popTypeNames[p],
                    cm.lifeCostsByPopType(p), cm.everydayCostsByPopType(p), cm.luxuryCostsByPopType(p));
            }

            int popEntities = countryIndex != null ? this.countryPopEntities[countryIndex] : 0;
            long scratchTotal = 0L;
            if (countryIndex != null) {
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    scratchTotal += this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + p];
                }
            }
            demographicsTotals.row(tick, date, name,
                cd.totalPopulation(), cd.totalEmployment(),
                cd.consciousness(), cd.militancy(), cd.literacy(), cd.savings(),
                cd.lifeNeedsSatisfaction(), cd.everydayNeedsSatisfaction(), cd.luxuryNeedsSatisfaction(),
                cd.totalChildren(), cd.totalAdults(), cd.totalSeniors(), popEntities,
                cd.lifeNeedsSatisfaction() / Math.max(1f, popEntities), scratchTotal);

            effectPolicy.row(tick, date, name,
                taxPolicy.poorTaxRate(), taxPolicy.middleTaxRate(), taxPolicy.richTaxRate(),
                budgetPolicy.socialSpendingRate(), budgetPolicy.militarySpendingRate(), educationPolicy.educationSpendingRate(), budgetPolicy.administrationSpendingRate(),
                tradePolicy.tariffRate(), profitDistributionPolicy.capitalistProfitShareRate(), profitDistributionPolicy.workerProfitShareRate(), profitDistributionPolicy.aristocratProfitShareRate(),
                profitDistributionPolicy.stateProfitShareRate(), laborPolicy.minWageFactor(), educationPolicy.educationEfficiency(),
                productionPolicy.factoryOutputModifier(), productionPolicy.factoryInputModifier(), productionPolicy.rgoOutputModifier(),
                productionPolicy.constructionSpeed(), populationPolicy.popGrowthFactor(), populationPolicy.migrationPull(), budgetPolicy.popSpending(),
                laborPolicy.slaveryAllowed() ? 1 : 0, politicalPolicy.politicalConsciousness(), politicalPolicy.politicalRadicalism(),
                politicalPolicy.suppression(), politicalPolicy.socialMobility(), politicalPolicy.classRigidity(), politicalPolicy.administrativeEfficiency(),
                culturePolicy.religiousConversionSpeed(), culturePolicy.secularism(), culturePolicy.assimilationRate(), populationPolicy.migrationPush(),
                productionPolicy.maximumEconomyScaleFactor());
        });
    }

    private void recordRgo(int tick, String date) {
        this.rgoQuery.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 1);

            for (int i = 0; i < iter.count(); i++) {
                ProvinceView province = provinceField.getMutView(i);
                ResourceGatheringView rgo = rgoField.getMutView(i);

                Integer countryIndex = this.countryIndexByEntity.get(province.ownerId());
                if (countryIndex == null) {
                    continue;
                }

                int goodIndex = rgo.goodIndex();
                long key = key(countryIndex, goodIndex);
                float[] agg = this.countryRgoByGood.computeIfAbsent(key, k -> new float[5]);
                agg[0] += rgo.production();
                agg[1] += rgo.workerAmount();
                agg[2] += rgo.slaveAmount();
                agg[3] += rgo.profit();
                agg[4] += rgo.workerMinWage();
                this.tickRgoWagesTotal += rgo.workerMinWage();
                this.tickRgoProfitTotal += rgo.profit();
                this.tickRgoTreasuryTotal += rgo.treasury();
                this.trackBalance((float) rgo.treasury(), "rgo_treasury", this.countryNames.get(countryIndex));
            }
        });

        CsvWriter writer = this.detailWriter("rgo/employment.csv");
        for (Map.Entry<Long, float[]> entry : this.countryRgoByGood.entrySet()) {
            int countryIndex = (int) (entry.getKey() >> 16);
            int goodIndex = (int) (entry.getKey() & 0xFFFF);
            float[] agg = entry.getValue();
            writer.row(tick, date, this.countryNames.get(countryIndex), this.goodNames[goodIndex],
                agg[0], agg[1], agg[2], agg[3], agg[4]);
        }
    }

    private void recordBuildings(int tick, String date) {
        this.buildingQuery.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
            long buildingTypeId = iter.src(2);

            for (int i = 0; i < iter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

                float production = economyBuilding.production();
                this.tickBuildingTreasuryTotal += economyBuilding.treasury();
                this.tickBuildingWagesTotal += economyBuilding.primaryWorkerMinWage() + economyBuilding.secondaryWorkerMinWage();
                this.tickBuildingProfitTotal += economyBuilding.profit();
                Integer typeIndex = this.buildingTypeIndexByEntity.get(buildingTypeId);
                if (typeIndex != null) {
                    this.buildingTypeProduction[typeIndex] += production;
                    if (economyBuilding.ownerTagId() == this.stateOwnerTag) {
                        this.buildingTypeStateProduction[typeIndex] += production;
                    }
                    this.buildingTypePrimaryWorkers[typeIndex] += economyBuilding.primaryWorkerAmount();
                    this.buildingTypeSecondaryWorkers[typeIndex] += economyBuilding.secondaryWorkerAmount();
                    this.buildingTypeProfit[typeIndex] += economyBuilding.profit();
                    this.buildingTypeScale[typeIndex] += economyBuilding.utilization();
                    this.buildingTypeMargin[typeIndex] += economyBuilding.unitMargin();
                    if (economyBuilding.utilization() < STOPPED_UTILIZATION && economyBuilding.unitMargin() > 0f) {
                        this.buildingTypeStoppedProfitable[typeIndex]++;
                    }
                    this.buildingTypeCount[typeIndex]++;
                }

                Integer countryIndex = this.countryIndexByEntity.get(building.countryId());
                this.trackBalance((float) economyBuilding.treasury(), "building_treasury", countryIndex != null ? this.countryNames.get(countryIndex) : "");
                if (countryIndex != null) {
                    this.countryPrimaryWorkers[countryIndex] += economyBuilding.primaryWorkerAmount();
                    this.countrySecondaryWorkers[countryIndex] += economyBuilding.secondaryWorkerAmount();

                    Integer outputGood = this.buildingTypeOutputGoodIndex.get(buildingTypeId);
                    if (outputGood != null && outputGood >= 0) {
                        long key = key(countryIndex, outputGood);
                        float[] agg = this.countryBuildingByGood.computeIfAbsent(key, k -> new float[1]);
                        agg[0] += production;
                    }
                }

                for (int slot = 0; slot < economyBuilding.activeInputGoodIndexesLength(); slot++) {
                    int goodIndex = economyBuilding.activeInputGoodIndexes(slot);
                    if (goodIndex < 0) {
                        break;
                    }
                    float demand = economyBuilding.goodInputDemandAmounts(slot);
                    if (demand > 0f) {
                        long key = ((long) buildingTypeId << 16) | goodIndex;
                        this.buildingTypeInputDemand.merge(key, demand, Float::sum);
                    }
                }
            }
        });

        CsvWriter employment = this.detailWriter("country/building_employment.csv");
        for (int c = 0; c < this.countryNames.size(); c++) {
            employment.row(tick, date, this.countryNames.get(c), this.countryPrimaryWorkers[c], this.countrySecondaryWorkers[c]);
        }

        CsvWriter inputDemand = this.detailWriter("building_types/input_demand.csv");
        for (Map.Entry<Long, Float> entry : this.buildingTypeInputDemand.entrySet()) {
            long typeId = entry.getKey() >> 16;
            int goodIndex = (int) (entry.getKey() & 0xFFFF);
            inputDemand.row(tick, date, this.ecsWorld.obtainEntity(typeId).name(), this.goodNames[goodIndex], entry.getValue());
        }
    }

    private void recordArtisans() {
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(this.ecsWorld);
        int[] outputIndexes = new int[crafts.count()];
        for (int c = 0; c < crafts.count(); c++) {
            EconomyBuildingTypeView buildingType = this.ecsWorld.obtainEntityView(crafts.buildingTypeIds(c)).getView(EconomyBuildingType.class);
            outputIndexes[c] = buildingType.goodOutputIndex();
        }
        this.artisanQuery.iter(iter -> {
            Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
            Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                RegionArtisansView regionArtisans = regionArtisansField.getMutView(i);
                RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
                Integer countryIndex = this.countryIndexByEntity.get(regionInstance.ownerId());
                if (countryIndex != null) {
                    for (int c = 0; c < outputIndexes.length; c++) {
                        float production = regionArtisans.production(c);
                        if (production > 0f) {
                            long key = ((long) countryIndex << 16) | outputIndexes[c];
                            this.countryArtisanByGood.computeIfAbsent(key, k -> new float[1])[0] += production;
                        }
                    }
                }
                this.tickArtisanTreasuryTotal += regionArtisans.treasury();
                this.tickArtisanRevenueTotal += regionArtisans.revenueToday();
                this.tickArtisanInputTotal += regionArtisans.inputCostToday();
                this.tickArtisanWagesTotal += regionArtisans.wagesToday();
            }
        });
    }

    private void recordRegionIncome(int tick, String date) {
        this.regionIncomeQuery.iter(iter -> {
            Field<RegionInstance> regionField = iter.field(RegionInstance.class, 0);
            Field<RegionInstanceIncome> incomeField = iter.field(RegionInstanceIncome.class, 1);

            for (int i = 0; i < iter.count(); i++) {
                RegionInstanceView region = regionField.getMutView(i);
                RegionInstanceIncomeView income = incomeField.getMutView(i);

                Integer countryIndex = this.countryIndexByEntity.get(region.ownerId());
                if (countryIndex == null) {
                    continue;
                }

                float ruralReserve = 0f;
                EntityView regionEntity = this.ecsWorld.obtainEntityView(iter.entity(i));
                if (regionEntity.has(RegionDemographics.class)) {
                    RegionDemographicsView regionDemographics = regionEntity.getView(RegionDemographics.class);
                    for (int p = 0; p < POP_TYPE_COUNT; p++) {
                        if (this.ruralPopTypes[p]) {
                            ruralReserve += Math.max(0, regionDemographics.totalByPopType(p) - regionDemographics.employmentByPopType(p));
                        }
                    }
                }

                float[] agg = this.countryRegionIncome.computeIfAbsent((long) countryIndex, k -> new float[6 * POP_TYPE_COUNT + 8]);
                float collected = region.taxCollectionShare();
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    agg[4 * POP_TYPE_COUNT + 5 + p] += income.claimedIncomeByPopType(p) * collected;
                    agg[5 * POP_TYPE_COUNT + 7 + p] += income.donationsByPopType(p);
                    this.tickStatePosts[p] += region.statePostsByPopType(p);
                    this.tickVacancies[p] += region.vacanciesByPopType(p);
                    this.tickUncoveredVacancies[p] += Math.max(0f, region.vacanciesByPopType(p) - ruralReserve);
                    this.tickPremiumWorkers[p] += (double) region.wagePremiumByPopType(p) * income.workersByPopType(p);
                    this.tickWorkersByPopType[p] += income.workersByPopType(p);
                    agg[p] += income.minWagesByPopType(p);
                    agg[POP_TYPE_COUNT + p] += income.workersByPopType(p);
                    agg[2 * POP_TYPE_COUNT + p] += income.profitShareByPopType(p);
                    agg[3 * POP_TYPE_COUNT + 3 + p] += income.claimedIncomeByPopType(p);
                    this.countryWorkers[countryIndex] += income.workersByPopType(p);
                }
                agg[3 * POP_TYPE_COUNT] += income.capitalistProfitShare();
                agg[3 * POP_TYPE_COUNT + 1] += income.aristocratProfitShare();
                agg[3 * POP_TYPE_COUNT + 2] += income.countryProfitShare();
                agg[4 * POP_TYPE_COUNT + 3] += income.claimedCapitalistShare();
                agg[4 * POP_TYPE_COUNT + 4] += income.claimedAristocratShare();
                agg[5 * POP_TYPE_COUNT + 5] += income.claimedCapitalistShare() * collected;
                agg[5 * POP_TYPE_COUNT + 6] += income.claimedAristocratShare() * collected;
                agg[6 * POP_TYPE_COUNT + 7] += income.claimedDonations();
            }
        });

        for (Map.Entry<Long, float[]> entry : this.countryRegionIncome.entrySet()) {
            float[] agg = entry.getValue();
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                this.tickWagesPaidTotal += agg[p];
                this.tickWorkerProfitShareTotal += agg[2 * POP_TYPE_COUNT + p];
            }
            this.tickCapitalistShareTotal += agg[3 * POP_TYPE_COUNT];
            this.tickAristocratShareTotal += agg[3 * POP_TYPE_COUNT + 1];
            this.tickCountryShareTotal += agg[3 * POP_TYPE_COUNT + 2];
        }

        CsvWriter incomeWriter = this.detailWriter("region_income/income.csv");
        CsvWriter sharesWriter = this.detailWriter("region_income/shares.csv");
        for (Map.Entry<Long, float[]> entry : this.countryRegionIncome.entrySet()) {
            int countryIndex = entry.getKey().intValue();
            float[] agg = entry.getValue();
            String name = this.countryNames.get(countryIndex);
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                incomeWriter.row(tick, date, name, this.popTypeNames[p],
                    agg[p], agg[POP_TYPE_COUNT + p], agg[2 * POP_TYPE_COUNT + p]);
            }
            sharesWriter.row(tick, date, name, agg[3 * POP_TYPE_COUNT], agg[3 * POP_TYPE_COUNT + 1], agg[3 * POP_TYPE_COUNT + 2]);
        }
    }

    private void recordAccounts(int tick, String date) {
        Arrays.fill(this.tickAccounts, 0.0);
        CsvWriter deposits = this.detailWriter("country/deposits.csv");
        this.countryQuery.each((long countryId) -> {
            Integer countryIndex = this.countryIndexByEntity.get(countryId);
            if (countryIndex == null) {
                return;
            }
            EntityView country = this.ecsWorld.obtainEntityView(countryId);
            CountryMarketView market = country.getMutView(CountryMarket.class);
            CountryTaxPolicyView taxPolicy = country.getMutView(CountryTaxPolicy.class);
            float[] regionIncome = this.countryRegionIncome.get((long) countryIndex);
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                int accountBase = (countryIndex * POP_TYPE_COUNT + p) * POP_ACCOUNT_COUNT;
                double amount = this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + p];
                if (amount > 0.0) {
                    deposits.row(tick, date, this.countryNames.get(countryIndex), this.popTypeNames[p], amount, this.countryPopTypeAccounts[accountBase + POP_DEPOSIT],
                        this.countryPopTypeAccounts[accountBase + POP_SAVINGS], this.countryPopTypeAccounts[accountBase + POP_UNSPENT], this.countryPopTypeAccounts[accountBase + POP_INCOME]);
                }
                double wages = 0.0;
                double profitShares = 0.0;
                double taxable = 0.0;
                double donations = 0.0;
                if (regionIncome != null) {
                    donations = (p == this.religiousPopTypeIndex ? regionIncome[6 * POP_TYPE_COUNT + 7] : 0.0) - regionIncome[5 * POP_TYPE_COUNT + 7 + p];
                    taxable = regionIncome[4 * POP_TYPE_COUNT + 5 + p];
                    if (p == this.capitalistPopTypeIndex) {
                        taxable += regionIncome[5 * POP_TYPE_COUNT + 5];
                    }
                    if (p == this.aristocratPopTypeIndex) {
                        taxable += regionIncome[5 * POP_TYPE_COUNT + 6];
                    }
                    double wagesPool = regionIncome[p];
                    double profitPool = regionIncome[2 * POP_TYPE_COUNT + p];
                    double claimed = regionIncome[3 * POP_TYPE_COUNT + 3 + p];
                    wages = wagesPool + profitPool > 0.0 ? claimed * wagesPool / (wagesPool + profitPool) : 0.0;
                    profitShares = claimed - wages;
                    if (p == this.capitalistPopTypeIndex) {
                        profitShares += regionIncome[4 * POP_TYPE_COUNT + 3];
                    }
                    if (p == this.aristocratPopTypeIndex) {
                        profitShares += regionIncome[4 * POP_TYPE_COUNT + 4];
                    }
                }
                double taxRate = switch (this.popTypeStrata[p]) {
                    case POOR_STRATA -> taxPolicy.poorTaxRate();
                    case MIDDLE_STRATA -> taxPolicy.middleTaxRate();
                    default -> taxPolicy.richTaxRate();
                };
                double taxes = taxable * taxRate;
                double income = this.countryPopTypeAccounts[accountBase + POP_INCOME];
                int tickBase = p * ACCOUNT_COUNT;
                this.tickAccounts[tickBase + ACCOUNT_POPULATION] += amount;
                this.tickAccounts[tickBase + ACCOUNT_EMPLOYED] += this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + p];
                this.tickAccounts[tickBase + ACCOUNT_INCOME] += income;
                this.tickAccounts[tickBase + ACCOUNT_WAGES] += wages;
                this.tickAccounts[tickBase + ACCOUNT_PROFIT_SHARES] += profitShares;
                this.tickAccounts[tickBase + ACCOUNT_TAXES] += taxes;
                this.tickAccounts[tickBase + ACCOUNT_STATE] += income - (wages + profitShares - taxes) - donations;
                this.tickAccounts[tickBase + ACCOUNT_DONATIONS] += donations;
                this.tickAccounts[tickBase + ACCOUNT_LIFE] += this.countryPopTypeAccounts[accountBase + POP_LIFE] / NEEDS_SCALING_FACTOR * market.paidLifeCostsByPopType(p);
                this.tickAccounts[tickBase + ACCOUNT_EVERYDAY] += this.countryPopTypeAccounts[accountBase + POP_EVERYDAY] / NEEDS_SCALING_FACTOR * market.paidEverydayCostsByPopType(p);
                this.tickAccounts[tickBase + ACCOUNT_LUXURY] += this.countryPopTypeAccounts[accountBase + POP_LUXURY] / NEEDS_SCALING_FACTOR * market.paidLuxuryCostsByPopType(p);
                this.tickAccounts[tickBase + ACCOUNT_SAVINGS] += this.countryPopTypeAccounts[accountBase + POP_SAVINGS];
                this.tickAccounts[tickBase + ACCOUNT_DEPOSITS] += this.countryPopTypeAccounts[accountBase + POP_UNSPENT];
            }
        });
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            this.tickAccounts[p * ACCOUNT_COUNT + ACCOUNT_WAGE_PREMIUM] = this.tickWorkersByPopType[p] > 0.0 ? this.tickPremiumWorkers[p] / this.tickWorkersByPopType[p] : 0.0;
            this.tickAccounts[p * ACCOUNT_COUNT + ACCOUNT_VACANCIES] = this.tickVacancies[p];
            this.tickAccounts[p * ACCOUNT_COUNT + ACCOUNT_UNCOVERED_VACANCIES] = this.tickUncoveredVacancies[p];
            this.tickAccounts[p * ACCOUNT_COUNT + ACCOUNT_STATE_POSTS] = this.tickStatePosts[p];
        }
        for (int i = 0; i < this.tickAccounts.length; i++) {
            this.yearAccounts[i] += this.tickAccounts[i];
        }
        if (tick == 0) {
            this.writeAccounts(0, date, this.tickAccounts, 1.0);
        }
    }

    private void writeAccounts(int year, String date, double[] accounts, double ticks) {
        CsvWriter writer = this.writer("world/accounts.csv");
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            Object[] row = new Object[4 + ACCOUNT_COUNT];
            row[0] = year;
            row[1] = date;
            row[2] = this.popTypeNames[p];
            row[3] = this.popTypeStrata[p];
            for (int a = 0; a < ACCOUNT_COUNT; a++) {
                row[4 + a] = accounts[p * ACCOUNT_COUNT + a] / ticks;
            }
            writer.row(row);
        }
    }

    private void recordEmploymentRatio(int tick, String date) {
        CsvWriter writer = this.detailWriter("country/employment_ratio.csv");
        for (int c = 0; c < this.countryNames.size(); c++) {
            float employed = this.countryPopEmployment[c];
            float workers = this.countryWorkers[c];
            float ratio = workers > 0f ? employed / workers : 0f;
            this.tickEmploymentTotal += employed;
            this.tickWorkersBookedTotal += workers;
            writer.row(tick, date, this.countryNames.get(c), employed, workers, ratio);
        }
    }

    private void sumCountryEmployment() {
        double[] savings = new double[] { 0.0 };
        double[] deposits = new double[] { 0.0 };
        this.populationQuery.iter(iter -> {
            int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                float amount = population.amount();
                int strata = this.popTypeStrata[popTypeIndex];
                savings[0] += population.savings();
                deposits[0] += population.deposit();
                this.strataAmount[strata] += amount;
                this.strataLife[strata] += amount * population.lifeNeedsSatisfaction();
                this.strataEveryday[strata] += amount * population.everydayNeedsSatisfaction();
                this.strataLuxury[strata] += amount * population.luxuryNeedsSatisfaction();
                Integer countryIndex = this.countryIndexByEntity.get(population.countryId());
                if (countryIndex != null) {
                    this.trackBalance(population.savings(), "savings", this.countryNames.get(countryIndex));
                    this.countryPopEmployment[countryIndex] += population.employment();
                    this.countryPopEntities[countryIndex]++;
                    this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + popTypeIndex] += population.amount();
                    this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + popTypeIndex] += population.employment();
                    int accountBase = (countryIndex * POP_TYPE_COUNT + popTypeIndex) * POP_ACCOUNT_COUNT;
                    this.countryPopTypeAccounts[accountBase + POP_INCOME] += population.income();
                    this.countryPopTypeAccounts[accountBase + POP_SAVINGS] += population.savings();
                    this.countryPopTypeAccounts[accountBase + POP_LIFE] += amount * population.lifeNeedsDemandRatio();
                    this.countryPopTypeAccounts[accountBase + POP_EVERYDAY] += amount * population.everydayNeedsDemandRatio();
                    this.countryPopTypeAccounts[accountBase + POP_LUXURY] += amount * population.luxuryNeedsDemandRatio();
                    this.countryPopTypeAccounts[accountBase + POP_UNSPENT] += population.unspentBudget();
                    this.countryPopTypeAccounts[accountBase + POP_DEPOSIT] += population.deposit();
                    this.countryAdults[countryIndex] += amount;
                    this.yearCountryStrataSavings[countryIndex * STRATA_COUNT + strata] += population.savings();
                    this.yearCountryStrataIncome[countryIndex * STRATA_COUNT + strata] += population.income();
                    if (strata == POOR_STRATA) {
                        this.countryPoorAmount[countryIndex] += amount;
                        this.countryPoorLife[countryIndex] += amount * population.lifeNeedsSatisfaction();
                    }
                }
            }
        });
        this.tickSavingsTotal = (float) savings[0];
        this.tickPopDepositsTotal = deposits[0];
        this.tickMoney += savings[0];
    }

    private void recordMoney(int tick, String date) {
        float inputsCost = this.tickRevenueTotal - this.tickWagesPaidTotal
            - this.tickRgoProfitTotal - this.tickBuildingProfitTotal;
        this.minInputsCost = Math.min(this.minInputsCost, inputsCost);
        if (this.tickRevenueTotal > 0f) {
            this.minSurplusRatio = Math.min(this.minSurplusRatio, inputsCost / this.tickRevenueTotal);
        }

        CsvWriter writer = this.detailWriter("money.csv");
        writer.row(tick, date,
            this.tickWagesPaidTotal, this.tickBuildingWagesTotal, this.tickRgoWagesTotal,
            this.tickRgoProfitTotal, this.tickBuildingProfitTotal, this.tickWorkerProfitShareTotal,
            this.tickCapitalistShareTotal, this.tickAristocratShareTotal, this.tickCountryShareTotal,
            this.tickRevenueTotal, this.tickTreasuryTotal, this.tickSalesRevenueTotal, this.tickBuildingTreasuryTotal, this.tickTradeMoneyTotal, this.tickTariffTotal, this.tickSavingsTotal,
            this.tickEmploymentTotal, this.tickWorkersBookedTotal,
            this.tickWorkersBookedTotal > 0f ? this.tickEmploymentTotal / this.tickWorkersBookedTotal : 0f,
            this.tickTaxTotal, this.tickStateWagesTotal, this.tickStateSocialTotal, this.tickEnterpriseSupportTotal, this.tickStateConsumptionTotal, this.tickPublicWorksSpendingTotal, this.tickMaintenanceSpendingTotal, this.tickMaintenanceNeededTotal, this.tickMilitarySupplyTotal, this.tickMilitaryNeededTotal, this.tickTargetTreasuryTotal,
            this.tickBankReservesTotal, this.tickStateLoansTotal, this.tickPrivateLoansTotal, this.tickPopDepositsTotal,
            this.tickCreatedMoneyTotal,
            this.tickLoanFlows[0], this.tickLoanFlows[1], this.tickLoanFlows[2], this.tickLoanFlows[3],
            this.tickLoanFlows[4], this.tickLoanFlows[5], this.tickLoanFlows[6], this.tickLoanFlows[7],
            this.tickFundsTotal[0], this.tickFundsTotal[1], this.tickFundsTotal[2], this.tickConstructionSpendingTotal, this.tickRgoTreasuryTotal,
            this.tickArtisanTreasuryTotal, this.tickArtisanRevenueTotal, this.tickArtisanInputTotal, this.tickArtisanWagesTotal,
            this.tickSitesInProgressTotal, this.tickSitesStartedTotal, this.tickSitesCompletedTotal);
    }

    private void commitCountryProduction(int tick, String date) {
        CsvWriter writer = this.detailWriter("country/production.csv");
        Map<Long, float[]> combined = new HashMap<>();
        for (Map.Entry<Long, float[]> entry : this.countryRgoByGood.entrySet()) {
            float[] agg = combined.computeIfAbsent(entry.getKey(), k -> new float[3]);
            agg[0] += entry.getValue()[0];
        }
        for (Map.Entry<Long, float[]> entry : this.countryBuildingByGood.entrySet()) {
            float[] agg = combined.computeIfAbsent(entry.getKey(), k -> new float[3]);
            agg[1] += entry.getValue()[0];
        }
        for (Map.Entry<Long, float[]> entry : this.countryArtisanByGood.entrySet()) {
            float[] agg = combined.computeIfAbsent(entry.getKey(), k -> new float[3]);
            agg[2] += entry.getValue()[0];
        }
        for (Map.Entry<Long, float[]> entry : combined.entrySet()) {
            int countryIndex = (int) (entry.getKey() >> 16);
            int goodIndex = (int) (entry.getKey() & 0xFFFF);
            float rgo = entry.getValue()[0];
            float building = entry.getValue()[1];
            float artisan = entry.getValue()[2];
            this.countryProductionByGood[goodIndex] += rgo + building + artisan;
            writer.row(tick, date, this.countryNames.get(countryIndex), this.goodNames[goodIndex], rgo, building, artisan, rgo + building + artisan);
        }
    }

    private void commitBuildingTypes(int tick, String date) {
        CsvWriter writer = this.detailWriter("building_types/production.csv");
        for (int t = 0; t < this.buildingTypeNames.size(); t++) {
            this.buildingProductionTotal += this.buildingTypeProduction[t];
            writer.row(tick, date, this.buildingTypeNames.get(t),
                this.buildingTypeProduction[t],
                this.buildingTypePrimaryWorkers[t],
                this.buildingTypeSecondaryWorkers[t],
                this.buildingTypeProfit[t],
                this.buildingTypeCount[t] > 0 ? this.buildingTypeScale[t] / this.buildingTypeCount[t] : 0f,
                this.buildingTypeCount[t],
                this.buildingTypeCount[t] > 0 ? this.buildingTypeMargin[t] / this.buildingTypeCount[t] : 0f,
                this.buildingTypeStoppedProfitable[t],
                this.buildingTypeStateProduction[t]);
        }
    }

    private void checkConsistency() {
        for (int g = 0; g < GOOD_COUNT; g++) {
            float world = this.worldProductionByGood[g];
            float country = this.countryProductionByGood[g];
            if (Math.abs(country - world) > 1e-3f * Math.max(1f, Math.abs(world))) {
                this.mismatchCount++;
            }
        }
    }

    private static long key(int countryIndex, int goodIndex) {
        return ((long) countryIndex << 16) | (goodIndex & 0xFFFF);
    }

    public String[] getGoodNames() {
        return this.goodNames;
    }

    public List<String> getCountryNames() {
        return this.countryNames;
    }

    public String[] getPopTypeNames() {
        return this.popTypeNames;
    }

    public List<String> getBuildingTypeNames() {
        return this.buildingTypeNames;
    }

    public double getWorldProductionTotal() {
        return this.worldProductionTotal;
    }

    public double getBuildingProductionTotal() {
        return this.buildingProductionTotal;
    }

    public long getMismatchCount() {
        return this.mismatchCount;
    }

    public double getFirstTickWorldPopulation() {
        return this.firstTickWorldPopulation;
    }

    public double getLastTickWorldPopulation() {
        return this.lastTickWorldPopulation;
    }

    public float getLastTickWorldTreasury() {
        return this.lastTickWorldTreasury;
    }

    public float getFinalWorldProductionValue() {
        float value = 0f;
        for (int g = 0; g < GOOD_COUNT; g++) {
            value += this.worldProductionByGood[g] * this.lastPrices[g];
        }
        return value;
    }

    public long getEmploymentExceedsPopulationRows() {
        return this.employmentExceedsPopulationRows;
    }

    public float getFinalTotalPopulation() {
        float total = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            total += this.finalTotalByPopType[p];
        }
        return total;
    }

    public float getFinalTotalEmployment() {
        float total = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            total += this.finalEmploymentByPopType[p];
        }
        return total;
    }

    public float getFinalWagesPaid() {
        return this.tickWagesPaidTotal;
    }

    public float getFinalBuildingWages() {
        return this.tickBuildingWagesTotal;
    }

    public float getFinalRgoWages() {
        return this.tickRgoWagesTotal;
    }

    public float getFinalArtisanWages() {
        return (float) this.tickArtisanWagesTotal;
    }

    public double getMinRealOutputRatio() {
        return this.minRealOutputRatio;
    }

    public double getStartMinRatio() {
        return this.startMinRatio;
    }

    public double getStartMaxRatio() {
        return this.startMaxRatio;
    }

    public double getRealOutputPerAdultGrowth() {
        return this.firstRealOutputPerAdult > 0.0 ? this.lastRealOutputPerAdult / this.firstRealOutputPerAdult : 0.0;
    }

    public double getMaxGoodsResidual() {
        return this.maxGoodsResidual;
    }

    public String getMaxGoodsResidualSource() {
        return this.maxGoodsResidualSource;
    }

    public double getMaxMoneyDrift() {
        return this.maxMoneyDrift;
    }

    public double getFirstMoney() {
        return this.firstMoney;
    }

    public float getMinBalance() {
        return this.minBalance;
    }

    public String getMinBalanceSource() {
        return this.minBalanceSource;
    }

    public double getMinCpi() {
        return this.minCpi;
    }

    public double getMaxCpi() {
        return this.maxCpi;
    }

    public int getGoodsOutOfBandAtLeast(int days) {
        int count = 0;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.priceOutOfBandMaxStreak[g] >= days) {
                count++;
            }
        }
        return count;
    }

    public double getMinYearlyArmySupply() {
        return this.minYearlyArmySupply;
    }

    public double getMinYearlyPoorLifeWorld() {
        return this.minYearlyPoorLifeWorld;
    }

    public double getLastYearCashHoldings() {
        return this.lastYearCashHoldings;
    }

    public String getLastYearCashHoldingsSource() {
        return this.lastYearCashHoldingsSource;
    }

    public double getMinYearlyPoorLifeCountry() {
        return this.minYearlyPoorLifeCountry;
    }

    public String getWorstPoorLifeCountry() {
        return this.worstPoorLifeCountry;
    }

    public double getAverageTickMillis() {
        return this.ticksTimed > 0 ? this.tickNanosSum / this.ticksTimed / 1_000_000.0 : 0.0;
    }

    public double getMaxTickMillis() {
        return this.tickNanosMax / 1_000_000.0;
    }

    public float getMinSurplusRatio() {
        return this.minSurplusRatio;
    }

    @Override
    public void close() throws IOException {
        this.writeMetaRows();
        for (CsvWriter writer : this.writers.values()) {
            writer.close();
        }
        this.writers.clear();
    }

    private static final class CsvWriter {
        private static final CsvWriter DISCARD = new CsvWriter();

        private final BufferedWriter writer;

        private CsvWriter() {
            this.writer = null;
        }

        CsvWriter(File file) {
            try {
                this.writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open " + file, e);
            }
        }

        void header(String... columns) {
            this.row((Object[]) columns);
        }

        void row(Object... values) {
            if (this.writer == null) {
                return;
            }
            try {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }
                    append(sb, values[i]);
                }
                this.writer.write(sb.toString());
                this.writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException("CSV write failed", e);
            }
        }

        private static void append(StringBuilder sb, Object value) {
            if (value instanceof Float || value instanceof Double) {
                sb.append(value.toString());
            } else if (value instanceof Integer || value instanceof Long) {
                sb.append(value.toString());
            } else if (value instanceof Boolean) {
                sb.append(value.toString());
            } else {
                sb.append(value == null ? "" : value.toString());
            }
        }

        void close() throws IOException {
            if (this.writer != null) {
                this.writer.close();
            }
        }
    }
}
