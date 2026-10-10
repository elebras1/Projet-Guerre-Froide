package com.populaire.projetguerrefroide.simulation;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.Building;
import com.populaire.projetguerrefroide.component.BuildingView;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;
import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public final class CalibrationReport {

    public static final float RATIO_LOW = 0.9f;
    public static final float RATIO_HIGH = 1.3f;

    private static final float[] EVERYDAY_TARGET_BY_STRATA = { 0.3f, 0.6f, 1.0f };
    private static final float[] LUXURY_TARGET_BY_STRATA = { 0.0f, 0.2f, 1.0f };

    private final String[] goodNames = new String[GOOD_COUNT];
    private final float[] baseCosts = new float[GOOD_COUNT];
    private final double[] rgoSupply = new double[GOOD_COUNT];
    private final double[] buildingSupply = new double[GOOD_COUNT];
    private final double[] populationDemand = new double[GOOD_COUNT];
    private final double[] inputDemand = new double[GOOD_COUNT];
    private final List<String> outOfBandGoods = new ArrayList<>();
    private int demandedGoods;

    private CalibrationReport() {
    }

    public static CalibrationReport measure(World ecsWorld) {
        CalibrationReport report = new CalibrationReport();
        report.readGoods(ecsWorld);
        report.measureRgoSupply(ecsWorld);
        report.measureBuildings(ecsWorld);
        report.measurePopulationDemand(ecsWorld);
        report.classify();
        return report;
    }

    private void readGoods(World ecsWorld) {
        GlobalGoodView globalGood = ecsWorld.obtainEntityView(ecsWorld.lookup("global_good")).getMutView(GlobalGood.class);
        for (int g = 0; g < GOOD_COUNT; g++) {
            long goodId = globalGood.goodIds(g);
            this.goodNames[g] = ecsWorld.obtainEntity(goodId).name();
            GoodView good = ecsWorld.obtainEntityView(goodId).getMutView(Good.class);
            this.baseCosts[g] = good.cost();
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
        Query typeQuery = ecsWorld.query().with(EconomyBuildingType.class).build();
        List<Long> typeIds = new ArrayList<>();
        typeQuery.each((long id) -> typeIds.add(id));
        for (long typeId : typeIds) {
            EconomyBuildingTypeView type = ecsWorld.obtainEntityView(typeId).getMutView(EconomyBuildingType.class);
            outputGoodByType.put(typeId, type.goodOutputIndex());
        }

        Query query = ecsWorld.query().with(Building.class).with(EconomyBuilding.class).build();
        query.iter(iter -> {
            Field<Building> buildingField = iter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
                Integer outputGood = outputGoodByType.get(building.typeId());
                if (outputGood != null && outputGood >= 0) {
                    this.buildingSupply[outputGood] += economyBuilding.production();
                }
                for (int slot = 0; slot < economyBuilding.activeInputGoodIndexesLength(); slot++) {
                    int goodIndex = economyBuilding.activeInputGoodIndexes(slot);
                    if (goodIndex < 0) {
                        break;
                    }
                    this.inputDemand[goodIndex] += economyBuilding.goodInputDemandAmounts(slot);
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
        Query query = ecsWorld.query().with(Population.class).build();
        query.iter(iter -> {
            Field<Population> populationField = iter.field(Population.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                PopulationView population = populationField.getMutView(i);
                amountByPopType[population.index()] += population.amount();
            }
        });

        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            double scaledAmount = amountByPopType[p] / NEEDS_SCALING_FACTOR;
            for (int g = 0; g < GOOD_COUNT; g++) {
                this.populationDemand[g] += scaledAmount * targetBaskets[p][g];
            }
        }
    }

    private void classify() {
        for (int g = 0; g < GOOD_COUNT; g++) {
            double demand = this.demand(g);
            if (demand <= 0.0) {
                continue;
            }
            this.demandedGoods++;
            double ratio = this.supply(g) / demand;
            if (ratio < RATIO_LOW || ratio > RATIO_HIGH) {
                this.outOfBandGoods.add(String.format(Locale.ROOT, "%s=%.2f", this.goodNames[g], ratio));
            }
        }
    }

    private double supply(int good) {
        return this.rgoSupply[good] + this.buildingSupply[good];
    }

    private double demand(int good) {
        return this.populationDemand[good] + this.inputDemand[good];
    }

    private String status(int good) {
        double supply = this.supply(good);
        double demand = this.demand(good);
        if (demand <= 0.0) {
            return supply > 0.0 ? "no_demand" : "unused";
        }
        if (supply <= 0.0) {
            return "no_supply";
        }
        double ratio = supply / demand;
        if (ratio < RATIO_LOW) {
            return "deficit";
        }
        if (ratio > RATIO_HIGH) {
            return "surplus";
        }
        return "ok";
    }

    public void write(File outputDir) throws IOException {
        File file = new File(outputDir, "calibration/goods.csv");
        file.getParentFile().mkdirs();
        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write("good,base_cost,rgo_supply,building_supply,supply,population_demand_target,input_demand,demand,ratio,status");
            writer.newLine();
            for (int g = 0; g < GOOD_COUNT; g++) {
                double demand = this.demand(g);
                double ratio = demand > 0.0 ? this.supply(g) / demand : 0.0;
                writer.write(String.format(Locale.ROOT, "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                    this.goodNames[g], this.baseCosts[g], this.rgoSupply[g], this.buildingSupply[g], this.supply(g),
                    this.populationDemand[g], this.inputDemand[g], demand, ratio, this.status(g)));
                writer.newLine();
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
            if (this.demand(g) <= 0.0 && this.supply(g) > 0.0) {
                count++;
            }
        }
        return count;
    }
}
