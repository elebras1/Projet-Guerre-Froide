package com.populaire.projetguerrefroide.loader;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.json.*;
import com.populaire.projetguerrefroide.pojo.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.IncomeTypeUtils;
import com.populaire.projetguerrefroide.util.StrataUtils;
import io.github.elebras1.flecs.Entity;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import java.util.Arrays;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class EconomyLoader {
    private final WorldLoadingContext context;
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public EconomyLoader(WorldLoadingContext context) {
        this.context = context;
        this.ecsWorld = context.ecsWorld();
        this.ecsConstants = context.ecsConstants();
    }

    public void createGlobalMarket() {
        long globalMarketId = this.ecsWorld.entity("global_market");
        this.ecsWorld.obtainEntity(globalMarketId).set(new GlobalMarket(new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new float[GOOD_COUNT], new double[GOOD_COUNT]));
    }

    public void loadGoods(GoodsJson goods) {
        try {
            int goodIndex = 0;

            for(var resourceGoodEntry : goods.resourceGoods().entrySet()) {
                GoodJson resourceGood = resourceGoodEntry.getValue();
                long goodId = this.ecsWorld.entity(resourceGoodEntry.getKey());
                EntityView good = this.ecsWorld.obtainEntityView(goodId);
                good.add(this.ecsConstants.ressourceGoodTag());
                good.set(new Color(this.context.parseColor(resourceGood.color())));
                good.set(new Good((float) resourceGood.cost(), resourceGood.value().floatValue()));
                this.context.goodIds()[goodIndex] = goodId;
                goodIndex++;
            }

            for(var advancedGoodEntry : goods.advancedGoods().entrySet()) {
                GoodJson advancedGood = advancedGoodEntry.getValue();
                long goodId = this.ecsWorld.entity(advancedGoodEntry.getKey());
                EntityView good = this.ecsWorld.obtainEntityView(goodId);
                good.add(this.ecsConstants.advancedGoodTag());
                good.set(new Color(this.context.parseColor(advancedGood.color())));
                good.set(new Good((float) advancedGood.cost(), 1));
                this.context.goodIds()[goodIndex] = goodId;
                goodIndex++;
            }

            int serviceGoodStartIndex = goodIndex;
            for(var serviceGoodEntry : goods.serviceGoods().entrySet()) {
                GoodJson serviceGood = serviceGoodEntry.getValue();
                long goodId = this.ecsWorld.entity(serviceGoodEntry.getKey());
                EntityView good = this.ecsWorld.obtainEntityView(goodId);
                good.add(this.ecsConstants.serviceGoodTag());
                good.set(new Color(this.context.parseColor(serviceGood.color())));
                good.set(new Good((float) serviceGood.cost(), 1));
                this.context.goodIds()[goodIndex] = goodId;
                goodIndex++;
            }
            int serviceGoodEndIndex = goodIndex;

            for(var militaryGoodEntry : goods.militaryGoods().entrySet()) {
                GoodJson militaryGood = militaryGoodEntry.getValue();
                long goodId = this.ecsWorld.entity(militaryGoodEntry.getKey());
                EntityView good = this.ecsWorld.obtainEntityView(goodId);
                good.add(this.ecsConstants.militaryGoodTag());
                good.set(new Color(this.context.parseColor(militaryGood.color())));
                good.set(new Good((float) militaryGood.cost(), 1));
                this.context.goodIds()[goodIndex] = goodId;
                goodIndex++;
            }

            Entity globalGood = this.ecsWorld.obtainEntity(this.ecsWorld.entity("global_good"));
            globalGood.set(new GlobalGood(this.context.goodIds(), serviceGoodStartIndex, serviceGoodEndIndex));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadPopulationTypes(Map<String, PopulationTypeJson> populationTypes) {
        try {
            int index = 0;
            int capitalistPopTypeIndex = -1;
            int aristocratPopTypeIndex = -1;
            for (Map.Entry<String, PopulationTypeJson> populationType : populationTypes.entrySet()) {
                this.loadPopulationType(populationType.getValue(), populationType.getKey());
                long populationTypeId = this.ecsWorld.lookup(populationType.getKey());
                this.context.populationTypeIds()[index] = populationTypeId;

                EntityView populationTypeEntity = this.ecsWorld.obtainEntityView(populationTypeId);
                if (populationTypeEntity.has(this.ecsConstants.capitalistTag())) {
                    capitalistPopTypeIndex = index;
                } else if (populationTypeEntity.has(this.ecsConstants.aristocratTag())) {
                    aristocratPopTypeIndex = index;
                }
                index++;
            }
            long globalPopType = this.ecsWorld.entity("global_population_type");
            this.ecsWorld.obtainEntity(globalPopType).set(new GlobalPopulationType(this.context.populationTypeIds(), capitalistPopTypeIndex, aristocratPopTypeIndex));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadPopulationType(PopulationTypeJson populationTypeJson, String name) {
        try {
            int color = this.context.parseColor(populationTypeJson.color());
            long populationTypeId = this.ecsWorld.entity(name);
            EntityView populationType = this.ecsWorld.obtainEntityView(populationTypeId);
            populationType.set(new Color(color));
            switch (name) {
                case "aristocrats" -> populationType.add(this.ecsConstants.aristocratTag());
                case "capitalists" -> populationType.add(this.ecsConstants.capitalistTag());
            }

            int[] lifeNeedsGoodIndexes = new int[MAX_LIFE_NEEDS_GOODS];
            Arrays.fill(lifeNeedsGoodIndexes, -1);
            long[] lifeNeedsGoodIds = new long[MAX_LIFE_NEEDS_GOODS];
            float[] lifeNeedsGoodAmounts = new float[MAX_LIFE_NEEDS_GOODS];
            this.context.fillGoods(populationTypeJson.lifeNeeds(), lifeNeedsGoodIndexes, lifeNeedsGoodIds, lifeNeedsGoodAmounts);

            int[] everydayNeedsGoodIndexes = new int[MAX_EVERYDAY_NEEDS_GOODS];
            Arrays.fill(everydayNeedsGoodIndexes, -1);
            long[] everydayNeedsGoodIds = new long[MAX_EVERYDAY_NEEDS_GOODS];
            float[] everydayNeedsGoodAmounts = new float[MAX_EVERYDAY_NEEDS_GOODS];
            this.context.fillGoods(populationTypeJson.everydayNeeds(), everydayNeedsGoodIndexes, everydayNeedsGoodIds, everydayNeedsGoodAmounts);

            int[] luxuryNeedsGoodIndexes = new int[MAX_LUXURY_DEMAND_GOODS];
            Arrays.fill(luxuryNeedsGoodIndexes, -1);
            long[] luxuryNeedsGoodIds = new long[MAX_LUXURY_DEMAND_GOODS];
            float[] luxuryNeedsGoodAmounts = new float[MAX_LUXURY_DEMAND_GOODS];
            this.context.fillGoods(populationTypeJson.luxuryNeeds(), luxuryNeedsGoodIndexes, luxuryNeedsGoodIds, luxuryNeedsGoodAmounts);

            int strata = StrataUtils.getStrata(populationTypeJson.strata());
            int incomeType = IncomeTypeUtils.getIncomeType(populationTypeJson.incomeType());
            populationType.set(new PopulationType(lifeNeedsGoodIndexes, lifeNeedsGoodIds, lifeNeedsGoodAmounts, everydayNeedsGoodIndexes, everydayNeedsGoodIds, everydayNeedsGoodAmounts, luxuryNeedsGoodIndexes, luxuryNeedsGoodIds, luxuryNeedsGoodAmounts, strata, incomeType));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Map<String, ProductionType> loadProductionTypes(ProductionTypesJson productionTypesJson) {
        try {
            Map<String, ProductionType> productionTypes = new ObjectObjectMap<>(16, 1f);

            ObjectLongMap<String> workerPopTypeIdsByType = new ObjectLongMap<>(16, 1f);
            ObjectFloatMap<String> workerPopTypeRatiosByType = new ObjectFloatMap<>(16, 1f);
            ObjectFloatMap<String> workerPopTypeEffectMultiplierByType = new ObjectFloatMap<>(16, 1f);
            for(var typeWorkerEntry : productionTypesJson.typesWorkers().entrySet()) {
                String workerTypeName = typeWorkerEntry.getKey();
                WorkerTypeJson typeWorker = typeWorkerEntry.getValue();
                workerPopTypeIdsByType.put(workerTypeName, this.ecsWorld.lookup(typeWorker.poptype()));
                workerPopTypeRatiosByType.put(workerTypeName, (float) typeWorker.ratio());
                workerPopTypeEffectMultiplierByType.put(workerTypeName, (float) typeWorker.effectMultiplier());
            }

            for(var typeBuildingEntry : productionTypesJson.typesBuildings().entrySet()) {
                productionTypes.put(typeBuildingEntry.getKey(), this.createProductionType(typeBuildingEntry.getValue(), workerPopTypeIdsByType, workerPopTypeRatiosByType, workerPopTypeEffectMultiplierByType));
            }

            for(var typeRgoEntry : productionTypesJson.typesRgo().entrySet()) {
                productionTypes.put(typeRgoEntry.getKey(), this.createProductionType(typeRgoEntry.getValue(), workerPopTypeIdsByType, workerPopTypeRatiosByType, workerPopTypeEffectMultiplierByType));
            }

            return productionTypes;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private ProductionType createProductionType(ProductionTypeJson productionTypeJson, ObjectLongMap<String> workerPopTypeIdsByType, ObjectFloatMap<String> workerPopTypeRatiosByType, ObjectFloatMap<String> workerPopTypeEffectMultiplierByType) {
        long[] workerPopTypeIds = new long[MAX_POPS];
        int[] workerPopTypeIndexes = new int[MAX_POPS];
        float[] workerPopTypeRatios = new float[MAX_POPS];
        float[] workerPopTypeEffectMultipliers = new float[MAX_POPS];

        int i = 0;
        for(String workerTypeName : productionTypeJson.workers()) {
            long popTypeId = workerPopTypeIdsByType.get(workerTypeName);
            workerPopTypeIds[i] = popTypeId;
            workerPopTypeIndexes[i] = this.context.getPopulationTypeIndex(popTypeId);
            workerPopTypeRatios[i] = workerPopTypeRatiosByType.get(workerTypeName);
            workerPopTypeEffectMultipliers[i] = workerPopTypeEffectMultiplierByType.get(workerTypeName);
            i++;
        }

        return new ProductionType((int) productionTypeJson.workforce(), workerPopTypeIndexes, workerPopTypeIds, workerPopTypeRatios, workerPopTypeEffectMultipliers);
    }

    public void loadBuildings(BuildingsJson buildings, Map<String, ProductionType> productionTypes) {
        try {
            for(var economyBuildingEntry : buildings.economyBuilding().entrySet()) {
                EntityView building = this.ecsWorld.obtainEntityView(this.ecsWorld.entity(economyBuildingEntry.getKey()));
                BuildingJson buildingJson = economyBuildingEntry.getValue();
                int time = (int) buildingJson.time();
                byte maxLevel = buildingJson.maxLevel().byteValue();
                int[] goodCostIndexes = new int[MAX_GOODS];
                Arrays.fill(goodCostIndexes, -1);
                long[] goodCostIds = new long[MAX_GOODS];
                float[] goodCostAmounts = new float[MAX_GOODS];
                this.context.fillGoods(buildingJson.goodsCost(), goodCostIndexes, goodCostIds, goodCostAmounts);
                int[] inputGoodIndexes = new int[MAX_GOODS];
                Arrays.fill(inputGoodIndexes, -1);
                long[] inputGoodIds = new long[MAX_GOODS];
                float[] inputGoodAmounts = new float[MAX_GOODS];
                this.context.fillGoods(buildingJson.inputGoods(), inputGoodIndexes, inputGoodIds, inputGoodAmounts);
                int[] advancedGoodInputIndexes = new int[MAX_GOODS];
                Arrays.fill(advancedGoodInputIndexes, -1);
                long[] advancedGoodInputIds = new long[MAX_GOODS];
                float[] advancedGoodInputAmounts = new float[MAX_GOODS];
                if(buildingJson.advancedInputGoods() != null) {
                    this.context.fillGoods(buildingJson.advancedInputGoods(), advancedGoodInputIndexes, advancedGoodInputIds, advancedGoodInputAmounts);
                }
                int outputGoodIndex = 0;
                long outputGoodId = 0;
                float outputGoodAmount = 0;
                var outputGoodsIterator = buildingJson.outputGoods().entrySet().iterator();
                if (outputGoodsIterator.hasNext()) {
                    var outputGood = outputGoodsIterator.next();
                    outputGoodId = this.ecsWorld.lookup(outputGood.getKey());
                    outputGoodIndex = this.context.getGoodIndex(outputGoodId);
                    outputGoodAmount = outputGood.getValue().floatValue();
                }
                ProductionType productionType = productionTypes.get(buildingJson.baseType());
                building.set(new EconomyBuildingType(
                    time,
                    maxLevel,
                    goodCostIndexes,
                    goodCostIds,
                    goodCostAmounts,
                    inputGoodIndexes,
                    inputGoodIds,
                    inputGoodAmounts,
                    advancedGoodInputIndexes,
                    advancedGoodInputIds,
                    advancedGoodInputAmounts,
                    outputGoodIndex,
                    outputGoodId,
                    outputGoodAmount,
                    productionType.workforce(),
                    productionType.workerPopTypeIndexes()[0],
                    productionType.workerPopTypeIds()[0],
                    productionType.workerPopTypeRatios()[0],
                    productionType.workerPopTypeEffectMultipliers()[0],
                    productionType.workerPopTypeIndexes()[1],
                    productionType.workerPopTypeIds()[1],
                    productionType.workerPopTypeRatios()[1],
                    productionType.workerPopTypeEffectMultipliers()[1]
                ));
            }

            for(var specialBuildingEntry : buildings.specialBuilding().entrySet()) {
                EntityView building = this.ecsWorld.obtainEntityView(this.ecsWorld.entity(specialBuildingEntry.getKey()));
                BuildingJson buildingJson = specialBuildingEntry.getValue();
                long[] goodCostIds = new long[MAX_GOODS];
                float[] goodCostAmounts = new float[MAX_GOODS];
                this.context.fillGoodCosts(buildingJson.goodsCost(), goodCostIds, goodCostAmounts);
                if (buildingJson.modifier() != null) {
                    building.set(this.createBuildingModifiers(buildingJson.modifier()));
                }
                building.set(new SpecialBuildingType((int) buildingJson.time(), buildingJson.cost().intValue(), goodCostIds, goodCostAmounts));
            }

            for(var developmentBuildingEntry : buildings.developmentBuilding().entrySet()) {
                EntityView building = this.ecsWorld.obtainEntityView(this.ecsWorld.entity(developmentBuildingEntry.getKey()));
                BuildingJson buildingJson = developmentBuildingEntry.getValue();
                long[] goodCostIds = new long[MAX_GOODS];
                float[] goodCostAmounts = new float[MAX_GOODS];
                this.context.fillGoodCosts(buildingJson.goodsCost(), goodCostIds, goodCostAmounts);
                if(buildingJson.onmap()) {
                    building.add(this.ecsConstants.onMap());
                }
                if (buildingJson.modifier() != null) {
                    building.set(this.createBuildingModifiers(buildingJson.modifier()));
                }
                building.set(new DevelopmentBuildingType((int) buildingJson.time(), buildingJson.cost().intValue(), goodCostIds, goodCostAmounts, buildingJson.maxLevel().intValue()));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Modifiers createBuildingModifiers(Map<String, Double> modifiers) {
        long[] modifierIds = new long[MAX_MODIFIERS];
        float[] modifierValues = new float[MAX_MODIFIERS];
        int i = 0;
        for(var modifierEntry : modifiers.entrySet()) {
            modifierIds[i] = this.ecsWorld.entity(modifierEntry.getKey());
            modifierValues[i] = modifierEntry.getValue().floatValue();
            i++;
        }
        return new Modifiers(modifierValues, modifierIds);
    }

    public void loadResourceProductions(Map<String, ResourceProductionJson> resourceProductions, Map<String, ProductionType> productionTypes) {
        try {
            for(var resourceProductionEntry : resourceProductions.entrySet()) {
                String goodNameId = resourceProductionEntry.getKey();
                ProductionType productionType = productionTypes.get(resourceProductionEntry.getValue().baseType());
                long rgoTypeId = this.ecsWorld.entity("rgo_" + goodNameId);
                EntityView rgoType = this.ecsWorld.obtainEntityView(rgoTypeId);
                rgoType.set(new ResourceGatheringType(
                    productionType.workforce(),
                    productionType.workerPopTypeIndexes()[0],
                    productionType.workerPopTypeIds()[0],
                    productionType.workerPopTypeRatios()[0],
                    productionType.workerPopTypeEffectMultipliers()[0],
                    productionType.workerPopTypeIndexes()[1],
                    productionType.workerPopTypeIds()[1],
                    productionType.workerPopTypeRatios()[1],
                    productionType.workerPopTypeEffectMultipliers()[1]
                ));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
