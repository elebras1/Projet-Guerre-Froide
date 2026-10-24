package com.populaire.projetguerrefroide.simulation;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.BuildingView;
import com.populaire.projetguerrefroide.component.Demographics;
import com.populaire.projetguerrefroide.component.DemographicsView;
import com.populaire.projetguerrefroide.component.EconomyBuilding;
import com.populaire.projetguerrefroide.component.EconomyBuildingType;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;
import com.populaire.projetguerrefroide.component.EconomyBuildingView;
import com.populaire.projetguerrefroide.component.GlobalGood;
import com.populaire.projetguerrefroide.component.GlobalGoodView;
import com.populaire.projetguerrefroide.component.GlobalPopulationType;
import com.populaire.projetguerrefroide.component.GlobalPopulationTypeView;
import com.populaire.projetguerrefroide.component.Good;
import com.populaire.projetguerrefroide.component.GoodView;
import com.populaire.projetguerrefroide.component.Population;
import com.populaire.projetguerrefroide.component.PopulationType;
import com.populaire.projetguerrefroide.component.PopulationTypeView;
import com.populaire.projetguerrefroide.component.PopulationView;
import com.populaire.projetguerrefroide.component.ResourceGathering;
import com.populaire.projetguerrefroide.component.ResourceGatheringView;
import com.populaire.projetguerrefroide.component.Unit;
import com.populaire.projetguerrefroide.component.UnitInstance;
import com.populaire.projetguerrefroide.component.UnitInstanceView;
import com.populaire.projetguerrefroide.component.UnitView;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_YEAR;
import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public final class CalibrationReport {

    public static final float RATIO_LOW = 0.9f;
    public static final float RATIO_HIGH = 1.3f;
    public static final float RESOURCE_RATIO_LOW = 1.1f;
    public static final float RESOURCE_RATIO_HIGH = 1.6f;
    public static final float RATIO_TARGET = 1.1f;

    private static final float[] EVERYDAY_TARGET_BY_STRATA = { 0.3f, 0.6f, 1.0f };
    private static final float[] LUXURY_TARGET_BY_STRATA = { 0.0f, 0.2f, 1.0f };
    private static final int ARMY_UTILIZATION_ITERATIONS = 20;
    private static final String CONSTRUCTION_ANCHOR_GOOD = "cement";
    private static final Map<String, Float> CONSTRUCTION_BASKET = Map.of("cement", 1.0f, "steel", 1.0f, "machine_parts", 0.35f, "glass", 0.35f);
    private static final String[] RGO_COLUMNS = { "provinces", "farmers", "labourers", "slaves", "workers", "employed_slaves", "production" };
    private static final String[] BUILDING_COLUMNS = { "buildings", "levels", "output_per_level", "primary_needed", "primary_employed", "secondary_needed", "secondary_employed", "production", "input_demand" };

    private final String[] goodNames = new String[GOOD_COUNT];
    private final float[] baseCosts = new float[GOOD_COUNT];
    private final boolean[] armyGoods = new boolean[GOOD_COUNT];
    private final boolean[] resourceGoods = new boolean[GOOD_COUNT];
    private final double[] constructionDemand = new double[GOOD_COUNT];
    private final double[] rgoSupply = new double[GOOD_COUNT];
    private final double[] buildingSupply = new double[GOOD_COUNT];
    private final double[] populationDemand = new double[GOOD_COUNT];
    private final double[] inputDemand = new double[GOOD_COUNT];
    private final double[] armyDemand = new double[GOOD_COUNT];
    private final List<String> outOfBandGoods = new ArrayList<>();
    private final Map<String, double[]> buildingTypes = new TreeMap<>();
    private final Map<String, double[]> buildingInputs = new TreeMap<>();
    private final Map<String, Integer> buildingOutputGoods = new HashMap<>();
    private final double[] utilization = new double[GOOD_COUNT];
    private final double[][] rgoWorkforce = new double[GOOD_COUNT][RGO_COLUMNS.length];
    private final double[] ruralsWithoutRgo = new double[2];
    private int demandedGoods;

    private CalibrationReport() {
    }

    public static CalibrationReport measure(World ecsWorld) {
        CalibrationReport report = new CalibrationReport();
        report.readGoods(ecsWorld);
        report.measureRgoSupply(ecsWorld);
        report.measureBuildings(ecsWorld);
        report.measurePopulationDemand(ecsWorld);
        report.measureRgoWorkforce(ecsWorld);
        report.measureConstructionDemand();
        report.measureArmyDemand(ecsWorld);
        report.resolveArmyUtilization();
        report.classify();
        return report;
    }

    private void readGoods(World ecsWorld) {
        GlobalGoodView globalGood = ecsWorld.obtainEntityView(ecsWorld.lookup("global_good")).getMutView(GlobalGood.class);
        long militaryGoodTag = ecsWorld.lookup(EcsConstants.MILITARY_GOOD_TAG);
        long resourceGoodTag = ecsWorld.lookup(EcsConstants.RESSOURCE_GOOD_TAG);
        for (int g = 0; g < GOOD_COUNT; g++) {
            long goodId = globalGood.goodIds(g);
            this.goodNames[g] = ecsWorld.obtainEntity(goodId).name();
            GoodView good = ecsWorld.obtainEntityView(goodId).getMutView(Good.class);
            this.baseCosts[g] = good.cost();
            this.armyGoods[g] = ecsWorld.obtainEntity(goodId).has(militaryGoodTag);
            this.resourceGoods[g] = ecsWorld.obtainEntity(goodId).has(resourceGoodTag);
        }
    }

    private void measureRgoSupply(World ecsWorld) {
        Query query = ecsWorld.query().with(ResourceGathering.class).build();
        query.iter(iter -> {
            Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);
                this.rgoSupply[resourceGathering.goodIndex()] += resourceGathering.production();
            }
        });
    }

    private void measureBuildings(World ecsWorld) {
        Map<Long, Integer> outputGoodByType = new HashMap<>();
        Map<Long, float[]> workforceByType = new HashMap<>();
        Map<Long, String> typeNames = new HashMap<>();
        Query typeQuery = ecsWorld.query().with(EconomyBuildingType.class).self().with(Flecs.Prefab).build();
        List<Long> typeIds = new ArrayList<>();
        typeQuery.each((long id) -> typeIds.add(id));
        for (long typeId : typeIds) {
            EconomyBuildingTypeView type = ecsWorld.obtainEntityView(typeId).getMutView(EconomyBuildingType.class);
            outputGoodByType.put(typeId, type.goodOutputIndex());
            workforceByType.put(typeId, new float[] { type.workforce() * type.primaryWorkerPopTypeRatio(), type.workforce() * type.secondaryWorkerPopTypeRatio(), type.goodOutputAmount() });
            typeNames.put(typeId, ecsWorld.obtainEntity(typeId).name());
        }

        Query query = ecsWorld.query().with(Building.class).with(EconomyBuilding.class).with(EconomyBuildingType.class).build();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
            long buildingTypeId = iter.src(2);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
                Integer outputGood = outputGoodByType.get(buildingTypeId);
                if (outputGood != null && outputGood >= 0) {
                    this.buildingSupply[outputGood] += economyBuilding.production();
                }
                float[] workforce = workforceByType.get(buildingTypeId);
                double[] typeTotals = this.buildingTypes.computeIfAbsent(typeNames.get(buildingTypeId), name -> new double[BUILDING_COLUMNS.length]);
                double[] typeInputs = this.buildingInputs.computeIfAbsent(typeNames.get(buildingTypeId), name -> new double[GOOD_COUNT]);
                this.buildingOutputGoods.put(typeNames.get(buildingTypeId), outputGood);
                typeTotals[0] += 1;
                typeTotals[1] += building.size();
                typeTotals[2] = workforce[2];
                typeTotals[3] += building.size() * workforce[0];
                typeTotals[4] += economyBuilding.primaryWorkerAmount();
                typeTotals[5] += building.size() * workforce[1];
                typeTotals[6] += economyBuilding.secondaryWorkerAmount();
                typeTotals[7] += economyBuilding.production();
                for (int slot = 0; slot < economyBuilding.activeInputGoodIndexesLength(); slot++) {
                    int goodIndex = economyBuilding.activeInputGoodIndexes(slot);
                    if (goodIndex < 0) {
                        break;
                    }
                    typeTotals[8] += economyBuilding.goodInputDemandAmounts(slot);
                    typeInputs[goodIndex] += economyBuilding.goodInputDemandAmounts(slot);
                }
            }
        });
    }

    private void measurePopulationDemand(World ecsWorld) {
        GlobalPopulationTypeView globalPopType = ecsWorld.obtainEntityView(ecsWorld.lookup("global_population_type")).getMutView(GlobalPopulationType.class);
        float[][] targetBaskets = new float[POP_TYPE_COUNT][GOOD_COUNT];
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            PopulationTypeView popType = ecsWorld.obtainEntityView(globalPopType.popTypeIds(p)).getMutView(PopulationType.class);
            float everydayTarget = EVERYDAY_TARGET_BY_STRATA[popType.strata()];
            float luxuryTarget = LUXURY_TARGET_BY_STRATA[popType.strata()];
            for (int j = 0; j < popType.lifeNeedsGoodIndexesLength(); j++) {
                int goodIndex = popType.lifeNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                targetBaskets[p][goodIndex] += popType.lifeNeedsGoodAmounts(j);
            }
            for (int j = 0; j < popType.everydayNeedsGoodIndexesLength(); j++) {
                int goodIndex = popType.everydayNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                targetBaskets[p][goodIndex] += everydayTarget * popType.everydayNeedsGoodAmounts(j);
            }
            for (int j = 0; j < popType.luxuryNeedsGoodIndexesLength(); j++) {
                int goodIndex = popType.luxuryNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                targetBaskets[p][goodIndex] += luxuryTarget * popType.luxuryNeedsGoodAmounts(j);
            }
        }

        double[] amountByPopType = new double[POP_TYPE_COUNT];
        Query query = ecsWorld.query().with(Population.class).with(PopulationType.class).build();
        query.iter(iter -> {
            int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                amountByPopType[popTypeIndex] += population.amount();
            }
        });

        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            double scaledAmount = amountByPopType[p] / NEEDS_SCALING_FACTOR;
            for (int g = 0; g < GOOD_COUNT; g++) {
                this.populationDemand[g] += scaledAmount * targetBaskets[p][g];
            }
        }
    }

    private void measureRgoWorkforce(World ecsWorld) {
        GlobalPopulationTypeView globalPopType = ecsWorld.obtainEntityView(ecsWorld.lookup("global_population_type")).getMutView(GlobalPopulationType.class);
        int farmers = -1;
        int labourers = -1;
        int slaves = -1;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            String name = ecsWorld.obtainEntity(globalPopType.popTypeIds(p)).name();
            if (name.equals("farmers")) {
                farmers = p;
            } else if (name.equals("labourers")) {
                labourers = p;
            } else if (name.equals("slaves")) {
                slaves = p;
            }
        }
        int farmerIndex = farmers;
        int labourerIndex = labourers;
        int slaveIndex = slaves;

        Query query = ecsWorld.query().with(Demographics.class).build();
        query.iter(iter -> {
            Field<Demographics> demographicsField = iter.field(Demographics.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                DemographicsView demographics = demographicsField.getMutView(i);
                EntityView province = iter.world().obtainEntityView(iter.entity(i));
                if (!province.has(ResourceGathering.class)) {
                    this.ruralsWithoutRgo[0] += demographics.totalByPopType(farmerIndex);
                    this.ruralsWithoutRgo[1] += demographics.totalByPopType(labourerIndex);
                    continue;
                }
                ResourceGatheringView resourceGathering = province.getMutView(ResourceGathering.class);
                double[] totals = this.rgoWorkforce[resourceGathering.goodIndex()];
                totals[0] += 1;
                totals[1] += demographics.totalByPopType(farmerIndex);
                totals[2] += demographics.totalByPopType(labourerIndex);
                totals[3] += demographics.totalByPopType(slaveIndex);
                totals[4] += resourceGathering.workerAmount();
                totals[5] += resourceGathering.slaveAmount();
                totals[6] += resourceGathering.production();
            }
        });
    }

    private void measureConstructionDemand() {
        int anchor = this.goodIndex(CONSTRUCTION_ANCHOR_GOOD);
        double constructionRate = this.supply(anchor) / RATIO_TARGET;
        for (Map.Entry<String, Float> entry : CONSTRUCTION_BASKET.entrySet()) {
            this.constructionDemand[this.goodIndex(entry.getKey())] = constructionRate * entry.getValue();
        }
    }

    private void measureArmyDemand(World ecsWorld) {
        Query query = ecsWorld.query().with(UnitInstance.class).build();
        query.iter(iter -> {
            Field<UnitInstance> unitInstanceField = iter.field(UnitInstance.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                UnitInstanceView unitInstance = unitInstanceField.getMutView(i);
                UnitView unit = ecsWorld.obtainEntityView(unitInstance.typeId()).getView(Unit.class);
                float scale = unit.supplyConsumption() * unitInstance.strength();
                for (int k = 0; k < unit.supplyCostGoodIndexesLength(); k++) {
                    int goodIndex = unit.supplyCostGoodIndexes(k);
                    if (goodIndex < 0) {
                        break;
                    }
                    this.armyDemand[goodIndex] += unit.supplyCostAmounts(k) * scale / DAYS_PER_YEAR;
                }
            }
        });
    }

    private void resolveArmyUtilization() {
        Arrays.fill(this.utilization, 1.0);
        for (int iteration = 0; iteration < ARMY_UTILIZATION_ITERATIONS; iteration++) {
            Arrays.fill(this.inputDemand, 0.0);
            for (Map.Entry<String, double[]> entry : this.buildingInputs.entrySet()) {
                double typeUtilization = this.utilization[this.buildingOutputGoods.get(entry.getKey())];
                for (int g = 0; g < GOOD_COUNT; g++) {
                    this.inputDemand[g] += entry.getValue()[g] * typeUtilization;
                }
            }
            for (int g = 0; g < GOOD_COUNT; g++) {
                if (this.armyGoods[g] && this.supply(g) > 0.0) {
                    this.utilization[g] = Math.min(1.0, RATIO_TARGET * this.demand(g) / this.supply(g));
                }
            }
        }
    }

    private int goodIndex(String goodName) {
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (this.goodNames[g].equals(goodName)) {
                return g;
            }
        }
        throw new IllegalArgumentException("Unknown good: " + goodName);
    }

    private void classify() {
        for (int g = 0; g < GOOD_COUNT; g++) {
            double demand = this.demand(g);
            if (demand <= 0.0 || this.armyGoods[g]) {
                continue;
            }
            this.demandedGoods++;
            double ratio = this.supply(g) / demand;
            if (ratio < this.ratioLow(g) || ratio > this.ratioHigh(g)) {
                this.outOfBandGoods.add(String.format(Locale.ROOT, "%s=%.2f", this.goodNames[g], ratio));
            }
        }
    }

    private float ratioLow(int good) {
        return this.resourceGoods[good] ? RESOURCE_RATIO_LOW : RATIO_LOW;
    }

    private float ratioHigh(int good) {
        return this.resourceGoods[good] ? RESOURCE_RATIO_HIGH : RATIO_HIGH;
    }

    private double supply(int good) {
        return this.rgoSupply[good] + this.buildingSupply[good];
    }

    private double demand(int good) {
        return this.populationDemand[good] + this.inputDemand[good] + this.constructionDemand[good] + this.armyDemand[good];
    }

    private String status(int good) {
        double supply = this.supply(good);
        double demand = this.demand(good);
        if (this.armyGoods[good]) {
            return "army";
        }
        if (demand <= 0.0) {
            return supply > 0.0 ? "no_demand" : "unused";
        }
        if (supply <= 0.0) {
            return "no_supply";
        }
        double ratio = supply / demand;
        if (ratio < this.ratioLow(good)) {
            return "deficit";
        }
        if (ratio > this.ratioHigh(good)) {
            return "surplus";
        }
        return "ok";
    }

    public void write(File outputDir) throws IOException {
        File file = new File(outputDir, "calibration/goods.csv");
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("good,base_cost,rgo_supply,building_supply,supply,population_demand_target,input_demand,construction_demand,demand,ratio,utilization,status");
            writer.newLine();
            for (int g = 0; g < GOOD_COUNT; g++) {
                double demand = this.demand(g);
                double ratio = demand > 0.0 ? this.supply(g) / demand : 0.0;
                writer.write(String.format(Locale.ROOT, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    this.goodNames[g], this.baseCosts[g], this.rgoSupply[g], this.buildingSupply[g], this.supply(g),
                    this.populationDemand[g], this.inputDemand[g], this.constructionDemand[g], demand, ratio, this.utilization[g], this.status(g)));
                writer.newLine();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(new File(outputDir, "calibration/rgo.csv").toPath(), StandardCharsets.UTF_8)) {
            writer.write("good," + String.join(",", RGO_COLUMNS));
            writer.newLine();
            for (int g = 0; g < GOOD_COUNT; g++) {
                if (this.rgoWorkforce[g][0] == 0) {
                    continue;
                }
                writer.write(this.goodNames[g]);
                for (double value : this.rgoWorkforce[g]) {
                    writer.write(String.format(Locale.ROOT, ",%s", value));
                }
                writer.newLine();
            }
            writer.write(String.format(Locale.ROOT, "without_rgo,0,%s,%s,0,0,0,0", this.ruralsWithoutRgo[0], this.ruralsWithoutRgo[1]));
            writer.newLine();
        }

        try (BufferedWriter writer = Files.newBufferedWriter(new File(outputDir, "calibration/buildings.csv").toPath(), StandardCharsets.UTF_8)) {
            writer.write("building," + String.join(",", BUILDING_COLUMNS));
            writer.newLine();
            for (Map.Entry<String, double[]> entry : this.buildingTypes.entrySet()) {
                writer.write(entry.getKey());
                for (double value : entry.getValue()) {
                    writer.write(String.format(Locale.ROOT, ",%s", value));
                }
                writer.newLine();
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(new File(outputDir, "calibration/building_inputs.csv").toPath(), StandardCharsets.UTF_8)) {
            writer.write("building,good,input_demand");
            writer.newLine();
            for (Map.Entry<String, double[]> entry : this.buildingInputs.entrySet()) {
                for (int g = 0; g < GOOD_COUNT; g++) {
                    if (entry.getValue()[g] > 0.0) {
                        writer.write(String.format(Locale.ROOT, "%s,%s,%s", entry.getKey(), this.goodNames[g], entry.getValue()[g]));
                        writer.newLine();
                    }
                }
            }
        }
    }

    public List<String> getOutOfBandGoods() {
        return this.outOfBandGoods;
    }

    public int getDemandedGoods() {
        return this.demandedGoods;
    }

    public int getGoodsWithoutDemand() {
        int count = 0;
        for (int g = 0; g < GOOD_COUNT; g++) {
            if (!this.armyGoods[g] && this.demand(g) <= 0.0 && this.supply(g) > 0.0) {
                count++;
            }
        }
        return count;
    }
}
