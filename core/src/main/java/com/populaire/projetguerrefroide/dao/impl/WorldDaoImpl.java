package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.Demographics;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.pojo.*;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.ForceTypeUtils;
import com.populaire.projetguerrefroide.util.StrataUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WorldDaoImpl implements WorldDao {
    private final String commonPath = "common/";
    private final String mapPath = "map/";
    private final String historyPath = "history/";
    private final String diplomacyPath = historyPath + "diplomacy/";
    private final String countriesJsonFiles = this.commonPath + "countries.json";
    private final String countriesHistoryJsonFiles = this.historyPath + "countries.json";
    private final String leadersJsonFiles = this.historyPath + "leaders.json";
    private final String regionJsonFiles = this.mapPath + "region.json";
    private final String provincesJsonFile = this.historyPath + "provinces.json";
    private final String definitionCsvFile = this.mapPath + "definition.csv";
    private final String continentJsonFile = this.mapPath + "continent.json";
    private final String positionsJsonFile = this.mapPath + "positions.json";
    private final String adjacenciesJsonFile = this.mapPath + "adjacencies.json";
    private final String terrainJsonFile = this.mapPath + "terrain.json";
    private final String governmentJsonFile = this.commonPath + "governments.json";
    private final String ideologiesJsonFile = this.commonPath + "ideologies.json";
    private final String nationalIdeasJsonFile = this.commonPath + "national_ideas.json";
    private final String goodsJsonFile = this.commonPath + "goods.json";
    private final String populationTemplatesJsonFile = this.commonPath + "population_templates.json";
    private final String ministerTypesJsonFile = this.commonPath + "minister_types.json";
    private final String productionTypesJsonFile = this.commonPath + "production_types.json";
    private final String buildingsJsonFile = this.commonPath + "buildings.json";
    private final String resourceProductionsJsonFile = this.commonPath + "resource_productions.json";
    private final String populationTypesJsonFile = this.commonPath + "poptypes.json";
    private final String lawsJsonFile = this.commonPath + "laws.json";
    private final String relationJsonFile = this.diplomacyPath + "relation.json";
    private final String alliancesJsonFile = this.diplomacyPath + "alliances.json";
    private final String traitsJsonFile = this.commonPath + "traits.json";
    private final JsonMapper mapper = new JsonMapper();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final long[] populationTypeIds = new long[12];
    private final long[] goodIds = new long[40];

    public WorldDaoImpl() {

    }

    @Override
    public WorldData createWorld(GameContext gameContext) {
        World ecsWorld = gameContext.getEcsWorld();
        EcsConstants ecsConstants = gameContext.getEcsConstants();
        this.readIdeologies(ecsWorld);
        this.readLaws(ecsWorld, ecsConstants);
        this.readGovernments(ecsWorld);
        this.readNationalIdeas(ecsWorld);
        this.readMinisterTypes(ecsWorld);
        this.readGoods(ecsWorld, ecsConstants);
        this.readPopulationTypes(ecsWorld);
        Map<String, ProductionType> productionTypes = this.readProductionTypes(ecsWorld);
        this.readBuildings(ecsWorld, ecsConstants, productionTypes);
        this.readResourceProductions(ecsWorld, productionTypes);
        this.readTraits(ecsWorld);
        this.loadCountries(ecsWorld, ecsConstants);
        this.readTerrains(ecsWorld);
        IntLongMap provinces = new IntLongMap(15000, 1f);
        Borders borders = this.loadProvinces(ecsWorld, ecsConstants, provinces);

        return new WorldData(provinces, borders, this.goodIds);
    }

    private JsonValue parseJsonFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return this.mapper.parse(fileHandle.read(), (int) fileHandle.length());
    }

    private BufferedReader parseCsvFile(String filePath) {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return new BufferedReader(new StringReader(fileHandle.readString()));
    }

    private void readNationalIdeas(World ecsWorld) {
        try {
            JsonValue nationalIdeasValues = this.parseJsonFile(this.nationalIdeasJsonFile);

            for(var cultureEntry : nationalIdeasValues.get("cultures").object()) {
                String name = cultureEntry.getKey();
                JsonValue cultureValue = cultureEntry.getValue();
                int color = this.parseColor(cultureValue.get("color"));
                long cultureEntityId = ecsWorld.entity(name);
                EntityView cultureEntity = ecsWorld.obtainEntityView(cultureEntityId);
                cultureEntity.set(new Color(color));
            }

            for(var religionEntry : nationalIdeasValues.get("religions").object()) {
                String name = religionEntry.getKey();
                JsonValue religionValue = religionEntry.getValue();
                int color = this.parseColor(religionValue.get("color"));
                long religionEntityId = ecsWorld.entity(name);
                EntityView religionEntity = ecsWorld.obtainEntityView(religionEntityId);
                religionEntity.set(new Color(color));
            }

            for(var identityEntry : nationalIdeasValues.get("national_identity").object()) {
                String name = identityEntry.getKey();
                long identityEntityId = ecsWorld.entity(name);
                EntityView identityEntity = ecsWorld.obtainEntityView(identityEntityId);
                JsonValue identityValue = identityEntry.getValue();
                float[] modifierValues = new float[8];
                long[] modifierTagIds = new long[8];
                int i = 0;
                for(var modifierEntry : identityValue.object()) {
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    modifierValues[i] = (float) modifierValue.asDouble();
                    modifierTagIds[i] = ecsWorld.entity(modifierName);
                    i++;
                }
                identityEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }

            for(var attitudeEntry : nationalIdeasValues.get("national_attitude").object()) {
                String attitudeName = attitudeEntry.getKey();
                long attitudeEntityId = ecsWorld.entity(attitudeName);
                EntityView attitudeEntity = ecsWorld.obtainEntityView(attitudeEntityId);
                JsonValue attitudeValue = attitudeEntry.getValue();
                float[] modifierValues = new float[8];
                long[] modifierTagIds = new long[8];
                int i = 0;
                for(var modifierEntry : attitudeValue.object()) {
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    modifierValues[i] = (float) modifierValue.asDouble();
                    modifierTagIds[i] = ecsWorld.entity(modifierName);
                    i++;
                }
                attitudeEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readGovernments(World ecsWorld) {
        try {
            JsonValue governmentsValues = this.parseJsonFile(this.governmentJsonFile);

            for(var governmentEntry : governmentsValues.object()) {
                String governmentName = governmentEntry.getKey();
                JsonValue governmentValue = governmentEntry.getValue();

                long governmentEntityId = ecsWorld.entity(governmentName);
                EntityView governmentEntity = ecsWorld.obtainEntityView(governmentEntityId);

                long[] associatedIdeologies = new long[4];
                int i = 0;
                for(var associatedIdeologyValue : governmentValue.get("associated_ideologies").array()) {
                    String ideologyName = associatedIdeologyValue.asString();
                    long ideologyEntityId = ecsWorld.lookup(ideologyName);
                    associatedIdeologies[i] = ideologyEntityId;
                    i++;
                }

                JsonValue electionValue = governmentValue.get("election");
                if(!electionValue.isNull() && electionValue.getSize() > 0) {
                    boolean headOfState = electionValue.get("head_of_state").asBoolean();
                    boolean headOfGovernment = electionValue.get("head_of_government").asBoolean();
                    int duration = (int) electionValue.get("duration").asLong();
                    governmentEntity.set(new ElectoralMechanism(headOfState, headOfGovernment, duration));
                }

                long[] supportedLaws = new long[32];
                int j = 0;
                for(var governmentElementEntry : governmentValue.object()) {
                    long lawGroupId = ecsWorld.lookup(governmentElementEntry.getKey());
                    if(lawGroupId != 0) {
                        for(var supportedLawValue : governmentElementEntry.getValue().array()) {
                            String lawNameId = supportedLawValue.asString();
                            long lawId = ecsWorld.lookup(lawNameId);
                            supportedLaws[j] = lawGroupId;
                            supportedLaws[j + 1] = lawId;
                            j += 2;
                        }
                    }
                }

                governmentEntity.set(new GovernmentPolicy(associatedIdeologies, supportedLaws));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readIdeologies(World ecsWorld) {
        try {
            JsonValue ideologiesValues = this.parseJsonFile(this.ideologiesJsonFile);
            for(var ideologyEntry : ideologiesValues.object()) {
                String ideologyName = ideologyEntry.getKey();
                JsonValue ideologyValue = ideologyEntry.getValue();
                int color = this.parseColor(ideologyValue.get("color"));
                byte factionDriftingSpeed = (byte) ideologyValue.get("faction_drifting_speed").asLong();

                long ideologyEntityId = ecsWorld.entity(ideologyName);
                EntityView ideologyEntity = ecsWorld.obtainEntityView(ideologyEntityId);
                ideologyEntity.set(new Ideology(color, factionDriftingSpeed));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readGoods(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue goodsValues = this.parseJsonFile(this.goodsJsonFile);
            int goodIndex = 0;

            for(var resourceGoodEntry : goodsValues.get("resource_goods").object()) {
                String goodName = resourceGoodEntry.getKey();
                JsonValue goodValue = resourceGoodEntry.getValue();
                float cost = (float) goodValue.get("cost").asDouble();
                float value = (float) goodValue.get("value").asDouble();
                int color = this.parseColor(goodValue.get("color"));

                long goodId = ecsWorld.entity(goodName);
                EntityView good = ecsWorld.obtainEntityView(goodId);
                good.add(ecsConstants.ressourceGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, value));
                this.goodIds[goodIndex] = goodId;
                goodIndex++;
            }

            for(var advancedGoodEntry : goodsValues.get("advanced_goods").object()) {
                String advancedGoodName = advancedGoodEntry.getKey();
                JsonValue advancedGoodValue = advancedGoodEntry.getValue();
                float cost = (float) advancedGoodValue.get("cost").asDouble();
                int color = this.parseColor(advancedGoodValue.get("color"));

                long goodId = ecsWorld.entity(advancedGoodName);
                EntityView good = ecsWorld.obtainEntityView(goodId);
                good.add(ecsConstants.advancedGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, -0));
                this.goodIds[goodIndex] = goodId;
                goodIndex++;
            }

            for(var militaryGood : goodsValues.get("military_goods").object()) {
                String militaryGoodName = militaryGood.getKey();
                JsonValue militaryGoodNode = militaryGood.getValue();
                float cost = (float) militaryGoodNode.get("cost").asDouble();
                int color = this.parseColor(militaryGoodNode.get("color"));

                long goodId = ecsWorld.entity(militaryGoodName);
                EntityView good = ecsWorld.obtainEntityView(goodId);
                good.add(ecsConstants.militaryGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, 0));
                this.goodIds[goodIndex] = goodId;
                goodIndex++;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readPopulationTypes(World ecsWorld) {
        Map<String, String> populationPaths = new ObjectObjectMap<>(12, 1f);
        try {
            JsonValue populationTypesValues = this.parseJsonFile(this.populationTypesJsonFile);
            for(var populationTypeEntry : populationTypesValues.object()) {
                populationPaths.put(populationTypeEntry.getKey(), this.commonPath + populationTypeEntry.getValue().asString());
            }

            int index = 0;
            for (Map.Entry<String, String> populationPath : populationPaths.entrySet()) {
                this.readPopulationType(ecsWorld, populationPath.getValue(), populationPath.getKey());
                this.populationTypeIds[index++] = ecsWorld.lookup(populationPath.getKey());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readPopulationType(World ecsWorld, String populationTypePath, String name) {
        try {
            JsonValue populationTypeValue = this.parseJsonFile(populationTypePath);
            int color = this.parseColor(populationTypeValue.get("color"));
            long populationTypeId = ecsWorld.entity(name);
            EntityView populationType = ecsWorld.obtainEntityView(populationTypeId);
            populationType.set(new Color(color));

            long[] standardDemandGoodIds = new long[16];
            float[] standardDemandGoodValues = new float[16];
            int standardDemandIndex = 0;
            for(var standardDemandEntry : populationTypeValue.get("standard_demands").object()) {
                long goodId = ecsWorld.lookup(standardDemandEntry.getKey());
                float value = (float) standardDemandEntry.getValue().asDouble();
                standardDemandGoodIds[standardDemandIndex] = goodId;
                standardDemandGoodValues[standardDemandIndex] = value;
                standardDemandIndex++;
            }

            long[] luxuryDemandGoodIds = new long[8];
            float[] luxuryDemandGoodValues = new float[8];
            int luxuryDemandIndex = 0;
            for(var luxuryDemandEntry : populationTypeValue.get("luxury_demands").object()) {
                long goodId = ecsWorld.lookup(luxuryDemandEntry.getKey());
                float value = (float) luxuryDemandEntry.getValue().asDouble();
                luxuryDemandGoodIds[luxuryDemandIndex] = goodId;
                luxuryDemandGoodValues[luxuryDemandIndex] = value;
                luxuryDemandIndex++;
            }
            int strata = StrataUtils.getStrata(populationTypeValue.get("strata").asString());
            populationType.set(new PopulationType(standardDemandGoodIds, standardDemandGoodValues, luxuryDemandGoodIds, luxuryDemandGoodValues, strata));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Map<String, ProductionType> readProductionTypes(World ecsWorld) {
        try {
            Map<String, ProductionType> productionTypes = new ObjectObjectMap<>(16, 1f);

            JsonValue buildingTypesJson = this.parseJsonFile(this.productionTypesJsonFile);
            ObjectLongMap<String> workerPopTypeIdsByType = new ObjectLongMap<>(16, 1f);
            ObjectFloatMap<String> workerPopTypeRatiosByType = new ObjectFloatMap<>(16, 1f);
            ObjectFloatMap<String> workerPopTypeEffectMultiplierByType = new ObjectFloatMap<>(16, 1f);
            for(var typeWorkerEntry : buildingTypesJson.get("types_workers").object()) {
                String workerTypeName = typeWorkerEntry.getKey();
                JsonValue typeWorkerValue = typeWorkerEntry.getValue();
                long popTypeId = ecsWorld.lookup(typeWorkerValue.get("poptype").asString());
                float ratio = (float) typeWorkerValue.get("ratio").asDouble();
                float effectMultiplier = (float) typeWorkerValue.get("effect_multiplier").asDouble();
                workerPopTypeIdsByType.put(workerTypeName, popTypeId);
                workerPopTypeRatiosByType.put(workerTypeName, ratio);
                workerPopTypeEffectMultiplierByType.put(workerTypeName, effectMultiplier);
            }

            for(var typeBuildingEntry : buildingTypesJson.get("types_buildings").object()) {
                String typeName = typeBuildingEntry.getKey();
                JsonValue typeBuildingValue = typeBuildingEntry.getValue();
                int workforce = (int) typeBuildingValue.get("workforce").asLong();
                long ownerId = ecsWorld.lookup(typeBuildingValue.get("owner").get("poptype").asString());

                long[] workerPopTypeIds = new long[4];
                float[] workerPopTypeRatios = new float[4];
                float[] workerPopTypeEffectMultipliers = new float[4];

                int i = 0;
                for(var workerValue : typeBuildingValue.get("workers").array()) {
                    String workerTypeName = workerValue.asString();
                    workerPopTypeIds[i] = workerPopTypeIdsByType.get(workerTypeName);
                    workerPopTypeRatios[i] = workerPopTypeRatiosByType.get(workerTypeName);
                    workerPopTypeEffectMultipliers[i] = workerPopTypeEffectMultiplierByType.get(workerTypeName);
                    i++;
                }

                productionTypes.put(typeName, new ProductionType(workforce, ownerId, workerPopTypeIds, workerPopTypeRatios, workerPopTypeEffectMultipliers));
            }

            for(var typeRgoEntry: buildingTypesJson.get("types_rgo").object()) {
                String typeName = typeRgoEntry.getKey();
                JsonValue typeRgoValue = typeRgoEntry.getValue();
                int workforce = (int) typeRgoValue.get("workforce").asLong();
                long ownerId = ecsWorld.lookup(typeRgoValue.get("owner").get("poptype").asString());

                long[] workerPopTypeIds = new long[4];
                float[] workerPopTypeRatios = new float[4];
                float[] workerPopTypeEffectMultipliers = new float[4];

                int i = 0;
                for(var workerValue : typeRgoValue.get("workers").array()) {
                    String workerTypeName = workerValue.asString();
                    workerPopTypeIds[i] = workerPopTypeIdsByType.get(workerTypeName);
                    workerPopTypeRatios[i] = workerPopTypeRatiosByType.get(workerTypeName);
                    workerPopTypeEffectMultipliers[i] = workerPopTypeEffectMultiplierByType.get(workerTypeName);
                    i++;
                }

                productionTypes.put(typeName, new ProductionType(workforce, ownerId, workerPopTypeIds, workerPopTypeRatios, workerPopTypeEffectMultipliers));
            }

            return productionTypes;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readBuildings(World ecsWorld, EcsConstants ecsConstants, Map<String, ProductionType> productionTypes) {
        try {
            JsonValue buildingsValues = this.parseJsonFile(this.buildingsJsonFile);
            for(var economyBuilding : buildingsValues.get("economy_building").object()) {
                String buildingName = economyBuilding.getKey();
                EntityView building = ecsWorld.obtainEntityView(ecsWorld.entity(buildingName));
                JsonValue buildingValue = economyBuilding.getValue();
                int time = (int) buildingValue.get("time").asLong();
                String productionTypeId = buildingValue.get("base_type").asString();
                if(buildingValue.get("artisans_type") != null) {
                    // TODO
                }
                byte maxLevel = (byte) buildingValue.get("max_level").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostAmounts = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodAmount = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostAmounts[goodCostIndex] = goodAmount;
                    goodCostIndex++;
                }
                long[] inputGoodIds = new long[8];
                float[] inputGoodAmounts = new float[8];
                int inputGoodIndex = 0;
                for(var inputGoodEntry : buildingValue.get("input_goods").object()) {
                    long goodId = ecsWorld.lookup(inputGoodEntry.getKey());
                    float goodAmount = (float) inputGoodEntry.getValue().asDouble();
                    inputGoodIds[inputGoodIndex] = goodId;
                    inputGoodAmounts[inputGoodIndex] = goodAmount;
                    inputGoodIndex++;
                }
                long outputGoodId = 0;
                float outputGoodAmount = 0;
                Iterator<Map.Entry<String, JsonValue>> outputGoodsEntryIterator = buildingValue.get("output_goods").objectIterator();
                if (outputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> outputGood = outputGoodsEntryIterator.next();
                    long goodId = ecsWorld.lookup(outputGood.getKey());
                    float goodAmount = (float) outputGood.getValue().asDouble();
                    outputGoodId = goodId;
                    outputGoodAmount = goodAmount;
                }
                ProductionType productionType = productionTypes.get(productionTypeId);
                building.set(new EconomyBuildingType(time, maxLevel, goodCostIds, goodCostAmounts, inputGoodIds, inputGoodAmounts, outputGoodId, outputGoodAmount, productionType.workforce(), productionType.ownerId(), productionType.workerPopTypeIds(), productionType.workerPopTypeRatios(), productionType.workerPopTypeEffectMultipliers()));
            }

            for(var specialBuildingEntry : buildingsValues.get("special_building").object()) {
                String buildingName = specialBuildingEntry.getKey();
                EntityView building = ecsWorld.obtainEntityView(ecsWorld.entity(buildingName));
                JsonValue buildingValue = specialBuildingEntry.getValue();
                int time = (int) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostAmounts = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodAmount = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostAmounts[goodCostIndex] = goodAmount;
                    goodCostIndex++;
                }

                JsonValue modifiersValues = buildingValue.get("modifier");
                if (modifiersValues != null) {
                    long[] modifierIds = new long[8];
                    float[] modifierValues = new float[8];
                    int i = 0;
                    for(var modifierEntry: modifiersValues.object()) {
                        String modifierName = modifierEntry.getKey();
                        JsonValue modifierValue = modifierEntry.getValue();
                        long modifierId = ecsWorld.entity(modifierName);
                        float value = (float) modifierValue.asDouble();
                        modifierIds[i] = modifierId;
                        modifierValues[i] = value;
                        i++;
                    }
                    building.set(new Modifiers(modifierValues, modifierIds));
                }
                building.set(new SpecialBuildingType(time, cost, goodCostIds, goodCostAmounts));
            }

            for(var developmentBuildingEntry : buildingsValues.get("development_building").object()) {
                String buildingName = developmentBuildingEntry.getKey();
                EntityView building = ecsWorld.obtainEntityView(ecsWorld.entity(buildingName));
                JsonValue buildingValue = developmentBuildingEntry.getValue();
                int time = (int) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostAmounts = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodAmount = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostAmounts[goodCostIndex] = goodAmount;
                    goodCostIndex++;
                }
                boolean onMap = buildingValue.get("onmap").asBoolean();
                if(onMap) {
                    building.add(ecsConstants.onMap());
                }
                int maxLevel = (int) buildingValue.get("max_level").asLong();
                JsonValue modifiersValues = buildingValue.get("modifier");
                if (modifiersValues != null) {
                    long[] modifierIds = new long[8];
                    float[] modifierValues = new float[8];
                    int i = 0;
                    for(var modifierEntry : modifiersValues.object()) {
                        String modifierName = modifierEntry.getKey();
                        JsonValue modifierValue = modifierEntry.getValue();
                        long modifierId = ecsWorld.entity(modifierName);
                        float value = (float) modifierValue.asDouble();
                        modifierIds[i] = modifierId;
                        modifierValues[i] = value;
                        i++;
                    }
                    building.set(new Modifiers(modifierValues, modifierIds));
                }
                building.set(new DevelopmentBuildingType(time, cost, goodCostIds, goodCostAmounts, maxLevel));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readResourceProductions(World ecsWorld, Map<String, ProductionType> productionTypes) {
        try {
            JsonValue resourceProductionsValues = this.parseJsonFile(this.resourceProductionsJsonFile);
            for(var resourceProductionEntry : resourceProductionsValues.object()) {
                String goodNameId = resourceProductionEntry.getKey();
                String productionTypeId = resourceProductionEntry.getValue().get("base_type").asString();
                ProductionType productionType = productionTypes.get(productionTypeId);
                long rgoTypeId = ecsWorld.entity("rgo_" + goodNameId);
                EntityView rgoType = ecsWorld.obtainEntityView(rgoTypeId);
                rgoType.set(new ResourceGatheringType(productionType.workforce(), productionType.ownerId(), productionType.workerPopTypeIds(), productionType.workerPopTypeRatios(), productionType.workerPopTypeEffectMultipliers()));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readMinisterTypes(World ecsWorld) {
        try {
            JsonValue ministerTypesValues = this.parseJsonFile(this.ministerTypesJsonFile);
            for(var ministerTypeEntry : ministerTypesValues.object()) {
                long ministerTypeEntityId = ecsWorld.entity(ministerTypeEntry.getKey());
                EntityView ministerTypeEntity = ecsWorld.obtainEntityView(ministerTypeEntityId);
                float[] modifierValues = new float[8];
                long[] modifierTagIds = new long[8];
                int i = 0;
                for(var modifierEntry : ministerTypeEntry.getValue().object()) {
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    modifierValues[i] = (float) modifierValue.asDouble();
                    modifierTagIds[i] = ecsWorld.entity(modifierName);
                    i++;
                }
                ministerTypeEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readTerrains(World ecsWorld) {
        try {
            JsonValue terrainsValues = this.parseJsonFile(this.terrainJsonFile);
            for(var terrainEntry : terrainsValues.object()) {
                String terrainName = terrainEntry.getKey();
                JsonValue terrainValue = terrainEntry.getValue();
                byte movementCost = (byte) terrainValue.get("movement_cost").asLong();
                byte humidity = (byte) terrainValue.get("humidity").asLong();
                byte temperature = (byte) terrainValue.get("temperature").asLong();
                byte precipitation = (byte) terrainValue.get("precipitation").asLong();
                int color = this.parseColor(terrainValue.get("color"));

                long entityTerrainId = ecsWorld.entity(terrainName);
                EntityView entityTerrain = ecsWorld.obtainEntityView(entityTerrainId);
                entityTerrain.set(new Terrain(movementCost, temperature, humidity, precipitation, color));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readLaws(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue lawsValues = this.parseJsonFile(this.lawsJsonFile);
            int baseEnactmentDaysLaw = (int) lawsValues.get("base_enactment_days").asLong();
            for(var lawGroupEntry : lawsValues.get("groups").object()) {
                String name = lawGroupEntry.getKey();
                JsonValue lawGroupValue = lawGroupEntry.getValue();
                int factorEnactmentDays = (int) lawGroupValue.get("factor_enactment_days").asLong();
                int enactmentDuration = baseEnactmentDaysLaw * factorEnactmentDays;
                long lawGroupEntityId = ecsWorld.entity(name);
                EntityView lawGroupEntity = ecsWorld.obtainEntityView(lawGroupEntityId);
                lawGroupEntity.add(ecsConstants.lawGroupTag());
                lawGroupEntity.set(new EnactmentDuration(enactmentDuration));
                for(var lawEntry : lawGroupValue.get("laws").object()) {
                    String lawName = lawEntry.getKey();
                    long lawEntityId = ecsWorld.entity(lawName);
                    EntityView lawEntity = ecsWorld.obtainEntityView(lawEntityId);
                    JsonValue lawValue = lawEntry.getValue();
                    // TODO associate requirements with law
                    /*Iterator<JsonValue> requirementsIterator = lawValue.get("requirements").arrayIterator();
                    for(var requirementValue : lawValue.get("requirements").array()) {
                    }*/
                    float[] modifierValues = new float[8];
                    long[] modifierTagIds = new long[8];
                    int i = 0;
                    for(var modifierEntry : lawValue.get("modifiers").object()) {
                        String modifierName = modifierEntry.getKey();
                        JsonValue modifierValue = modifierEntry.getValue();
                        modifierValues[i] = (float) modifierValue.asDouble();
                        modifierTagIds[i] = ecsWorld.entity(modifierName);
                        i++;
                    }
                    lawEntity.set(new Modifiers(modifierValues, modifierTagIds));
                    long[] supportIdeologies = new long[8];
                    long[] opponentIdeologies = new long[8];
                    int indexSupporters = 0;
                    int indexOpponents = 0;
                    for(var interestIdeologyEntry : lawValue.get("interest_ideologies").object()) {
                        long ideologyId = ecsWorld.lookup(interestIdeologyEntry.getKey());
                        int value = (int) interestIdeologyEntry.getValue().asLong();
                        if(value > 0) {
                            supportIdeologies[indexSupporters] = ideologyId;
                            indexSupporters++;
                        } else if (value < 0) {
                            opponentIdeologies[indexOpponents] = ideologyId;
                            indexOpponents++;
                        }
                    }
                    lawEntity.set(new Law(lawGroupEntityId, supportIdeologies, opponentIdeologies));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readTraits(World ecsWorld) {
        try {
            JsonValue traitsValues = this.parseJsonFile(this.traitsJsonFile);
            for(var traitEntry : traitsValues.object()) {
                String traitName = traitEntry.getKey();
                JsonValue traitValue = traitEntry.getValue();
                Map.Entry<String, JsonValue> modifierEntry = traitValue.objectIterator().next();
                String modifierName = modifierEntry.getKey();
                float modifierValue = (float) modifierEntry.getValue().asDouble();
                long modifierTagId = ecsWorld.entity(modifierName);
                long traitEntityId = ecsWorld.entity(traitName);
                EntityView traitEntity = ecsWorld.obtainEntityView(traitEntityId);
                float[] modifierValues = new float[8];
                long[] modifierTagIds = new long[8];
                modifierValues[0] = modifierValue;
                modifierTagIds[0] = modifierTagId;
                traitEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadCountries(World ecsWorld, EcsConstants ecsConstants) {
        this.readCountries(ecsWorld, ecsConstants);
        this.readRelation(ecsWorld);
        this.readAlliances(ecsWorld, ecsConstants);
        this.readLeaders(ecsWorld);
    }

    private void readCountries(World ecsWorld, EcsConstants ecsConstants) {
        Map<String, String> countriesPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesValues = this.parseJsonFile(this.countriesJsonFiles);
            for(var countryEntry: countriesValues.object()) {
                countriesPaths.put(countryEntry.getKey(), this.commonPath + countryEntry.getValue().asString());
            }

            for (Map.Entry<String, String> countryEntry : countriesPaths.entrySet()) {
                this.readCountry(ecsWorld, ecsConstants, countryEntry.getValue(), countryEntry.getKey());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readCountry(World ecsWorld, EcsConstants ecsConstants, String countryPath, String countryId) {
        try {
            JsonValue countryValues = this.parseJsonFile(countryPath);

            int color = this.parseColor(countryValues.get("color"));
            long countryEntityId = ecsWorld.entity(countryId);
            EntityView countryEntity = ecsWorld.obtainEntityView(countryEntityId);
            countryEntity.add(ecsConstants.countryTag());
            countryEntity.set(new Color(color));

            JsonValue ministersValues = countryValues.get("ministers");
            if (ministersValues != null && ministersValues.isObject()) {
                for(var ministerEntry : ministersValues.object()) {
                    String ministerNameId = ministerEntry.getKey();
                    JsonValue ministerNode = ministerEntry.getValue();
                    String name = ministerNode.get("name").asString();
                    String ideology = ministerNode.get("ideology").asString();
                    float loyalty = (float) ministerNode.get("loyalty").asDouble();
                    String imageNameFile = ministerNode.get("picture").asString();
                    String type = ministerNode.get("type").asString();
                    int startDate = (int) LocalDate.parse(ministerNode.get("start_date").asString(), this.dateFormatter).toEpochDay();
                    int deathDate = (int) LocalDate.parse(ministerNode.get("death_date").asString(), this.dateFormatter).toEpochDay();

                    long ideologyEntityId = ecsWorld.entity(ideology);
                    long typeEntityId = ecsWorld.lookup(type);
                    long ministerEntityId = ecsWorld.entity(ministerNameId);
                    EntityView ministerEntity = ecsWorld.obtainEntityView(ministerEntityId);
                    ministerEntity.set(new Minister(name, imageNameFile, loyalty, startDate, deathDate, countryEntityId, ideologyEntityId, typeEntityId));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readRelation(World ecsWorld) {
        try {
            JsonValue relationsValues = this.parseJsonFile(this.relationJsonFile);
            for(var relationValue : relationsValues.get("relation").array()) {
                String countryNameId1 = relationValue.get("country1").asString();
                EntityView country1 = ecsWorld.obtainEntityView(ecsWorld.lookup(countryNameId1));
                String countryNameId2 = relationValue.get("country2").asString();
                EntityView country2 = ecsWorld.obtainEntityView(ecsWorld.lookup(countryNameId2));
                int value = (int) relationValue.get("value").asLong();
                country1.set(new DiplomaticRelation(value), country2.id());
                country2.set(new DiplomaticRelation(value), country1.id());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readAlliances(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue alliancesValues = this.parseJsonFile(this.alliancesJsonFile);
            for(var allianceValue : alliancesValues.get("alliances").array()) {
                String countryNameId1 = allianceValue.get("country1").asString();
                EntityView country1 = ecsWorld.obtainEntityView(ecsWorld.entity(countryNameId1));
                String countryNameId2 = allianceValue.get("country2").asString();
                EntityView country2 = ecsWorld.obtainEntityView(ecsWorld.entity(countryNameId2));
                String type = allianceValue.get("type").asString();
                country1.addRelation(ecsConstants.getAllianceRelation(type, true), country2.id());
                country2.addRelation(ecsConstants.getAllianceRelation(type, false), country1.id());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readLeaders(World ecsWorld) {
        List<String> leadersPaths = new ObjectList<>();
        try {
            JsonValue leadersValues = this.parseJsonFile(this.leadersJsonFiles);
            for(var leaderValue : leadersValues.array()) {
                leadersPaths.add(this.historyPath + leaderValue.asString());
            }

            for (var leaderPath : leadersPaths) {
                this.readLeader(ecsWorld, leaderPath);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readLeader(World ecsWorld, String filename) {
        try {
            JsonValue leaderValues = this.parseJsonFile(filename);
            String countryId = leaderValues.get("country").asString();
            long countryEntityId = ecsWorld.lookup(countryId);
            for(var leaderValue : leaderValues.get("leaders").array()) {
                String name = leaderValue.get("name").asString();
                int skill = (byte) leaderValue.get("skill").asLong();
                int forceType = ForceTypeUtils.getForceType(leaderValue.get("force_type").asString());
                long traitId = ecsWorld.lookup(leaderValue.get("trait").asString());
                long leaderEntityId = ecsWorld.entity();
                EntityView leaderEntity = ecsWorld.obtainEntityView(leaderEntityId);
                leaderEntity.set(new Leader(name, skill, forceType, traitId, countryEntityId));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Borders loadProvinces(World ecsWorld, EcsConstants ecsConstants, IntLongMap provinces) {
        IntObjectMap<LongIntMap> regionBuildingsByProvince = new IntObjectMap<>(396, 1f);
        Map<String, PopulationTemplate> populationTemplates = this.readPopulationTemplates();
        this.readProvinces(ecsWorld, regionBuildingsByProvince, populationTemplates);
        this.readRegion(ecsWorld, ecsConstants, regionBuildingsByProvince);
        this.readDefinition(ecsWorld, provinces);
        Borders border = this.readProvinceBitmap(ecsWorld, provinces);
        this.readCountriesHistory(ecsWorld);
        this.readContinent(ecsWorld);
        this.readAdjacencies(ecsWorld);
        this.readPositions(ecsWorld);
        return border;
    }

    private Map<String, PopulationTemplate> readPopulationTemplates() {
        try {
            Map<String, PopulationTemplate> populationTemplates = new ObjectObjectMap<>();
            JsonValue populationTemplatesValues = this.parseJsonFile(this.populationTemplatesJsonFile);
            for(var populationTemplateEntry : populationTemplatesValues.object()) {
                String populationTemplateId = populationTemplateEntry.getKey();
                Iterator<JsonValue> populationValuesIterator = populationTemplateEntry.getValue().get("value").arrayIterator();
                float childrenRatio = (float) populationValuesIterator.next().asDouble();
                float adultsRatio = (float) populationValuesIterator.next().asDouble();
                float seniorsRatio = (float) populationValuesIterator.next().asDouble();
                populationTemplates.put(populationTemplateId, new PopulationTemplate(childrenRatio, adultsRatio, seniorsRatio));
            }

            return populationTemplates;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readProvinces(World ecsWorld, IntObjectMap<LongIntMap> regionBuildingsByProvince, Map<String, PopulationTemplate> populationTemplates) {
        IntObjectMap<String> provincesPaths = new IntObjectMap<>(15000, 1f);
        try {
            JsonValue provincesValues = this.parseJsonFile(this.provincesJsonFile);
            for(var provinceEntry : provincesValues.object()) {
                provincesPaths.put(Integer.parseInt(provinceEntry.getKey()), this.historyPath + provinceEntry.getValue().asString());
            }

            for (IntObjectMap.Entry<String> provinceEntry : provincesPaths.entrySet()) {
                int provinceId = provinceEntry.getKey();
                String provincePath = provinceEntry.getValue();
                this.readProvince(ecsWorld, provincePath, provinceId, regionBuildingsByProvince, populationTemplates);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readProvince(World ecsWorld, String provincePath, int provinceNameId, IntObjectMap<LongIntMap> regionBuildingsByProvince, Map<String, PopulationTemplate> populationTemplates) {
        try {
            JsonValue provinceValues = this.parseJsonFile(provincePath);

            long provinceId = ecsWorld.entity(String.valueOf(provinceNameId));
            Entity province = ecsWorld.obtainEntity(provinceId);

            long[] coreIds = new long[8];
            JsonValue addCoreValue = provinceValues.get("add_core");
            if (addCoreValue.isArray()) {
                int i = 0;
                for(var countryCore : addCoreValue.array()) {
                    long countryCoreId = ecsWorld.lookup(countryCore.asString());
                    coreIds[i] = countryCoreId;
                    i++;
                }
            } else {
                coreIds[0] = ecsWorld.lookup(addCoreValue.asString());
            }

            String owner = provinceValues.get("owner").asString();
            long countryOwnerId = ecsWorld.lookup(owner);

            String controller = provinceValues.get("controller").asString();
            long countryControllerId = ecsWorld.lookup(controller);

            String terrain = provinceValues.get("terrain").asString();
            long terrainId = ecsWorld.lookup(terrain);

            JsonValue populationValue = provinceValues.get("population_total");
            int amount = (int) populationValue.get("amount").asLong();
            String populationTemplateId = String.valueOf(populationValue.get("template").asLong());
            PopulationTemplate populationTemplate = populationTemplates.get(populationTemplateId);
            int childrenAmount = (int) (amount * populationTemplate.childrenRatio());
            int adultsAmount = (int) (amount * populationTemplate.adultsRatio());
            int seniorsAmount = (int) (amount * populationTemplate.seniorsRatio());

            this.parsePopulation(ecsWorld, provinceId, populationValue.get("populations"), adultsAmount);
            Pair<long[], int[]> cultures = this.parseDistribution(ecsWorld, populationValue.get("cultures"), adultsAmount);
            Pair<long[], int[]> religions = this.parseDistribution(ecsWorld, populationValue.get("religions"), adultsAmount);

            JsonValue buildingsValue = provinceValues.get("economy_buildings");
            if(buildingsValue != null) {
                LongIntMap buildings = new LongIntMap();
                for(var buildingValue : buildingsValue.array()) {
                    String buildingName = buildingValue.get("name").asString();
                    int size = (int) buildingValue.get("size").asLong();
                    buildings.put(ecsWorld.lookup(buildingName), size);
                }
                regionBuildingsByProvince.put(provinceNameId, buildings);
            }

            JsonValue goodJsonValue = provinceValues.get("good");
            if(goodJsonValue != null) {
                String goodNameId = goodJsonValue.asString();
                long goodId = ecsWorld.lookup(goodNameId);
                EntityView goodEntity = ecsWorld.obtainEntityView(goodId);
                Good good = goodEntity.get(Good.class);
                float goodAmount = good.value();
                long rgoTypeId = ecsWorld.lookup("rgo_" + goodNameId);
                province.set(new ResourceGathering(rgoTypeId, goodId, this.getGoodIndex(goodId), goodAmount, 0, 0f, new int[12]));
            }

            JsonValue buildingsProvinceValue = provinceValues.get("buildings");
            if(buildingsProvinceValue != null) {
                for(var buildingValue : buildingsProvinceValue.array()) {
                    String buildingName = buildingValue.get("name").asString();
                    int size = (int) buildingValue.get("size").asLong();
                    long buildingTypeId = ecsWorld.lookup(buildingName);
                    EntityView building = ecsWorld.obtainEntityView(ecsWorld.entity());
                    building.set(new Building(provinceId, buildingTypeId, size));
                    building.set(new DevelopmentBuilding());
                }
            }

            province.set(new Province(coreIds, countryOwnerId, countryControllerId, terrainId, childrenAmount, adultsAmount, seniorsAmount, cultures.first(), cultures.second(), religions.first(), religions.second()));
            province.set(new Demographics(0, 0, 0, 0f, 0f, 0f, new long[12], new long[12], new float[12], new float[12], new float[12], new float[12], 0, 0, 0, new long[20], new long[20], new long[20], new long[20]));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void parsePopulation(World ecsWorld, long provinceId, JsonValue distributionValue, int adultsAmount) {
        for(var distributionEntry : distributionValue.object()) {
            String name = distributionEntry.getKey();
            float percentage = (float) distributionEntry.getValue().asDouble();
            int amount = (int) (adultsAmount * percentage);
            long typeId = ecsWorld.lookup(name);
            int index = this.getPopulationTypeIndex(typeId);
            long popId = ecsWorld.entity();
            EntityView pop = ecsWorld.obtainEntityView(popId);
            pop.set(new Population(index, typeId, provinceId, amount, 0, 0f, 0f, 0f, 0f));
        }
    }

    private Pair<long[], int[]> parseDistribution(World ecsWorld, JsonValue distributionValue, int adultsAmount) {
        long[] ids = new long[20];
        int[] amounts = new int[20];
        int i = 0;
        for(var distributionEntry : distributionValue.object()) {
            String name = distributionEntry.getKey();
            float percentage = (float) distributionEntry.getValue().asDouble();
            int value = (int) (adultsAmount * percentage);
            long id = ecsWorld.lookup(name);
            ids[i] = id;
            amounts[i] = value;
            i++;
        }

        return new Pair<>(ids, amounts);
    }

    private void readRegion(World ecsWorld, EcsConstants ecsConstants, IntObjectMap<LongIntMap> regionBuildingsByProvince) {
        try {
            JsonValue regionValue = this.parseJsonFile(this.regionJsonFiles);
            for(var regionEntry : regionValue.object()) {
                String regionId = regionEntry.getKey();
                long regionEntityId = ecsWorld.entity(regionId);
                EntityView region = ecsWorld.obtainEntityView(regionEntityId);
                region.add(ecsConstants.regionTag());
                for(var provinceValue : regionEntry.getValue().array()) {
                    int provinceId = (int) provinceValue.asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    EntityView province = provinceEntityId != 0 ? ecsWorld.obtainEntityView(provinceEntityId) : null;
                    if(provinceEntityId != 0 && province.has(Province.class)) {
                        ProvinceView provinceData = province.getMutView(Province.class);
                        long localMarketId = ecsWorld.entity("local_market_" + region.id() + "_" + provinceData.ownerId());
                        EntityView localMarket = ecsWorld.obtainEntityView(localMarketId);
                        if(!localMarket.has(LocalMarket.class)) {
                            localMarket.set(new LocalMarket(regionEntityId, provinceData.ownerId(), new float[40], new float[40]));
                            localMarket.set(new Demographics(0, 0, 0, 0f, 0f, 0f, new long[12], new long[12], new float[12], new float[12], new float[12], new float[12], 0, 0, 0, new long[20], new long[20], new long[20], new long[20]));

                        }
                        province.set(new GeoHierarchy(regionEntityId, -1, localMarketId));
                        LongIntMap regionBuildingIds = regionBuildingsByProvince.get(provinceId);
                        if(regionBuildingIds != null) {
                            for(var buildingEntry : regionBuildingIds) {
                                long buildingTypeId = buildingEntry.key;
                                int size = buildingEntry.value;
                                EntityView building = ecsWorld.obtainEntityView(ecsWorld.entity());
                                EntityView buildingType = ecsWorld.obtainEntityView(buildingTypeId);
                                building.set(new Building(localMarketId, buildingTypeId, size));
                                if(buildingType.has(EconomyBuildingType.class)) {
                                    building.set(new EconomyBuilding(0f, 0f, new int[12]));
                                } else if (buildingType.has(SpecialBuildingType.class)) {
                                    building.set(new SpecialBuilding());
                                }
                            }
                        }
                    } else {
                        provinceEntityId = ecsWorld.entity(String.valueOf(provinceId));
                        province = ecsWorld.obtainEntityView(provinceEntityId);
                        province.add(ecsConstants.seaProvinceTag());
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readDefinition(World ecsWorld, IntLongMap provinces) {
        try (BufferedReader bufferedReader = this.parseCsvFile(this.definitionCsvFile)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    int provinceId = Integer.parseInt(values[0]);
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    EntityView provinceEntity = ecsWorld.obtainEntityView(provinceEntityId);
                    int red = Integer.parseInt(values[1]);
                    int green = Integer.parseInt(values[2]);
                    int blue = Integer.parseInt(values[3]);
                    int alpha = 255;

                    int color =  (red << 24) | (green << 16) | (blue << 8) | alpha;
                    provinceEntity.set(new Color(color));
                    if (provinceEntity.has(Province.class)) {
                        provinces.put(color, provinceEntityId);
                    }
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private Borders readProvinceBitmap(World ecsWorld, IntLongMap provinces) {
        Pixmap provincesPixmap = new Pixmap(Gdx.files.internal(this.mapPath + "provinces.bmp"));

        IntList xyValues = new IntList();
        LongObjectMap<IntList> temporaryGroups = new LongObjectMap<>();

        int height = provincesPixmap.getHeight();
        int width = provincesPixmap.getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = provincesPixmap.getPixel(x, y);
                long provinceEntityId = provinces.get(color);

                if (provinceEntityId != 0 && isBorderPixel(provincesPixmap, x, y, color, width, height)) {
                    IntList group = temporaryGroups.get(provinceEntityId);
                    if (group == null) {
                        group = new IntList();
                        temporaryGroups.put(provinceEntityId, group);
                    }
                    group.add(x);
                    group.add(y);
                }
            }
        }

        for (LongObjectMap.Entry<IntList> entry : temporaryGroups.entrySet()) {
            IntList coords = entry.value;
            int startIndex = xyValues.size();
            xyValues.addAll(coords);
            int endIndex = xyValues.size();
            EntityView provinceEntity = ecsWorld.obtainEntityView(entry.key);
            provinceEntity.set(new Border(startIndex, endIndex));
        }
        provincesPixmap.dispose();
        return new Borders(xyValues.shrink());
    }

    private boolean isBorderPixel(Pixmap pixmap, int x, int y, int color, int w, int h) {
        if (x + 1 < w && pixmap.getPixel(x + 1, y) != color) {
            return true;
        }
        if (x > 0 && pixmap.getPixel(x - 1, y) != color) {
            return true;
        }
        if (y + 1 < h && pixmap.getPixel(x, y + 1) != color) {
            return true;
        }
        return y > 0 && pixmap.getPixel(x, y - 1) != color;
    }

    private void readCountriesHistory(World ecsWorld) {
        ObjectObjectMap<String, String> countriesHistoryPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesJson = this.parseJsonFile(this.countriesHistoryJsonFiles);
            for(var countryEntry : countriesJson.object()) {
                countriesHistoryPaths.put(countryEntry.getKey(), this.historyPath + countryEntry.getValue().asString());
            }
            for (var countryHistory : countriesHistoryPaths) {
                String countryNameId = countryHistory.getKey();
                String countryFileName = countryHistory.getValue();
                this.readCountryHistory(ecsWorld, countryFileName, countryNameId);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readCountryHistory(World ecsWorld, String countryFileName, String countryNameId) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            EntityView country = ecsWorld.obtainEntityView(ecsWorld.lookup(countryNameId));

            JsonValue countryValues = this.parseJsonFile(countryFileName);
            int capital = (int) countryValues.get("capital").asLong();
            long capitalId = ecsWorld.lookup(String.valueOf(capital));
            String government = countryValues.get("government").asString();
            long governmentId = ecsWorld.lookup(government);
            String ideology = countryValues.get("ideology").asString();
            long ideologyId = ecsWorld.lookup(ideology);
            String identity = countryValues.get("national_identity").asString();
            long identityId = ecsWorld.lookup(identity);
            String attitude = countryValues.get("national_attitude").asString();
            long attitudeId = ecsWorld.lookup(attitude);
            long ministerHeadOfStateEntityId = 0;
            long ministerHeadOfGovernmentEntityId = 0;
            if(countryValues.get("head_of_state") != null && countryValues.get("head_of_government") != null) {
                String ministerHeadOfStateId = countryValues.get("head_of_state").asString();
                String ministerHeadOfGovernmentId = countryValues.get("head_of_government").asString();
                ministerHeadOfStateEntityId = ecsWorld.lookup(String.valueOf(ministerHeadOfStateId));
                ministerHeadOfGovernmentEntityId = ecsWorld.lookup(String.valueOf(ministerHeadOfGovernmentId));
            }

            long[] lawIds = new long[48];
            int i = 0;
            for(var lawEntry : countryValues.get("laws").object()) {
                String lawGroupName = lawEntry.getKey();
                String lawName = lawEntry.getValue().asString();

                long lawGroupId = ecsWorld.lookup(lawGroupName);
                long lawId = ecsWorld.lookup(lawName);

                lawIds[i] = lawGroupId;
                lawIds[i + 1] = lawId;
                i += 2;
            }
            country.set(new Country(capitalId, governmentId, ideologyId, identityId, attitudeId, ministerHeadOfStateEntityId, ministerHeadOfGovernmentEntityId, lawIds));
            country.set(new Demographics(0, 0, 0, 0f, 0f, 0f, new long[12], new long[12], new float[12], new float[12], new float[12], new float[12], 0, 0, 0, new long[20], new long[20], new long[20], new long[20]));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readContinent(World ecsWorld) {
        try {
            JsonValue continentValues = this.parseJsonFile(this.continentJsonFile);
            for(var continentEntry : continentValues.object()) {
                String continentName = continentEntry.getKey();
                long continentEntityId = ecsWorld.entity(continentName);
                for(var provinceEntry : continentEntry.getValue().array()) {
                    int provinceId = (int) provinceEntry.asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    EntityView provinceEntity = ecsWorld.obtainEntityView(provinceEntityId);
                    provinceEntity.set(GeoHierarchy.class, (GeoHierarchyView geoHierarchyView) ->
                        geoHierarchyView.continentId(continentEntityId));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readAdjacencies(World ecsWorld) {
        try {
            JsonValue adjenciesValues = this.parseJsonFile(this.adjacenciesJsonFile);
            for(var provinceEntry : adjenciesValues.object()) {
                int provinceId = Integer.parseInt(provinceEntry.getKey());
                long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                EntityView provinceEntity = ecsWorld.obtainEntityView(provinceEntityId);
                long[] adjacencyIds = new long[32];
                int i = 0;
                for(var adjacencyValue : provinceEntry.getValue().array()) {
                    int adjacencyId = (int) adjacencyValue.asLong();
                    long adjacencyEntityId = ecsWorld.lookup(String.valueOf(adjacencyId));
                    adjacencyIds[i] = adjacencyEntityId;
                    i++;
                }
                provinceEntity.set(new Adjacencies(adjacencyIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void readPositions(World ecsWorld) {
        try {
            JsonValue positionsValues = this.parseJsonFile(this.positionsJsonFile);
            for(var provinceEntry : positionsValues.object()) {
                int provinceId = Integer.parseInt(provinceEntry.getKey());
                long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                for(var positionEntry : provinceEntry.getValue().object()) {
                    String name = positionEntry.getKey();
                    JsonValue positionNode = positionEntry.getValue();
                    int x = (int) positionNode.get("x").asLong();
                    int y = (int) positionNode.get("y").asLong();
                    long positionEntityId = ecsWorld.entity("province_" + provinceId + "_pos_" + name);
                    EntityView positionEntity = ecsWorld.obtainEntityView(positionEntityId);
                    positionEntity.set(new Position(x, y, provinceEntityId));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private int parseColor(JsonValue colorValue) {
        Iterator<JsonValue> colorValueIterator = colorValue.arrayIterator();
        int red = (int) colorValueIterator.next().asLong();
        int green = (int) colorValueIterator.next().asLong();
        int blue = (int) colorValueIterator.next().asLong();
        int alpha = 255;
        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    private int getPopulationTypeIndex(long popTypeId) {
        for (int i = 0; i < this.populationTypeIds.length; i++) {
            if (this.populationTypeIds[i] == popTypeId) {
                return i;
            }
        }
        return -1;
    }

    private int getGoodIndex(long goodId) {
        for (int i = 0; i < this.goodIds.length; i++) {
            if (this.goodIds[i] == goodId) {
                return i;
            }
        }
        return -1;
    }
}

