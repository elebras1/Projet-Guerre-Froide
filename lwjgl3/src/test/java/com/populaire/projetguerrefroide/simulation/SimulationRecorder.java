package com.populaire.projetguerrefroide.simulation;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.BuildingView;
import com.populaire.projetguerrefroide.component.Country;
import com.populaire.projetguerrefroide.component.CountryBudgetPolicy;
import com.populaire.projetguerrefroide.component.CountryBudgetPolicyView;
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
import com.populaire.projetguerrefroide.component.Population;
import com.populaire.projetguerrefroide.component.PopulationView;
import com.populaire.projetguerrefroide.component.Province;
import com.populaire.projetguerrefroide.component.ProvinceView;
import com.populaire.projetguerrefroide.component.RegionInstance;
import com.populaire.projetguerrefroide.component.RegionInstanceIncome;
import com.populaire.projetguerrefroide.component.RegionInstanceIncomeView;
import com.populaire.projetguerrefroide.component.RegionInstanceView;
import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringType;
import com.populaire.projetguerrefroide.component.ResourceGatheringTypeView;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;

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

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public class SimulationRecorder implements AutoCloseable {

    private final World ecsWorld;
    private final LocalDate startDate;

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
    private final Query regionIncomeQuery;
    private final Query populationQuery;

    private final float[] worldProductionByGood = new float[GOOD_COUNT];
    private final float[] worldDemandByGood = new float[GOOD_COUNT];
    private final float[] countryProductionByGood = new float[GOOD_COUNT];
    private final float[] countryDemandByGood = new float[GOOD_COUNT];

    private final float[] buildingTypeProduction;
    private final float[] buildingTypePrimaryWorkers;
    private final float[] buildingTypeSecondaryWorkers;
    private final float[] buildingTypeProfit;
    private final float[] buildingTypeScale;
    private final int[] buildingTypeCount;

    private final float[] countryPrimaryWorkers;
    private final float[] countrySecondaryWorkers;
    private final float[] countryPopEmployment;
    private final float[] countryWorkers;
    private final int[] countryPopEntities;
    private final int[] countryPopTypeTotal;
    private final int[] countryPopTypeEmployment;

    private final Map<Long, float[]> countryRgoByGood = new HashMap<>();
    private final Map<Long, float[]> countryBuildingByGood = new HashMap<>();
    private final Map<Long, Float> buildingTypeInputDemand = new HashMap<>();
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
    private float tickTreasuryTotal;
    private float tickSalesRevenueTotal;
    private float tickPendingRevenueTotal;
    private float tickTradeMoneyTotal;
    private float tickTariffTotal;
    private float tickSavingsTotal;
    private float tickProductionTotal;
    private double tickPopulationTotal;
    private float tickEmploymentTotal;
    private float tickWorkersBookedTotal;
    private float minSurplusRatio = Float.MAX_VALUE;
    private float minInputsCost = Float.MAX_VALUE;

    private float firstTickWorldProduction = Float.NaN;
    private float lastTickWorldProduction;
    private double firstTickWorldPopulation = -1.0;
    private double lastTickWorldPopulation;
    private float firstTickWorldTreasury = Float.NaN;
    private float lastTickWorldTreasury;
    private final float[] firstPrices = new float[GOOD_COUNT];
    private final float[] lastPrices = new float[GOOD_COUNT];
    private final float[] finalTotalByPopType = new float[POP_TYPE_COUNT];
    private final float[] finalEmploymentByPopType = new float[POP_TYPE_COUNT];
    private final float[] finalLifeSatisfactionByPopType = new float[POP_TYPE_COUNT];
    private long employmentExceedsPopulationRows = 0;

    private final Map<String, CsvWriter> writers = new HashMap<>();
    private final File outputDir;

    public SimulationRecorder(World ecsWorld, File outputDir, LocalDate startDate) {
        this.ecsWorld = ecsWorld;
        this.outputDir = outputDir;
        this.startDate = startDate;

        this.clearOutputDirectory(outputDir);

        this.countryQuery = ecsWorld.query().with(Country.class).build();
        this.rgoQuery = ecsWorld.query()
            .with(Province.class)
            .with(ResourceGathering.class)
            .build();
        this.buildingQuery = ecsWorld.query()
            .with(Building.class)
            .with(EconomyBuilding.class)
            .build();
        this.regionIncomeQuery = ecsWorld.query()
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .build();
        this.populationQuery = ecsWorld.query()
            .with(Population.class)
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

        this.countryPrimaryWorkers = new float[this.countryNames.size()];
        this.countrySecondaryWorkers = new float[this.countryNames.size()];
        this.countryPopEmployment = new float[this.countryNames.size()];
        this.countryWorkers = new float[this.countryNames.size()];
        this.countryPopEntities = new int[this.countryNames.size()];
        this.countryPopTypeTotal = new int[this.countryNames.size() * POP_TYPE_COUNT];
        this.countryPopTypeEmployment = new int[this.countryNames.size() * POP_TYPE_COUNT];

        this.openWriters();
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
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            this.popTypeNames[p] = this.ecsWorld.obtainEntity(data.popTypeIds(p)).name();
        }
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
        Query query = this.ecsWorld.query().with(EconomyBuildingType.class).build();
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

        this.writer("world/market.csv").header("tick", "date", "good", "production", "demand", "price", "pool", "leftover", "offer_total", "need_total", "trade_ratio");

        this.writer("country/market.csv").header("tick", "date", "country", "good", "price", "demand", "satisfaction", "pool", "stockpile", "stockpile_target", "stockpile_deficit", "drawing_on_stockpile", "import_need", "imported");
        this.writer("country/treasury.csv").header("tick", "date", "country", "treasury", "spending_ratio", "private_investment", "sales_revenue", "pending_revenue", "production_value", "tariff_revenue");
        this.writer("country/employment_ratio.csv").header("tick", "date", "country", "employment", "workers", "ratio");
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
        this.writer("country/production.csv").header("tick", "date", "country", "good", "rgo_production", "building_production", "total");
        this.writer("country/building_employment.csv").header("tick", "date", "country", "primary_workers", "secondary_workers");

        this.writer("building_types/production.csv").header("tick", "date", "building_type", "production", "primary_workers", "secondary_workers", "profit", "scale", "count");
        this.writer("building_types/input_demand.csv").header("tick", "date", "building_type", "good", "demand");

        this.writer("rgo/employment.csv").header("tick", "date", "country", "good", "production", "workers", "slaves", "profit", "wage");

        this.writer("region_income/income.csv").header("tick", "date", "country", "pop_type", "min_wage", "workers", "profit_share");
        this.writer("region_income/shares.csv").header("tick", "date", "country", "capitalist_share", "aristocrat_share", "country_share");

        this.writer("money.csv").header("tick", "date",
            "wages_paid", "wages_buildings", "wages_rgo",
            "profit_rgo", "profit_buildings", "profit_workers", "profit_capitalists", "profit_aristocrats", "profit_state",
            "revenue", "treasury_total", "sales_revenue_total", "pending_revenue_total", "trade_money_total", "tariff_total", "savings_total",
            "employment_total", "workers_booked", "ratio_emp_workers");
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

    public void recordTick(int tick) {
        String date = this.startDate.plusDays(tick).toString();

        this.resetScratch();
        this.sumCountryEmployment();
        this.recordWorldMarket(tick, date);
        this.recordCountryMarkets(tick, date);
        this.recordRgo(tick, date);
        this.recordBuildings(tick, date);
        this.recordRegionIncome(tick, date);
        this.recordEmploymentRatio(tick, date);
        this.commitCountryProduction(tick, date);
        this.commitBuildingTypes(tick, date);
        this.tickSavingsTotal = this.sumPopulationSavings();
        this.recordMoney(tick, date);

        this.lastTickWorldProduction = this.tickProductionTotal;
        if (Float.isNaN(this.firstTickWorldProduction)) {
            this.firstTickWorldProduction = this.tickProductionTotal;
        }
        this.lastTickWorldPopulation = this.tickPopulationTotal;
        if (this.firstTickWorldPopulation < 0.0) {
            this.firstTickWorldPopulation = this.tickPopulationTotal;
        }
        if (!Float.isNaN(this.lastTickWorldTreasury)) {
        }
        this.lastTickWorldTreasury = this.tickTreasuryTotal;
        if (Float.isNaN(this.firstTickWorldTreasury)) {
            this.firstTickWorldTreasury = this.tickTreasuryTotal;
        }

        this.checkConsistency();
    }

    private void resetScratch() {
        Arrays.fill(this.worldProductionByGood, 0f);
        Arrays.fill(this.worldDemandByGood, 0f);
        Arrays.fill(this.countryProductionByGood, 0f);
        Arrays.fill(this.countryDemandByGood, 0f);
        Arrays.fill(this.buildingTypeProduction, 0f);
        Arrays.fill(this.buildingTypePrimaryWorkers, 0f);
        Arrays.fill(this.buildingTypeSecondaryWorkers, 0f);
        Arrays.fill(this.buildingTypeProfit, 0f);
        Arrays.fill(this.buildingTypeScale, 0f);
        Arrays.fill(this.buildingTypeCount, 0);
        Arrays.fill(this.countryPrimaryWorkers, 0f);
        Arrays.fill(this.countrySecondaryWorkers, 0f);
        Arrays.fill(this.countryPopEmployment, 0f);
        Arrays.fill(this.countryWorkers, 0f);
        Arrays.fill(this.countryPopEntities, 0);
        Arrays.fill(this.countryPopTypeTotal, 0);
        Arrays.fill(this.countryPopTypeEmployment, 0);
        this.countryRgoByGood.clear();
        this.countryBuildingByGood.clear();
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
        this.tickTreasuryTotal = 0f;
        this.tickSalesRevenueTotal = 0f;
        this.tickPendingRevenueTotal = 0f;
        this.tickTradeMoneyTotal = 0f;
        this.tickTariffTotal = 0f;
        this.tickSavingsTotal = 0f;
        this.tickProductionTotal = 0f;
        this.tickPopulationTotal = 0.0;
        this.tickEmploymentTotal = 0f;
        this.tickWorkersBookedTotal = 0f;
        Arrays.fill(this.finalTotalByPopType, 0f);
        Arrays.fill(this.finalEmploymentByPopType, 0f);
        Arrays.fill(this.finalLifeSatisfactionByPopType, 0f);
    }

    private void recordWorldMarket(int tick, String date) {
        EntityView globalMarket = this.ecsWorld.obtainEntityView(this.ecsWorld.lookup("global_market"));
        GlobalMarketView data = globalMarket.getMutView(GlobalMarket.class);

        CsvWriter writer = this.writer("world/market.csv");
        for (int g = 0; g < GOOD_COUNT; g++) {
            float production = data.goodProductionAmounts(g);
            float demand = data.goodDemandAmounts(g);
            float price = data.goodPrices(g);
            this.worldProductionByGood[g] = production;
            this.worldDemandByGood[g] = demand;
            this.worldProductionTotal += production;
            this.tickProductionTotal += production;
            this.tickRevenueTotal += production * price;
            this.lastPrices[g] = price;
            if (this.firstPrices[g] == 0f) {
                this.firstPrices[g] = price;
            }
            writer.row(tick, date, this.goodNames[g], production, demand, price, data.goodAmountsPool(g), data.goodLeftoverAmounts(g),
                data.goodOfferTotals(g), data.goodNeedTotals(g), data.goodTradeRatios(g));
            this.tickTradeMoneyTotal += data.goodTradeMoney(g);
        }
    }

    private void recordCountryMarkets(int tick, String date) {
        CsvWriter market = this.writer("country/market.csv");
        CsvWriter treasury = this.writer("country/treasury.csv");
        CsvWriter demographics = this.writer("country/demographics.csv");
        CsvWriter demographicsTotals = this.writer("country/demographics_totals.csv");
        CsvWriter needsCosts = this.writer("country/needs_costs.csv");
        CsvWriter effectPolicy = this.writer("country/effect_policy.csv");

        this.countryQuery.each((long countryId) -> {
            EntityView country = this.ecsWorld.obtainEntityView(countryId);
            CountryMarketView cm = country.getMutView(CountryMarket.class);
            CountryDemographicsView cd = country.getMutView(CountryDemographics.class);
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
            this.tickSalesRevenueTotal += cm.salesRevenue();
            this.tickPendingRevenueTotal += cm.pendingRevenue();
            this.tickTariffTotal += cm.tariffRevenue();
            for (int p = 0; p < POP_TYPE_COUNT; p++) {
                float popTypeTotal = countryIndex != null ? this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + p] : cd.totalByPopType(p);
                float popTypeEmployment = countryIndex != null ? this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + p] : cd.employmentByPopType(p);
                if (popTypeEmployment > popTypeTotal) {
                    this.employmentExceedsPopulationRows++;
                }
                this.finalTotalByPopType[p] += popTypeTotal;
                this.finalEmploymentByPopType[p] += popTypeEmployment;
                this.finalLifeSatisfactionByPopType[p] += cd.lifeNeedsSatisfactionByPopType(p) * popTypeTotal;
            }

            for (int g = 0; g < GOOD_COUNT; g++) {
                float demand = cm.goodDemandAmounts(g);
                this.countryDemandByGood[g] += demand;
                market.row(tick, date, name, this.goodNames[g],
                    cm.goodPrices(g), demand, cm.goodDemandSatisfactionRatios(g),
                    cm.goodAmountsPool(g), cm.goodStockpiles(g), cm.goodStockpileTargets(g),
                    cm.goodStockpileDailyDeficits(g), cm.goodDrawingOnStockpiles(g) ? 1 : 0, cm.goodImportNeeds(g),
                    cm.goodImportedAmounts(g));
            }

            treasury.row(tick, date, name, cm.treasury(), cm.spendingRatio(), cm.privateInvestmentAmount(), cm.salesRevenue(), cm.pendingRevenue(), cm.productionValue(), cm.tariffRevenue());

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
            }
        });

        CsvWriter writer = this.writer("rgo/employment.csv");
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

            for (int i = 0; i < iter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

                float production = economyBuilding.production();
                this.tickBuildingWagesTotal += economyBuilding.primaryWorkerMinWage() + economyBuilding.secondaryWorkerMinWage();
                this.tickBuildingProfitTotal += economyBuilding.profit();
                Integer typeIndex = this.buildingTypeIndexByEntity.get(building.typeId());
                if (typeIndex != null) {
                    this.buildingTypeProduction[typeIndex] += production;
                    this.buildingTypePrimaryWorkers[typeIndex] += economyBuilding.primaryWorkerAmount();
                    this.buildingTypeSecondaryWorkers[typeIndex] += economyBuilding.secondaryWorkerAmount();
                    this.buildingTypeProfit[typeIndex] += economyBuilding.profit();
                    this.buildingTypeScale[typeIndex] += economyBuilding.scale();
                    this.buildingTypeCount[typeIndex]++;
                }

                Integer countryIndex = this.countryIndexByEntity.get(building.countryId());
                if (countryIndex != null) {
                    this.countryPrimaryWorkers[countryIndex] += economyBuilding.primaryWorkerAmount();
                    this.countrySecondaryWorkers[countryIndex] += economyBuilding.secondaryWorkerAmount();

                    Integer outputGood = this.buildingTypeOutputGoodIndex.get(building.typeId());
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
                        long key = ((long) building.typeId() << 16) | goodIndex;
                        this.buildingTypeInputDemand.merge(key, demand, Float::sum);
                    }
                }
            }
        });

        CsvWriter employment = this.writer("country/building_employment.csv");
        for (int c = 0; c < this.countryNames.size(); c++) {
            employment.row(tick, date, this.countryNames.get(c), this.countryPrimaryWorkers[c], this.countrySecondaryWorkers[c]);
        }

        CsvWriter inputDemand = this.writer("building_types/input_demand.csv");
        for (Map.Entry<Long, Float> entry : this.buildingTypeInputDemand.entrySet()) {
            long typeId = entry.getKey() >> 16;
            int goodIndex = (int) (entry.getKey() & 0xFFFF);
            inputDemand.row(tick, date, this.ecsWorld.obtainEntity(typeId).name(), this.goodNames[goodIndex], entry.getValue());
        }
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

                float[] agg = this.countryRegionIncome.computeIfAbsent((long) countryIndex, k -> new float[3 * POP_TYPE_COUNT + 3]);
                for (int p = 0; p < POP_TYPE_COUNT; p++) {
                    agg[p] += income.minWagesByPopType(p);
                    agg[POP_TYPE_COUNT + p] += income.workersByPopType(p);
                    agg[2 * POP_TYPE_COUNT + p] += income.profitShareByPopType(p);
                    this.countryWorkers[countryIndex] += income.workersByPopType(p);
                }
                agg[3 * POP_TYPE_COUNT] += income.capitalistProfitShare();
                agg[3 * POP_TYPE_COUNT + 1] += income.aristocratProfitShare();
                agg[3 * POP_TYPE_COUNT + 2] += income.countryProfitShare();
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

        CsvWriter incomeWriter = this.writer("region_income/income.csv");
        CsvWriter sharesWriter = this.writer("region_income/shares.csv");
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

    private void recordEmploymentRatio(int tick, String date) {
        CsvWriter writer = this.writer("country/employment_ratio.csv");
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
        this.populationQuery.iter(iter -> {
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                Integer countryIndex = this.countryIndexByEntity.get(population.countryId());
                if (countryIndex != null) {
                    this.countryPopEmployment[countryIndex] += population.employment();
                    this.countryPopEntities[countryIndex]++;
                    this.countryPopTypeTotal[countryIndex * POP_TYPE_COUNT + population.index()] += population.amount();
                    this.countryPopTypeEmployment[countryIndex * POP_TYPE_COUNT + population.index()] += population.employment();
                }
            }
        });
    }

    private void recordMoney(int tick, String date) {
        float inputsCost = this.tickRevenueTotal - this.tickWagesPaidTotal
            - this.tickRgoProfitTotal - this.tickBuildingProfitTotal;
        this.minInputsCost = Math.min(this.minInputsCost, inputsCost);
        if (this.tickRevenueTotal > 0f) {
            this.minSurplusRatio = Math.min(this.minSurplusRatio, inputsCost / this.tickRevenueTotal);
        }

        CsvWriter writer = this.writer("money.csv");
        writer.row(tick, date,
            this.tickWagesPaidTotal, this.tickBuildingWagesTotal, this.tickRgoWagesTotal,
            this.tickRgoProfitTotal, this.tickBuildingProfitTotal, this.tickWorkerProfitShareTotal,
            this.tickCapitalistShareTotal, this.tickAristocratShareTotal, this.tickCountryShareTotal,
            this.tickRevenueTotal, this.tickTreasuryTotal, this.tickSalesRevenueTotal, this.tickPendingRevenueTotal, this.tickTradeMoneyTotal, this.tickTariffTotal, this.tickSavingsTotal,
            this.tickEmploymentTotal, this.tickWorkersBookedTotal,
            this.tickWorkersBookedTotal > 0f ? this.tickEmploymentTotal / this.tickWorkersBookedTotal : 0f);
    }

    private float sumPopulationSavings() {
        float[] total = new float[] { 0f };
        this.populationQuery.iter(iter -> {
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                total[0] += population.savings();
            }
        });
        return total[0];
    }

    private void commitCountryProduction(int tick, String date) {
        CsvWriter writer = this.writer("country/production.csv");
        Map<Long, float[]> combined = new HashMap<>();
        for (Map.Entry<Long, float[]> entry : this.countryRgoByGood.entrySet()) {
            float[] agg = combined.computeIfAbsent(entry.getKey(), k -> new float[2]);
            agg[0] += entry.getValue()[0];
        }
        for (Map.Entry<Long, float[]> entry : this.countryBuildingByGood.entrySet()) {
            float[] agg = combined.computeIfAbsent(entry.getKey(), k -> new float[2]);
            agg[1] += entry.getValue()[0];
        }
        for (Map.Entry<Long, float[]> entry : combined.entrySet()) {
            int countryIndex = (int) (entry.getKey() >> 16);
            int goodIndex = (int) (entry.getKey() & 0xFFFF);
            float rgo = entry.getValue()[0];
            float building = entry.getValue()[1];
            this.countryProductionByGood[goodIndex] += rgo + building;
            writer.row(tick, date, this.countryNames.get(countryIndex), this.goodNames[goodIndex], rgo, building, rgo + building);
        }
    }

    private void commitBuildingTypes(int tick, String date) {
        CsvWriter writer = this.writer("building_types/production.csv");
        for (int t = 0; t < this.buildingTypeNames.size(); t++) {
            this.buildingProductionTotal += this.buildingTypeProduction[t];
            writer.row(tick, date, this.buildingTypeNames.get(t),
                this.buildingTypeProduction[t],
                this.buildingTypePrimaryWorkers[t],
                this.buildingTypeSecondaryWorkers[t],
                this.buildingTypeProfit[t],
                this.buildingTypeCount[t] > 0 ? this.buildingTypeScale[t] / this.buildingTypeCount[t] : 0f,
                this.buildingTypeCount[t]);
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

    public float getFirstTickWorldProduction() {
        return this.firstTickWorldProduction;
    }

    public float getLastTickWorldProduction() {
        return this.lastTickWorldProduction;
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

    public float getPriceDriftMin() {
        float min = Float.MAX_VALUE;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.firstPrices[g] > 0f) {
                min = Math.min(min, this.lastPrices[g] / this.firstPrices[g]);
            }
        }
        return min;
    }

    public float getPriceDriftMax() {
        float max = 0f;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.firstPrices[g] > 0f) {
                max = Math.max(max, this.lastPrices[g] / this.firstPrices[g]);
            }
        }
        return max;
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

    public int getStarvingPopTypesCount() {
        int starving = 0;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (this.finalTotalByPopType[p] > 0f
                && this.finalLifeSatisfactionByPopType[p] <= 0f) {
                starving++;
            }
        }
        return starving;
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
        private final BufferedWriter writer;

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
            this.writer.close();
        }
    }
}
