package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.builder.*;
import com.populaire.projetguerrefroide.economy.building.*;
import com.populaire.projetguerrefroide.economy.good.*;
import com.populaire.projetguerrefroide.economy.population.PopulationTemplateStore;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.ForceType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
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
    private final String adjenciesJsonFile = this.mapPath + "adjacencies.json";
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

    public WorldDaoImpl() {

    }

    @Override
    public WorldManager createWorld(GameContext gameContext) {
        World ecsWorld = gameContext.getEcsWorld();
        this.initializeRelations(ecsWorld);
        this.readIdeologiesJson(ecsWorld);
        this.readGovernmentsJson(ecsWorld);
        this.readNationalIdeasJson(ecsWorld);
        this.readMinisterTypesJson(ecsWorld);
        ObjectIntMap<String> goodIds = new ObjectIntMap<>(40, 1f);
        GoodStore goodStore = this.readGoodsJson(goodIds);
        ObjectIntMap<String> populationTypeIds = new ObjectIntMap<>(12, 1f);
        PopulationTypeStore populationTypeStore = this.readPopulationTypesJson(goodIds, populationTypeIds);
        ObjectIntMap<String> productionTypeIds = new ObjectIntMap<>(5, 1f);
        EmployeeStoreBuilder employeeStoreBuilder = new EmployeeStoreBuilder();
        EmployeeStore employeeStore = employeeStoreBuilder.build();
        ObjectIntMap<String> employeeIds = new ObjectIntMap<>(employeeStoreBuilder.getDefaultCapacity(), 1f);
        ProductionTypeStore productionTypeStore = this.readProductionTypesJson(populationTypeIds, employeeStoreBuilder, productionTypeIds, employeeIds);
        ObjectIntMap<String> buildingIds = new ObjectIntMap<>(54, 1f);
        BuildingStore buildingStore = this.readBuildingsJson(goodIds, productionTypeIds, buildingIds);
        this.readResourceProductionsJson(goodStore, goodIds, productionTypeIds);
        this.readLawsJson(ecsWorld);
        this.readTraitsJson(ecsWorld);
        Map<String, Country> countries = this.loadCountries(ecsWorld);
        this.readTerrainsJson(ecsWorld);
        RegionStoreBuilder regionStoreBuilder = new RegionStoreBuilder();
        IntLongMap provinces = new IntLongMap(15000, 1f);
        Borders borders = new Borders();
        ProvinceStore provinceStore = this.loadProvinces(ecsWorld, regionStoreBuilder, countries, provinces, goodIds, buildingIds, populationTypeIds, borders);
        RegionStore regionStore = regionStoreBuilder.build();

        return new WorldManager(new ObjectList<>(countries.values()), provinces, provinceStore, regionStore, buildingStore, goodStore, productionTypeStore, employeeStore, populationTypeStore, borders, gameContext);
    }

    private void initializeRelations(World ecsWorld) {
        long alignedWithId = ecsWorld.entity(EcsConstants.EcsAlignedWith);
        ecsWorld.obtainEntity(alignedWithId).add(FlecsConstants.EcsExclusive);
        long locatedInRegion = ecsWorld.entity(EcsConstants.EcsLocatedInRegion);
        ecsWorld.obtainEntity(locatedInRegion).add(FlecsConstants.EcsExclusive);
        long locatedInContinent = ecsWorld.entity(EcsConstants.EcsLocatedInContinent);
        ecsWorld.obtainEntity(locatedInContinent).add(FlecsConstants.EcsExclusive);
        ecsWorld.entity(EcsConstants.EcsAcceptance);
        long hasId = ecsWorld.entity(EcsConstants.EcsHas);
        ecsWorld.obtainEntity(hasId).add(FlecsConstants.EcsExclusive);
        ecsWorld.entity(EcsConstants.EcsBelongsTo);
        ecsWorld.entity(EcsConstants.EcsSupportedBy);
        ecsWorld.entity(EcsConstants.EcsOpposedBy);
        ecsWorld.entity(EcsConstants.EcsLawGroupTag);
        long provinceTagId = ecsWorld.entity(EcsConstants.EcsProvinceTag);
        long landProvinceTagId = ecsWorld.entity(EcsConstants.EcsLandProvinceTag);
        ecsWorld.obtainEntity(landProvinceTagId).isA(provinceTagId);
        long seaProvinceTagId = ecsWorld.entity(EcsConstants.EcsSeaProvinceTag);
        ecsWorld.obtainEntity(seaProvinceTagId).isA(provinceTagId);
        ecsWorld.entity(EcsConstants.EcsRegionTag);
        ecsWorld.entity(EcsConstants.EcsCountryTag);
        ecsWorld.entity(EcsConstants.EcsContinentTag);
        ecsWorld.entity(EcsConstants.EcsAdjacentTo);
        long controlledById = ecsWorld.entity(EcsConstants.EcsControlledBy);
        ecsWorld.obtainEntity(controlledById).add(FlecsConstants.EcsExclusive);
        long ownedById = ecsWorld.entity(EcsConstants.EcsOwnedBy);
        ecsWorld.obtainEntity(ownedById).add(FlecsConstants.EcsExclusive);
        ecsWorld.entity(EcsConstants.EcsCoreOf);
        ecsWorld.entity(EcsConstants.EcsPositionElementTag);
        long hasTerrain = ecsWorld.entity(EcsConstants.EcsHasTerrain);
        ecsWorld.obtainEntity(hasTerrain).add(FlecsConstants.EcsExclusive);
        ecsWorld.entity(EcsConstants.EcsAlliedWith);
        ecsWorld.entity(EcsConstants.EcsGuarantees);
        ecsWorld.entity(EcsConstants.EcsIsGuaranteedBy);
        ecsWorld.entity(EcsConstants.EcsIsPuppetMasterOf);
        ecsWorld.entity(EcsConstants.EcsIsPuppetOf);
        ecsWorld.entity(EcsConstants.EcsColonizes);
        ecsWorld.entity(EcsConstants.EcsIsColonyOf);
        ecsWorld.entity(EcsConstants.EcsHasCapital);
    }

    private JsonValue parseJsonFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return this.mapper.parse(fileHandle.read(), (int) fileHandle.length());
    }

    private BufferedReader parseCsvFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return new BufferedReader(new StringReader(fileHandle.readString()));
    }

    private void readNationalIdeasJson(World ecsWorld) {
        try {
            JsonValue nationalIdeasValues = this.parseJsonFile(this.nationalIdeasJsonFile);

            Iterator<Map.Entry<String, JsonValue>> culturesEntryIterator = nationalIdeasValues.get("cultures").objectIterator();
            while (culturesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> cultureEntry = culturesEntryIterator.next();
                String name = cultureEntry.getKey();
                JsonValue cultureValue = cultureEntry.getValue();
                int color = this.parseColor(cultureValue.get("color"));
                long cultureEntityId = ecsWorld.entity(name);
                Entity cultureEntity = ecsWorld.obtainEntity(cultureEntityId);
                cultureEntity.set(new Color(color));
            }

            Iterator<Map.Entry<String, JsonValue>> religionsEntryIterator = nationalIdeasValues.get("religions").objectIterator();
            while (religionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> religionEntry = religionsEntryIterator.next();
                String name = religionEntry.getKey();
                JsonValue religionValue = religionEntry.getValue();
                int color = this.parseColor(religionValue.get("color"));
                long religionEntityId = ecsWorld.entity(name);
                Entity religionEntity = ecsWorld.obtainEntity(religionEntityId);
                religionEntity.set(new Color(color));
            }

            Iterator<Map.Entry<String, JsonValue>> identitiesEntryIterator = nationalIdeasValues.get("national_identity").objectIterator();
            while (identitiesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> identityEntry = identitiesEntryIterator.next();
                String name = identityEntry.getKey();
                long identityEntityId = ecsWorld.entity(name);
                Entity identityEntity = ecsWorld.obtainEntity(identityEntityId);
                JsonValue identityValue = identityEntry.getValue();
                Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = identityValue.objectIterator();
                while (modifiersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    float value = (float) modifierValue.asDouble();
                    long modifierTagId = ecsWorld.entity(modifierName);
                    identityEntity.set(new Modifier(value), modifierTagId);
                }
            }

            Iterator<Map.Entry<String, JsonValue>> attitudesEntryIterator = nationalIdeasValues.get("national_attitude").objectIterator();
            while (attitudesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> attitudeEntry = attitudesEntryIterator.next();
                String attitudeName = attitudeEntry.getKey();
                long attitudeEntityId = ecsWorld.entity(attitudeName);
                Entity attitudeEntity = ecsWorld.obtainEntity(attitudeEntityId);
                JsonValue attitudeValue = attitudeEntry.getValue();
                Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = attitudeValue.objectIterator();
                while (modifiersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    float value = (float) modifierValue.asDouble();
                    long modifierTagId = ecsWorld.entity(modifierName);
                    attitudeEntity.set(new Modifier(value), modifierTagId);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readGovernmentsJson(World ecsWorld) {
        try {
            JsonValue governmentsValues = this.parseJsonFile(this.governmentJsonFile);

            long idAcceptance = ecsWorld.lookup(EcsConstants.EcsAcceptance);
            Iterator<Map.Entry<String, JsonValue>> governmentsEntryIterator = governmentsValues.objectIterator();
            while (governmentsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> governmentEntry = governmentsEntryIterator.next();
                String governmentName = governmentEntry.getKey();
                JsonValue governmentValue = governmentEntry.getValue();

                long governmentEntityId = ecsWorld.entity(governmentName);
                Entity governmentEntity = ecsWorld.obtainEntity(governmentEntityId);

                Iterator<JsonValue> associatedIdeologiesIterator = governmentValue.get("associated_ideologies").arrayIterator();
                while (associatedIdeologiesIterator.hasNext()) {
                    JsonValue associatedIdeologyValue = associatedIdeologiesIterator.next();
                    String ideologyName = associatedIdeologyValue.asString();
                    long ideologyEntityId = ecsWorld.lookup(ideologyName);
                    governmentEntity.addRelation(idAcceptance, ideologyEntityId);
                }

                JsonValue electionValue = governmentValue.get("election");
                if(!electionValue.isNull() && electionValue.getSize() > 0) {
                    boolean headOfState = electionValue.get("head_of_state").asBoolean();
                    boolean headOfGovernment = electionValue.get("head_of_government").asBoolean();
                    short duration = (short) electionValue.get("duration").asLong();
                    governmentEntity.set(new ElectoralMechanism(headOfState, headOfGovernment, duration));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readIdeologiesJson(World ecsWorld) {
        try {
            JsonValue ideologiesValues = this.parseJsonFile(this.ideologiesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> ideologiesEntryIterator = ideologiesValues.objectIterator();
            while (ideologiesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = ideologiesEntryIterator.next();
                String ideologyName = entry.getKey();
                JsonValue ideologyValue = entry.getValue();
                int color = this.parseColor(ideologyValue.get("color"));
                byte factionDriftingSpeed = (byte) ideologyValue.get("faction_drifting_speed").asLong();

                long ideologyEntityId = ecsWorld.entity(ideologyName);
                Entity ideologyEntity = ecsWorld.obtainEntity(ideologyEntityId);
                ideologyEntity.set(new Ideology(color, factionDriftingSpeed));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private GoodStore readGoodsJson(ObjectIntMap<String> goodIds) {
        GoodStoreBuilder goodStoreBuilder = new GoodStoreBuilder();
        try {
            JsonValue goodsValues = this.parseJsonFile(this.goodsJsonFile);

            Iterator<Map.Entry<String, JsonValue>> resourceGoodsEntryIterator = goodsValues.get("resource_goods").objectIterator();
            while (resourceGoodsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = resourceGoodsEntryIterator.next();
                String goodName = entry.getKey();
                JsonValue goodValue = entry.getValue();
                float cost = (float) goodValue.get("cost").asDouble();
                float value = (float) goodValue.get("value").asDouble();
                int color = this.parseColor(goodValue.get("color"));
                goodStoreBuilder.addGood(goodName, cost, color, GoodType.RESOURCE.getId(), -1, value);
                goodIds.put(goodName, goodStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> goodsEntryIterator = goodsValues.get("advanced_goods").objectIterator();
            while (goodsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = goodsEntryIterator.next();
                String advancedGoodName = entry.getKey();
                JsonValue advancedGoodValue = entry.getValue();
                float cost = (float) advancedGoodValue.get("cost").asDouble();
                int color = this.parseColor(advancedGoodValue.get("color"));
                goodStoreBuilder.addGood(advancedGoodName, cost, color, GoodType.ADVANCED.getId(), -1, -1f);
                goodIds.put(advancedGoodName, goodStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> militaryGoodsEntryIterator = goodsValues.get("military_goods").objectIterator();
            while (militaryGoodsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = militaryGoodsEntryIterator.next();
                String militaryGoodsName = entry.getKey();
                JsonValue militaryGoodNode = entry.getValue();
                float cost = (float) militaryGoodNode.get("cost").asDouble();
                int color = this.parseColor(militaryGoodNode.get("color"));
                goodStoreBuilder.addGood(militaryGoodsName, cost, color, GoodType.MILITARY.getId(), -1, -1f);
                goodIds.put(militaryGoodsName, goodStoreBuilder.getIndex());
            }

            return goodStoreBuilder.build();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private PopulationTypeStore readPopulationTypesJson(ObjectIntMap<String> goodIds, ObjectIntMap<String> productionTypesIds) {
        Map<String, String> populationPaths = new ObjectObjectMap<>(12, 1f);
        PopulationTypeStoreBuilder builder = new PopulationTypeStoreBuilder();
        try {
            JsonValue populationTypesValues = this.parseJsonFile(this.populationTypesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> populationTypesEntryIterator = populationTypesValues.objectIterator();
            while (populationTypesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = populationTypesEntryIterator.next();
                populationPaths.put(entry.getKey(), this.commonPath + entry.getValue().asString());
            }

            for (Map.Entry<String, String> populationPath : populationPaths.entrySet()) {
                this.readPopulationTypeJson(populationPath.getValue(), populationPath.getKey(), goodIds, builder, productionTypesIds);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return builder.build();
    }

    private void readPopulationTypeJson(String populationTypePath, String name, ObjectIntMap<String> goodIds, PopulationTypeStoreBuilder builder, ObjectIntMap<String> populationTypeIds) {
        try {
            JsonValue populationTypeValue = this.parseJsonFile(populationTypePath);
            int color = this.parseColor(populationTypeValue.get("color"));
            builder.addPopulationType(name, color);
            Iterator<Map.Entry<String, JsonValue>> standardDemandsEntryIterator = populationTypeValue.get("standard_demands").objectIterator();
            while (standardDemandsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = standardDemandsEntryIterator.next();
                int goodId = goodIds.get(entry.getKey());
                float value = (float) entry.getValue().asDouble();
                builder.addStandardDemand(goodId, value);
            }

            Iterator<Map.Entry<String, JsonValue>> luxuryDemandsEntryIterator = populationTypeValue.get("luxury_demands").objectIterator();
            while (luxuryDemandsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = luxuryDemandsEntryIterator.next();
                int goodId = goodIds.get(entry.getKey());
                float value = (float) entry.getValue().asDouble();
                builder.addLuxuryDemand(goodId, value);
            }
            populationTypeIds.put(name, builder.getIndex());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private ProductionTypeStore readProductionTypesJson(ObjectIntMap<String> populationTypeIds, EmployeeStoreBuilder employeeStoreBuilder, ObjectIntMap<String> productionTypeIds, ObjectIntMap<String> employeeIds) {
        ProductionTypeStoreBuilder productionTypeStoreBuilder = new ProductionTypeStoreBuilder();
        try {
            JsonValue buildingTypesJson = this.parseJsonFile(this.productionTypesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> typesEmployeesValues = buildingTypesJson.get("types_employees").objectIterator();
            while (typesEmployeesValues.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesEmployeesValues.next();
                String typeName = entry.getKey();
                JsonValue typeEmployeeValue = entry.getValue();
                int populationTypeId = populationTypeIds.get(typeEmployeeValue.get("poptype").asString());
                float amount = (float) typeEmployeeValue.get("amount").asDouble();
                float effectMultiplier = (float) typeEmployeeValue.get("effect_multiplier").asDouble();
                employeeStoreBuilder.addEmployee(populationTypeId, amount, effectMultiplier);
                employeeIds.put(typeName, employeeStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> typesBuildingsValues = buildingTypesJson.get("types_buildings").objectIterator();
            while (typesBuildingsValues.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesBuildingsValues.next();
                String typeName = entry.getKey();
                JsonValue typeBuildingsValue = entry.getValue();
                short workforce = (short) typeBuildingsValue.get("workforce").asLong();
                int ownerId = populationTypeIds.get(typeBuildingsValue.get("owner").get("poptype").asString());
                productionTypeStoreBuilder.addProductionType(workforce, ownerId);
                Iterator<JsonValue> employeesIterator = typeBuildingsValue.get("employees").arrayIterator();
                while (employeesIterator.hasNext()) {
                    JsonValue employee = employeesIterator.next();
                    String employeeName = employee.asString();
                    int employeeId = employeeIds.get(employeeName);
                    productionTypeStoreBuilder.addEmployee(employeeId);
                }
                productionTypeIds.put(typeName, productionTypeStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> typesRGOs = buildingTypesJson.get("types_rgo").objectIterator();
            while (typesRGOs.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesRGOs.next();
                String typeName = entry.getKey();
                JsonValue typeRGOValue = entry.getValue();
                int workforce = (int) typeRGOValue.get("workforce").asLong();
                int ownerId = populationTypeIds.get(typeRGOValue.get("owner").get("poptype").asString());
                productionTypeStoreBuilder.addProductionType(workforce, ownerId);
                Iterator<JsonValue> employeesIterator = typeRGOValue.get("employees").arrayIterator();
                while (employeesIterator.hasNext()) {
                    JsonValue employee = employeesIterator.next();
                    String employeeName = employee.asString();
                    int employeeId = employeeIds.get(employeeName);
                    productionTypeStoreBuilder.addEmployee(employeeId);
                }
                productionTypeIds.put(typeName, productionTypeStoreBuilder.getIndex());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return productionTypeStoreBuilder.build();
    }

    private BuildingStore readBuildingsJson(ObjectIntMap<String> goodIds, ObjectIntMap<String> productionTypeIds, ObjectIntMap<String> buildingIds) {
        BuildingStoreBuilder buildingStoreBuilder = new BuildingStoreBuilder();
        try {
            JsonValue buildingsValues = this.parseJsonFile(this.buildingsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> economyBuildingEntryIterator = buildingsValues.get("economy_building").objectIterator();
            while (economyBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = economyBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                short time = (short) buildingValue.get("time").asLong();
                buildingStoreBuilder.addBuilding(buildingName, time, BuildingType.ECONOMY.getId());
                int baseTypeId = productionTypeIds.get(buildingValue.get("base_type").asString());
                buildingStoreBuilder.addBaseType(baseTypeId);
                int artisansTypeId = -1;
                if(buildingValue.get("artisans_type") != null) {
                    artisansTypeId = productionTypeIds.get(buildingValue.get("artisans_type").asString());
                }
                buildingStoreBuilder.addArtisansType(artisansTypeId);
                byte maxLevel = (byte) buildingValue.get("max_level").asLong();
                buildingStoreBuilder.addMaxLevel(maxLevel);
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    int goodId = goodIds.get(goodCost.getKey());
                    float goodValue = (float) goodCost.getValue().asDouble();
                    buildingStoreBuilder.addGoodsCost(goodId, goodValue);
                }
                Iterator<Map.Entry<String, JsonValue>> inputGoodsEntryIterator = buildingValue.get("input_goods").objectIterator();
                while (inputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> inputGood = inputGoodsEntryIterator.next();
                    int goodId = goodIds.get(inputGood.getKey());
                    float goodValue = (float) inputGood.getValue().asDouble();
                    buildingStoreBuilder.addInputGood(goodId, goodValue);
                }
                Iterator<Map.Entry<String, JsonValue>> outputGoodsEntryIterator = buildingValue.get("output_goods").objectIterator();
                if (outputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> outputGood = outputGoodsEntryIterator.next();
                    int goodId = goodIds.get(outputGood.getKey());
                    float goodValue = (float) outputGood.getValue().asDouble();
                    buildingStoreBuilder.addOutputGood(goodId, goodValue);
                }

                buildingStoreBuilder.addCost(-1).addOnMap(false);
                buildingIds.put(buildingName, buildingStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> specialBuildingEntryIterator = buildingsValues.get("special_building").objectIterator();
            while (specialBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = specialBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                short time = (short) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                buildingStoreBuilder.addBuilding(buildingName, time, BuildingType.SPECIAL.getId()).addCost(cost);
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    int goodId = goodIds.get(goodCost.getKey());
                    float goodValue = (float) goodCost.getValue().asDouble();
                    buildingStoreBuilder.addGoodsCost(goodId, goodValue);
                }

                JsonValue modifiersValues = buildingValue.get("modifier");
                if (modifiersValues != null) {
                    Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = modifiersValues.objectIterator();
                    while (modifiersEntryIterator.hasNext()) {
                        Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                        String modifierName = modifierEntry.getKey();
                        JsonValue modifierValue = modifierEntry.getValue();
                        if (modifierValue.isLong()) {
                            buildingStoreBuilder.addModifier(-1); // todo : refactor modifier to data oriented
                        } else {
                            float value = (float) modifierValue.get("value").asDouble();
                            String modifierType = modifierValue.get("type").asString();
                            buildingStoreBuilder.addModifier(-1); // todo : refactor modifier to data oriented
                        }
                    }
                }
                buildingStoreBuilder.addMaxLevel((byte) -1).addBaseType(-1).addArtisansType(-1).addOnMap(false);
                buildingIds.put(buildingName, buildingStoreBuilder.getIndex());
            }

            Iterator<Map.Entry<String, JsonValue>> developmentBuildingEntryIterator = buildingsValues.get("development_building").objectIterator();
            while (developmentBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = developmentBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                short time = (short) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                buildingStoreBuilder.addBuilding(buildingName, time, BuildingType.DEVELOPMENT.getId()).addCost(cost);
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    int goodId = goodIds.get(goodCost.getKey());
                    float goodValue = (float) goodCost.getValue().asDouble();
                    buildingStoreBuilder.addGoodsCost(goodId, goodValue);
                }
                boolean onMap = buildingValue.get("onmap").asBoolean();
                byte maxLevel = (byte) buildingValue.get("max_level").asLong();
                buildingStoreBuilder.addOnMap(onMap).addMaxLevel(maxLevel);
                JsonValue modifierValues = buildingValue.get("modifier");
                if(modifierValues != null) {
                    Iterator<Map.Entry<String, JsonValue>> modifierEntryIterator = modifierValues.objectIterator();
                    Map.Entry<String, JsonValue> modifierEntry = modifierEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    float modifierValue = (float) modifierEntry.getValue().asLong();
                    buildingStoreBuilder.addModifier(-1); // todo : refactor modifier to data oriented
                }
                buildingStoreBuilder.addBaseType(-1).addArtisansType(-1);
                buildingIds.put(buildingName, buildingStoreBuilder.getIndex());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return buildingStoreBuilder.build();
    }

    private void readResourceProductionsJson(GoodStore goodStore, ObjectIntMap<String> goodIds, ObjectIntMap<String> productionTypeIds) {
        try {
            JsonValue resourceProductionsValues = this.parseJsonFile(this.resourceProductionsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> resourceProductionsEntryIterator = resourceProductionsValues.objectIterator();
            while (resourceProductionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = resourceProductionsEntryIterator.next();
                int goodId = goodIds.get(entry.getKey());
                JsonValue productionValue = entry.getValue();
                int productionTypeId = productionTypeIds.get(productionValue.get("base_type").asString());
                goodStore.getProductionTypeIds().set(goodId, productionTypeId);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readMinisterTypesJson(World ecsWorld) {
        try {
            JsonValue ministerTypesValues = this.parseJsonFile(this.ministerTypesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> ministerTypesEntryIterator = ministerTypesValues.objectIterator();
            while (ministerTypesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = ministerTypesEntryIterator.next();
                String ministerTypeName = entry.getKey();
                long ministerTypeEntityId = ecsWorld.entity(ministerTypeName);
                Entity ministerTypeEntity = ecsWorld.obtainEntity(ministerTypeEntityId);
                Iterator<Map.Entry<String, JsonValue>> modifierEntryIterator = entry.getValue().objectIterator();
                while (modifierEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifierEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    float modifierValue = (float) modifierEntry.getValue().asDouble();
                    long modifierTagId = ecsWorld.entity(modifierName);
                    ministerTypeEntity.set(new Modifier(modifierValue), modifierTagId);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readTerrainsJson(World ecsWorld) {
        try {
            JsonValue terrainsValues = this.parseJsonFile(this.terrainJsonFile);
            Iterator<Map.Entry<String, JsonValue>> terrainsEntryIterator = terrainsValues.objectIterator();
            while (terrainsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = terrainsEntryIterator.next();
                String terrainName = entry.getKey();
                JsonValue terrainValue = entry.getValue();
                byte movementCost = (byte) terrainValue.get("movement_cost").asLong();
                byte humidity = (byte) terrainValue.get("humidity").asLong();
                byte temperature = (byte) terrainValue.get("temperature").asLong();
                byte precipitation = (byte) terrainValue.get("precipitation").asLong();
                int color = this.parseColor(terrainValue.get("color"));

                long entityTerrainId = ecsWorld.entity(terrainName);
                Entity entityTerrain = ecsWorld.obtainEntity(entityTerrainId);
                entityTerrain.set(new Terrain(movementCost, temperature, humidity, precipitation, color));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLawsJson(World ecsWorld) {
        try {
            JsonValue lawsValues = this.parseJsonFile(this.lawsJsonFile);
            int baseEnactmentDaysLaw = (int) lawsValues.get("base_enactment_days").asLong();
            Iterator<Map.Entry<String, JsonValue>> lawGroupsEntryIterator = lawsValues.get("groups").objectIterator();
            long lawGroupTagId = ecsWorld.lookup(EcsConstants.EcsLawGroupTag);
            long belongsToId = ecsWorld.lookup(EcsConstants.EcsBelongsTo);
            long supportedById = ecsWorld.lookup(EcsConstants.EcsSupportedBy);
            long opposedById = ecsWorld.lookup(EcsConstants.EcsOpposedBy);
            while (lawGroupsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> lawGroupEntry = lawGroupsEntryIterator.next();
                String name = lawGroupEntry.getKey();
                JsonValue lawGroupValue = lawGroupEntry.getValue();
                int factorEnactmentDays = (int) lawGroupValue.get("factor_enactment_days").asLong();
                int enactmentDuration = baseEnactmentDaysLaw * factorEnactmentDays;
                long lawGroupEntityId = ecsWorld.entity(name);
                Entity lawGroupEntity = ecsWorld.obtainEntity(lawGroupEntityId);
                lawGroupEntity.add(lawGroupTagId);
                lawGroupEntity.set(new EnactmentDuration(enactmentDuration));
                Iterator<Map.Entry<String, JsonValue>> lawsEntryIterator = lawGroupValue.get("laws").objectIterator();
                while (lawsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> lawEntry = lawsEntryIterator.next();
                    String lawName = lawEntry.getKey();
                    long lawEntityId = ecsWorld.entity(lawName);
                    Entity lawEntity = ecsWorld.obtainEntity(lawEntityId);
                    lawEntity.addRelation(belongsToId, lawGroupEntityId);
                    JsonValue lawValue = lawEntry.getValue();
                    Iterator<JsonValue> requirementsIterator = lawValue.get("requirements").arrayIterator();
                    while (requirementsIterator.hasNext()) {
                        // TODO associate requirements with law
                    }
                    Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = lawValue.get("modifiers").objectIterator();
                    while (modifiersEntryIterator.hasNext()) {
                        Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                        String modifierName = modifierEntry.getKey();
                        float modifierValue = (float) modifierEntry.getValue().asDouble();
                        long modifierTagId = ecsWorld.entity(modifierName);
                        lawEntity.set(new Modifier(modifierValue), modifierTagId);
                    }
                    Iterator<Map.Entry<String, JsonValue>> interestIdeologiesEntryIterator = lawValue.get("interest_ideologies").objectIterator();
                    while (interestIdeologiesEntryIterator.hasNext()) {
                        Map.Entry<String, JsonValue> entry = interestIdeologiesEntryIterator.next();
                        long ideologyId = ecsWorld.lookup(entry.getKey());
                        int value = (int) entry.getValue().asLong();
                        if(value > 0) {
                            lawEntity.addRelation(supportedById, ideologyId);
                        } else if (value < 0) {
                            lawEntity.addRelation(opposedById, ideologyId);
                        }
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readTraitsJson(World ecsWorld) {
        try {
            JsonValue traitsValues = this.parseJsonFile(this.traitsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> traitsEntryIterator = traitsValues.objectIterator();
            while (traitsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = traitsEntryIterator.next();
                String traitName = entry.getKey();
                JsonValue traitValue = entry.getValue();
                Map.Entry<String, JsonValue> modifierEntry = traitValue.objectIterator().next();
                String modifierName = modifierEntry.getKey();
                float modifierValue = (float) modifierEntry.getValue().asDouble();
                long modifierTagId = ecsWorld.entity(modifierName);
                long traitEntityId = ecsWorld.entity(traitName);
                Entity traitEntity = ecsWorld.obtainEntity(traitEntityId);
                traitEntity.set(new Modifier(modifierValue), modifierTagId);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Map<String, Country> loadCountries(World ecsWorld) {
        Map<String, Country> countries = this.readCountriesJson(ecsWorld);
        this.readRelationJson(ecsWorld);
        this.readAlliancesJson(ecsWorld);
        this.readLeadersJson(ecsWorld, countries);
        return countries;
    }

    private Map<String, Country> readCountriesJson(World ecsWorld) {
        Map<String, Country> countries = new ObjectObjectMap<>(262, 1f);
        Map<String, String> countriesPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesValues = this.parseJsonFile(this.countriesJsonFiles);
            Iterator<Map.Entry<String, JsonValue>> countriesEntryIterator = countriesValues.objectIterator();
            while (countriesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = countriesEntryIterator.next();
                countriesPaths.put(entry.getKey(), this.commonPath + entry.getValue().asString());
            }

            for (Map.Entry<String, String> entry : countriesPaths.entrySet()) {
                Country country = this.readCountryJson(ecsWorld, entry.getValue(), entry.getKey());
                countries.put(entry.getKey(), country);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return countries;
    }

    private Country readCountryJson(World ecsWorld, String countryPath, String countryId) {
        try {
            JsonValue countryValues = this.parseJsonFile(countryPath);

            long countryTagId = ecsWorld.lookup(EcsConstants.EcsCountryTag);

            Country country = new Country(countryId);
            int color = this.parseColor(countryValues.get("color"));
            long countryEntityId = ecsWorld.entity(countryId);
            Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
            countryEntity.add(countryTagId);
            countryEntity.set(new Color(color));

            long alignedWithId = ecsWorld.lookup(EcsConstants.EcsAlignedWith);

            JsonValue ministersValues = countryValues.get("ministers");
            if (ministersValues != null && ministersValues.isObject()) {
                LongList countryMinisterIds = new LongList();
                Iterator<Map.Entry<String, JsonValue>> ministersEntryIterator = ministersValues.objectIterator();
                while (ministersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> ministerEntry = ministersEntryIterator.next();
                    short ministerId = Short.parseShort(ministerEntry.getKey());
                    JsonValue ministerNode = ministerEntry.getValue();
                    String name = ministerNode.get("name").asString();
                    String ideology = ministerNode.get("ideology").asString();
                    float loyalty = (float) ministerNode.get("loyalty").asDouble();
                    String imageNameFile = ministerNode.get("picture").asString();
                    String type = ministerNode.get("type").asString();
                    int startDate = (int) LocalDate.parse(ministerNode.get("start_date").asString(), this.dateFormatter).toEpochDay();
                    int deathDate = (int) LocalDate.parse(ministerNode.get("death_date").asString(), this.dateFormatter).toEpochDay();

                    long ideologyEntityId = ecsWorld.entity(ideology);
                    long typeEntityId = ecsWorld.entity(type);
                    long ministerEntityId = ecsWorld.entity(String.valueOf(ministerId));
                    Entity ministerEntity = ecsWorld.obtainEntity(ministerEntityId);
                    ministerEntity.addRelation(alignedWithId, ideologyEntityId);
                    ministerEntity.isA(typeEntityId);
                    ministerEntity.set(new Minister(name, imageNameFile, loyalty, startDate, deathDate));
                    countryMinisterIds.add(ministerEntity.id());
                }
                country.setMinisterIds(countryMinisterIds);
            }
            return country;
        } catch (DateTimeParseException dateTimeParseException) {
            dateTimeParseException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void readRelationJson(World ecsWorld) {
        try {
            JsonValue relationsValues = this.parseJsonFile(this.relationJsonFile);
            Iterator<JsonValue> relationsIterator = relationsValues.get("relation").arrayIterator();
            while (relationsIterator.hasNext()) {
                JsonValue relation = relationsIterator.next();
                String countryNameId1 = relation.get("country1").asString();
                Entity country1 = ecsWorld.obtainEntity(ecsWorld.lookup(countryNameId1));
                String countryNameId2 = relation.get("country2").asString();
                Entity country2 = ecsWorld.obtainEntity(ecsWorld.lookup(countryNameId2));
                int relationValue = (int) relation.get("value").asLong();
                country1.set(new DiplomaticRelation(relationValue), country2.id());
                country2.set(new DiplomaticRelation(relationValue), country1.id());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAlliancesJson(World ecsWorld) {
        try {
            JsonValue alliancesValues = this.parseJsonFile(this.alliancesJsonFile);
            Iterator<JsonValue> alliancesIterator = alliancesValues.get("alliances").arrayIterator();
            while (alliancesIterator.hasNext()) {
                JsonValue alliance = alliancesIterator.next();
                String countryNameId1 = alliance.get("country1").asString();
                Entity country1 = ecsWorld.obtainEntity(ecsWorld.entity(countryNameId1));
                String countryNameId2 = alliance.get("country2").asString();
                Entity country2 = ecsWorld.obtainEntity(ecsWorld.entity(countryNameId2));
                String type = alliance.get("type").asString();
                long relationId1 = ecsWorld.lookup(EcsConstants.getAllianceRelation(type, true));
                country1.addRelation(relationId1, country2.id());
                long relationId2 = ecsWorld.lookup(EcsConstants.getAllianceRelation(type, false));
                country2.addRelation(relationId2, country1.id());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLeadersJson(World ecsWorld, Map<String, Country> countries) {
        Map<String, String> leadersPaths = new ObjectObjectMap<>();
        try {
            JsonValue leadersValues = this.parseJsonFile(this.leadersJsonFiles);
            Iterator<Map.Entry<String, JsonValue>> leadersEntryIterator = leadersValues.objectIterator();
            while (leadersEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = leadersEntryIterator.next();
                leadersPaths.put(entry.getKey(), this.historyPath + entry.getValue().asString());
            }

            for (Map.Entry<String, String> entry : leadersPaths.entrySet()) {
                this.readLeaderJson(ecsWorld, entry.getValue(), countries);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLeaderJson(World ecsWorld, String filename, Map<String, Country> countries) {
        try {
            JsonValue leaderValues = this.parseJsonFile(filename);
            String countryId = leaderValues.get("country").asString();
            long hasId = ecsWorld.lookup(EcsConstants.EcsHas);
            Iterator<Map.Entry<String, JsonValue>> leadersEntryIterator = leaderValues.get("leaders").objectIterator();
            IntList leaderIds = new IntList();
            while (leadersEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = leadersEntryIterator.next();
                String leaderId = entry.getKey();
                JsonValue leaderValue = entry.getValue();
                String name = leaderValue.get("name").asString();
                byte skill = (byte) leaderValue.get("skill").asLong();
                byte forceType = ForceType.fromString(leaderValue.get("force_type").asString());
                long traitId = ecsWorld.lookup(leaderValue.get("trait").asString());
                long leaderEntityId = ecsWorld.entity(leaderId);
                Entity leaderEntity = ecsWorld.obtainEntity(leaderEntityId);
                leaderEntity.set(new Leader(name, skill, forceType));
                leaderEntity.addRelation(hasId, traitId);
            }
            countries.get(countryId).setLeadersIds(leaderIds);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private ProvinceStore loadProvinces(World ecsWorld, RegionStoreBuilder regionStoreBuilder, Map<String, Country> countries, IntLongMap provinces, ObjectIntMap<String> goodIds, ObjectIntMap<String> buildingIds, ObjectIntMap<String> populationTypeIds, Borders borders) {
        IntObjectMap<IntIntMap> regionBuildingsByProvince = new IntObjectMap<>(396, 1f);
        PopulationTemplateStore populationTemplateStore = this.readPopulationTemplatesJson();
        ProvinceStore provinceStore = this.readProvincesJson(ecsWorld, countries, regionBuildingsByProvince, populationTemplateStore, goodIds, buildingIds, populationTypeIds);
        this.readRegionJson(ecsWorld, regionBuildingsByProvince, regionStoreBuilder);
        this.readDefinitionCsv(ecsWorld, provinces, provinceStore);
        this.readProvinceBitmap(ecsWorld, provinces, borders);
        this.readCountriesHistoryJson(ecsWorld, countries);
        this.readContinentJsonFile(ecsWorld);
        this.readAdjenciesJson(ecsWorld);
        this.readPositionsJson(ecsWorld);
        return provinceStore;
    }

    private PopulationTemplateStore readPopulationTemplatesJson() {
        PopulationTemplateStoreBuilder builder = new PopulationTemplateStoreBuilder();
        try {
            JsonValue populationTemplatesValues = this.parseJsonFile(this.populationTemplatesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> populationTemplatesIterator = populationTemplatesValues.objectIterator();
            while (populationTemplatesIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = populationTemplatesIterator.next();
                short template = Short.parseShort(entry.getKey());
                JsonValue templateValue = entry.getValue();
                Iterator<JsonValue> populationValuesIterator = templateValue.get("value").arrayIterator();
                float children = (float) populationValuesIterator.next().asDouble();
                float adults = (float) populationValuesIterator.next().asDouble();
                float seniors = (float) populationValuesIterator.next().asDouble();
                builder.add(template, children, adults, seniors);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return builder.build();
    }

    private ProvinceStore readProvincesJson(World ecsWorld, Map<String, Country> countries, IntObjectMap<IntIntMap> regionBuildingsByProvince, PopulationTemplateStore populationTemplateStore, ObjectIntMap<String> goodIds, ObjectIntMap<String> buildingIds, ObjectIntMap<String> populationTypeIds) {
        ProvinceStoreBuilder builder = new ProvinceStoreBuilder();
        IntObjectMap<String> provincesPaths = new IntObjectMap<>(builder.getDefaultCapacity(), 1f);
        try {
            JsonValue provincesValues = this.parseJsonFile(this.provincesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> provincesEntryIterator = provincesValues.objectIterator();
            while (provincesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = provincesEntryIterator.next();
                provincesPaths.put(Short.parseShort(entry.getKey()), this.historyPath + entry.getValue().asString());
            }

            for (IntObjectMap.Entry<String> entry : provincesPaths.entrySet()) {
                short provinceId = (short) entry.getKey();
                String provincePath = entry.getValue();
                this.readProvinceJson(ecsWorld, provincePath, provinceId, regionBuildingsByProvince, populationTemplateStore, goodIds, buildingIds, populationTypeIds, builder);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return builder.build();
    }

    private void readProvinceJson(World ecsWorld, String provincePath, short provinceId, IntObjectMap<IntIntMap> regionBuildingsByProvince, PopulationTemplateStore populationTemplateStore, ObjectIntMap<String> goodIds, ObjectIntMap<String> buildingIds, ObjectIntMap<String> populationTypeIds, ProvinceStoreBuilder builder) {
        try {
            JsonValue provinceValues = this.parseJsonFile(provincePath);

            long hasTerrainId = ecsWorld.lookup(EcsConstants.EcsHasTerrain);
            long hasId = ecsWorld.lookup(EcsConstants.EcsHas);

            long coreOfId = ecsWorld.lookup(EcsConstants.EcsCoreOf);
            long ownedById = ecsWorld.lookup(EcsConstants.EcsOwnedBy);
            long controlledById = ecsWorld.lookup(EcsConstants.EcsControlledBy);

            long provinceEntityId = ecsWorld.entity(String.valueOf(provinceId));
            Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);

            JsonValue addCoreValue = provinceValues.get("add_core");
            if (addCoreValue.isArray()) {
                Iterator<JsonValue> addCoreIterator = addCoreValue.arrayIterator();
                while (addCoreIterator.hasNext()) {
                    JsonValue countryCore = addCoreIterator.next();
                    long countryCoreId = ecsWorld.lookup(countryCore.asString());
                    provinceEntity.addRelation(coreOfId, countryCoreId);
                }
            } else {
                long countryCoreId = ecsWorld.lookup(addCoreValue.asString());
                provinceEntity.addRelation(coreOfId, countryCoreId);
            }

            String owner = provinceValues.get("owner").asString();
            Entity countryOwner = ecsWorld.obtainEntity(ecsWorld.lookup(owner));
            provinceEntity.addRelation(ownedById, countryOwner.id());
            countryOwner.addRelation(hasId, provinceEntity.id());

            String controller = provinceValues.get("controller").asString();
            long countryControllerId = ecsWorld.lookup(controller);
            provinceEntity.addRelation(controlledById, countryControllerId);

            String terrain = provinceValues.get("terrain").asString();
            long terrainId = ecsWorld.lookup(terrain);
            provinceEntity.addRelation(hasTerrainId, terrainId);

            JsonValue populationValue = provinceValues.get("population_total");
            int amount = (int) populationValue.get("amount").asLong();
            short template = (short) populationValue.get("template").asLong();
            int populationTemplateIndex = populationTemplateStore.getIndexById().get(template);
            int amountChildren = (int) (amount * populationTemplateStore.getChildren().get(populationTemplateIndex));
            int amountSeniors = (int) (amount * populationTemplateStore.getSeniors().get(populationTemplateIndex));
            int amountAdults = (int) (amount * populationTemplateStore.getAdults().get(populationTemplateIndex));
            builder.addProvince(provinceId).addAmountPopulation(amountChildren, amountAdults, amountSeniors);

            this.parseDistribution(ecsWorld, populationValue.get("populations"), amountAdults, populationTypeIds, builder, "population");
            this.parseDistribution(ecsWorld, populationValue.get("cultures"), amountAdults, null, builder, "culture");
            this.parseDistribution(ecsWorld, populationValue.get("religions"), amountAdults, null, builder, "religion");

            JsonValue buildingsValue = provinceValues.get("economy_buildings");
            if(buildingsValue != null) {
                IntIntMap buildings = new IntIntMap();
                Iterator<JsonValue> buildingsIterator = buildingsValue.arrayIterator();
                while (buildingsIterator.hasNext()) {
                    JsonValue building = buildingsIterator.next();
                    String buildingName = building.get("name").asString();
                    short size = (short) building.get("size").asLong();
                    buildings.put(buildingIds.get(buildingName), size);
                }
                regionBuildingsByProvince.put(provinceId, buildings);
            }

            int resourceGoodId = -1;
            JsonValue goodValue = provinceValues.get("good");
            if(goodValue != null) {
                resourceGoodId = goodIds.get(goodValue.asString());
            }
            builder.addResourceGood(resourceGoodId);

            JsonValue buildingsProvinceValue = provinceValues.get("buildings");
            if(buildingsProvinceValue != null) {
                Iterator<JsonValue> buildingsProvinceIterator = buildingsProvinceValue.arrayIterator();
                while (buildingsProvinceIterator.hasNext()) {
                    JsonValue building = buildingsProvinceIterator.next();
                    String buildingName = building.get("name").asString();
                    short size = (short) building.get("size").asLong();
                    int buildingId = buildingIds.get(buildingName);
                    builder.addBuilding(buildingId, size);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void parseDistribution(World ecsWorld, JsonValue distributionValue, int amountAdults, ObjectIntMap<String> idsMap, ProvinceStoreBuilder builder, String distributionType) {
        if (distributionValue == null) {
            return;
        }

        Iterator<Map.Entry<String, JsonValue>> distributionEntryIterator = distributionValue.objectIterator();
        while (distributionEntryIterator.hasNext()) {
            Map.Entry<String, JsonValue> distributionEntry = distributionEntryIterator.next();
            String name = distributionEntry.getKey();
            float percentage = (float) distributionEntry.getValue().asDouble();
            int value = (int) (amountAdults * percentage);

            switch (distributionType) {
                case "population" -> {
                    int id = idsMap.get(name);
                    builder.addPopulationType(id, value);
                }
                case "culture" -> {
                    long id = ecsWorld.lookup(name);
                    builder.addCulture(id, value);
                }
                case "religion" -> {
                    long id = ecsWorld.lookup(name);
                    builder.addReligion(id, value);
                }
            }
        }
    }

    private void readRegionJson(World ecsWorld, IntObjectMap<IntIntMap> regionBuildingsByProvince, RegionStoreBuilder regionStoreBuilder) {
        try {
            JsonValue regionValue = this.parseJsonFile(this.regionJsonFiles);
            long controlledById = ecsWorld.lookup(EcsConstants.EcsControlledBy);
            long regionTagId = ecsWorld.lookup(EcsConstants.EcsRegionTag);
            long locatedInRegionId = ecsWorld.lookup(EcsConstants.EcsLocatedInRegion);
            long hasId = ecsWorld.lookup(EcsConstants.EcsHas);
            long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
            long seaProvinceTagId = ecsWorld.lookup(EcsConstants.EcsSeaProvinceTag);
            Iterator<Map.Entry<String, JsonValue>> regionEntryIterator = regionValue.objectIterator();
            while (regionEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = regionEntryIterator.next();
                String regionId = entry.getKey();
                long regionEntityId = ecsWorld.entity(regionId);
                Entity regionEntity = ecsWorld.obtainEntity(regionEntityId);
                regionEntity.add(regionTagId);
                regionStoreBuilder.addRegion(regionId);
                Iterator<JsonValue> regionIterator = entry.getValue().arrayIterator();
                while (regionIterator.hasNext()) {
                    short provinceId = (short) regionIterator.next().asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    if(provinceEntityId != -1) {
                        Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                        provinceEntity.add(landProvinceTagId);
                        long countryEntityId = provinceEntity.target(controlledById);
                        if (countryEntityId != 0) {
                            Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
                            countryEntity.addRelation(hasId, regionEntityId);
                        }
                        provinceEntity.addRelation(locatedInRegionId, regionEntityId);
                        regionEntity.addRelation(hasId, provinceEntityId);
                        IntIntMap regionBuildingIds = regionBuildingsByProvince.get(provinceId);
                        if(regionBuildingIds != null) {
                            for(IntIntMap.Entry buildingEntry : regionBuildingIds) {
                                int buildingId = buildingEntry.key;
                                int size = buildingEntry.value;
                                regionStoreBuilder.addBuilding(buildingId, size);
                            }
                        }
                    } else {
                        provinceEntityId = ecsWorld.entity(String.valueOf(provinceId));
                        Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                        provinceEntity.add(seaProvinceTagId);
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readDefinitionCsv(World ecsWorld, IntLongMap provinces, ProvinceStore provinceStore) {
        try (BufferedReader bufferedReader = this.parseCsvFile(this.definitionCsvFile)) {
            long landProvinceTagId = ecsWorld.lookup(EcsConstants.EcsLandProvinceTag);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    short provinceId = Short.parseShort(values[0]);
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                    int red = Integer.parseInt(values[1]);
                    int green = Integer.parseInt(values[2]);
                    int blue = Integer.parseInt(values[3]);
                    int alpha = 255;

                    int color =  (red << 24) | (green << 16) | (blue << 8) | alpha;
                    provinceEntity.set(new Color(color));
                    provinces.put(color, provinceEntityId);
                    if (provinceEntity.has(landProvinceTagId)) {
                        int provinceIndex = provinceStore.getIndexById().get(provinceId);
                        provinceStore.getColors().set(provinceIndex, color);
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readProvinceBitmap(World ecsWorld, IntLongMap provinces, Borders borders) {
        Pixmap provincesPixmap = new Pixmap(Gdx.files.internal(this.mapPath + "provinces.bmp"));

        IntList xyValues = new IntList();
        LongObjectMap<IntList> temporaryGroups = new LongObjectMap<>();

        short height = (short) provincesPixmap.getHeight();
        short width = (short) provincesPixmap.getWidth();

        for (short y = 0; y < height; y++) {
            for (short x = 0; x < width; x++) {
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
            Entity provinceEntity = ecsWorld.obtainEntity(entry.key);
            provinceEntity.set(new Border(startIndex, endIndex));
        }
        borders.setPixels(xyValues.shrink());

        provincesPixmap.dispose();
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

    private void readCountriesHistoryJson(World ecsWorld, Map<String, Country> countries) {
        ObjectObjectMap<String, String> countriesHistoryPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesJson = this.parseJsonFile(this.countriesHistoryJsonFiles);
            Iterator<Map.Entry<String, JsonValue>> countriesEntryIterator = countriesJson.objectIterator();
            while (countriesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = countriesEntryIterator.next();
                countriesHistoryPaths.put(entry.getKey(), this.historyPath + entry.getValue().asString());
            }
            for (Map.Entry<String, String> entry : countriesHistoryPaths) {
                String countryId = entry.getKey();
                String countryFileName = entry.getValue();
                this.readCountryHistoryJson(ecsWorld, countries, countryFileName, countryId);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readCountryHistoryJson(World ecsWorld, Map<String, Country> countries, String countryFileName, String idCountry) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            long alignedWithId = ecsWorld.lookup(EcsConstants.EcsAlignedWith);
            long hasCapitalId = ecsWorld.lookup(EcsConstants.EcsHasCapital);
            Entity countryEntity = ecsWorld.obtainEntity(ecsWorld.lookup(idCountry));

            JsonValue countryValues = this.parseJsonFile(countryFileName);
            short idCapital = (short) countryValues.get("capital").asLong();
            Country country = countries.get(idCountry);
            long capitalId = ecsWorld.lookup(String.valueOf(idCapital));
            countryEntity.addRelation(hasCapitalId, capitalId);
            String government = countryValues.get("government").asString();
            long governmentId = ecsWorld.lookup(government);
            country.setGovernmentId(governmentId);
            String ideology = countryValues.get("ideology").asString();
            long ideologyId = ecsWorld.lookup(ideology);
            countryEntity.addRelation(alignedWithId, ideologyId);
            String identity = countryValues.get("national_identity").asString();
            long identityId = ecsWorld.lookup(identity);
            country.setIdentityId(identityId);
            String attitude = countryValues.get("national_attitude").asString();
            long attitudeId = ecsWorld.lookup(attitude);
            country.setAttitudeId(attitudeId);
            if(countryValues.get("head_of_state") != null && countryValues.get("head_of_government") != null) {
                short ministerHeadOfStateId = (short) countryValues.get("head_of_state").asLong();
                short ministerHeadOfGovernmentId = (short) countryValues.get("head_of_government").asLong();

                long ministerHeadOfStateEntityId = ecsWorld.lookup(String.valueOf(ministerHeadOfStateId));
                long ministerHeadOfGovernmentEntityId = ecsWorld.lookup(String.valueOf(ministerHeadOfGovernmentId));
                country.setHeadOfStateId(ministerHeadOfStateEntityId);
                country.setHeadOfGovernmentId(ministerHeadOfGovernmentEntityId);
            }
            LongList lawIds = new LongList();
            Iterator<Map.Entry<String, JsonValue>> lawsIterator = countryValues.get("laws").objectIterator();
            while(lawsIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = lawsIterator.next();
                String LawGroupName = entry.getKey();
                String lawName = entry.getValue().asString();
                long lawId = ecsWorld.lookup(lawName);
                lawIds.add(lawId);
            }
            country.setLawIds(lawIds);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readContinentJsonFile(World ecsWorld) {
        try {
            JsonValue continentValues = this.parseJsonFile(this.continentJsonFile);
            Iterator<Map.Entry<String, JsonValue>> continentEntryIterator = continentValues.objectIterator();
            long locatedInContinentId = ecsWorld.lookup(EcsConstants.EcsLocatedInContinent);
            while (continentEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = continentEntryIterator.next();
                String continentName = entry.getKey();
                long continentEntityId = ecsWorld.entity(continentName);
                Iterator<JsonValue> provincesIterator = entry.getValue().arrayIterator();
                while (provincesIterator.hasNext()) {
                    short provinceId = (short) provincesIterator.next().asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                    provinceEntity.addRelation(locatedInContinentId, continentEntityId);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAdjenciesJson(World ecsWorld) {
        try {
            JsonValue adjenciesValues = this.parseJsonFile(this.adjenciesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> adjenciesEntryIterator = adjenciesValues.objectIterator();
            long adjacentToId = ecsWorld.lookup(EcsConstants.EcsAdjacentTo);
            while (adjenciesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = adjenciesEntryIterator.next();
                short provinceId = Short.parseShort(entry.getKey());
                long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                Iterator<JsonValue> adjacenciesIterator = entry.getValue().arrayIterator();
                while (adjacenciesIterator.hasNext()) {
                    short adjacencyId = (short) adjacenciesIterator.next().asLong();
                    long adjacencyEntityId = ecsWorld.lookup(String.valueOf(adjacencyId));
                    provinceEntity.addRelation(adjacentToId, adjacencyEntityId);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readPositionsJson(World ecsWorld) {
        try {
            JsonValue positionsValues = this.parseJsonFile(this.positionsJsonFile);
            long hasId = ecsWorld.lookup(EcsConstants.EcsHas);
            long positionElementTagId = ecsWorld.lookup(EcsConstants.EcsPositionElementTag);
            Iterator<Map.Entry<String, JsonValue>> positionsEntryIterator = positionsValues.objectIterator();
            while (positionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = positionsEntryIterator.next();
                short provinceId = Short.parseShort(entry.getKey());
                long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                provinceEntity.add(positionElementTagId);
                Iterator<Map.Entry<String, JsonValue>> positionIterator = entry.getValue().objectIterator();
                while (positionIterator.hasNext()) {
                    Map.Entry<String, JsonValue> position = positionIterator.next();
                    String name = position.getKey();
                    JsonValue positionNode = position.getValue();
                    short x = (short) positionNode.get("x").asLong();
                    short y = (short) positionNode.get("y").asLong();
                    long positionEntityId = ecsWorld.entity("province_" + provinceId + "_pos_" + name);
                    Entity positionEntity = ecsWorld.obtainEntity(positionEntityId);
                    positionEntity.set(new Position(x, y));
                    provinceEntity.addRelation(hasId, positionEntity.id());
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
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
}

