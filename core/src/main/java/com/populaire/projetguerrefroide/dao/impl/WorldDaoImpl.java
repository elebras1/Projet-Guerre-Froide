package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.pojo.Borders;
import com.populaire.projetguerrefroide.pojo.WorldData;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.ForceTypeUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    public WorldDaoImpl() {

    }

    @Override
    public WorldData createWorld(GameContext gameContext) {
        World ecsWorld = gameContext.getEcsWorld();
        EcsConstants ecsConstants = gameContext.getEcsConstants();
        this.readIdeologiesJson(ecsWorld);
        this.readLawsJson(ecsWorld, ecsConstants);
        this.readGovernmentsJson(ecsWorld);
        this.readNationalIdeasJson(ecsWorld);
        this.readMinisterTypesJson(ecsWorld);
        this.readGoodsJson(ecsWorld, ecsConstants);
        this.readPopulationTypesJson(ecsWorld);
        this.readProductionTypesJson(ecsWorld);
        this.readBuildingsJson(ecsWorld, ecsConstants);
        this.readResourceProductionsJson(ecsWorld);
        this.readTraitsJson(ecsWorld);
        this.loadCountries(ecsWorld, ecsConstants);
        this.readTerrainsJson(ecsWorld);
        IntLongMap provinces = new IntLongMap(15000, 1f);
        Borders borders = new Borders();
        this.loadProvinces(ecsWorld, ecsConstants, provinces, borders);

        return new WorldData(provinces, borders);
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

            for(var cultureEntry : nationalIdeasValues.get("cultures").object()) {
                String name = cultureEntry.getKey();
                JsonValue cultureValue = cultureEntry.getValue();
                int color = this.parseColor(cultureValue.get("color"));
                long cultureEntityId = ecsWorld.entity(name);
                Entity cultureEntity = ecsWorld.obtainEntity(cultureEntityId);
                cultureEntity.set(new Color(color));
            }

            for(var religionEntry : nationalIdeasValues.get("religions").object()) {
                String name = religionEntry.getKey();
                JsonValue religionValue = religionEntry.getValue();
                int color = this.parseColor(religionValue.get("color"));
                long religionEntityId = ecsWorld.entity(name);
                Entity religionEntity = ecsWorld.obtainEntity(religionEntityId);
                religionEntity.set(new Color(color));
            }

            for(var identityEntry : nationalIdeasValues.get("national_identity").object()) {
                String name = identityEntry.getKey();
                long identityEntityId = ecsWorld.entity(name);
                Entity identityEntity = ecsWorld.obtainEntity(identityEntityId);
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
                Entity attitudeEntity = ecsWorld.obtainEntity(attitudeEntityId);
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readGovernmentsJson(World ecsWorld) {
        try {
            JsonValue governmentsValues = this.parseJsonFile(this.governmentJsonFile);

            for(var governmentEntry : governmentsValues.object()) {
                String governmentName = governmentEntry.getKey();
                JsonValue governmentValue = governmentEntry.getValue();

                long governmentEntityId = ecsWorld.entity(governmentName);
                Entity governmentEntity = ecsWorld.obtainEntity(governmentEntityId);

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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readIdeologiesJson(World ecsWorld) {
        try {
            JsonValue ideologiesValues = this.parseJsonFile(this.ideologiesJsonFile);
            for(var ideologyEntry : ideologiesValues.object()) {
                String ideologyName = ideologyEntry.getKey();
                JsonValue ideologyValue = ideologyEntry.getValue();
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

    private void readGoodsJson(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue goodsValues = this.parseJsonFile(this.goodsJsonFile);

            for(var resourceGoodEntry : goodsValues.get("resource_goods").object()) {
                String goodName = resourceGoodEntry.getKey();
                JsonValue goodValue = resourceGoodEntry.getValue();
                float cost = (float) goodValue.get("cost").asDouble();
                float value = (float) goodValue.get("value").asDouble();
                int color = this.parseColor(goodValue.get("color"));

                long goodId = ecsWorld.entity(goodName);
                Entity good = ecsWorld.obtainEntity(goodId);
                good.add(ecsConstants.ressourceGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, value));
            }

            for(var advancedGoodEntry : goodsValues.get("advanced_goods").object()) {
                String advancedGoodName = advancedGoodEntry.getKey();
                JsonValue advancedGoodValue = advancedGoodEntry.getValue();
                float cost = (float) advancedGoodValue.get("cost").asDouble();
                int color = this.parseColor(advancedGoodValue.get("color"));

                long goodId = ecsWorld.entity(advancedGoodName);
                Entity good = ecsWorld.obtainEntity(goodId);
                good.add(ecsConstants.advancedGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, -0));
            }

            for(var militaryGood : goodsValues.get("military_goods").object()) {
                String militaryGoodName = militaryGood.getKey();
                JsonValue militaryGoodNode = militaryGood.getValue();
                float cost = (float) militaryGoodNode.get("cost").asDouble();
                int color = this.parseColor(militaryGoodNode.get("color"));

                long goodId = ecsWorld.entity(militaryGoodName);
                Entity good = ecsWorld.obtainEntity(goodId);
                good.add(ecsConstants.militaryGoodTag());
                good.set(new Color(color));
                good.set(new Good(cost, 0));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readPopulationTypesJson(World ecsWorld) {
        Map<String, String> populationPaths = new ObjectObjectMap<>(12, 1f);
        try {
            JsonValue populationTypesValues = this.parseJsonFile(this.populationTypesJsonFile);
            for(var populationTypeEntry : populationTypesValues.object()) {
                populationPaths.put(populationTypeEntry.getKey(), this.commonPath + populationTypeEntry.getValue().asString());
            }

            for (Map.Entry<String, String> populationPath : populationPaths.entrySet()) {
                this.readPopulationTypeJson(ecsWorld, populationPath.getValue(), populationPath.getKey());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readPopulationTypeJson(World ecsWorld, String populationTypePath, String name) {
        try {
            JsonValue populationTypeValue = this.parseJsonFile(populationTypePath);
            int color = this.parseColor(populationTypeValue.get("color"));
            long populationTypeId = ecsWorld.entity(name);
            Entity populationType = ecsWorld.obtainEntity(populationTypeId);
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
            }
            populationType.set(new PopulationType(standardDemandGoodIds, standardDemandGoodValues, luxuryDemandGoodIds, luxuryDemandGoodValues));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readProductionTypesJson(World ecsWorld) {
        try {
            JsonValue buildingTypesJson = this.parseJsonFile(this.productionTypesJsonFile);
            for(var typeEmployeeEntry : buildingTypesJson.get("types_employees").object()) {
                String typeName = typeEmployeeEntry.getKey();
                JsonValue typeEmployeeValue = typeEmployeeEntry.getValue();
                long populationTypeId = ecsWorld.lookup(typeEmployeeValue.get("poptype").asString());
                float amount = (float) typeEmployeeValue.get("amount").asDouble();
                float effectMultiplier = (float) typeEmployeeValue.get("effect_multiplier").asDouble();
                Entity employee = ecsWorld.obtainEntity(ecsWorld.entity(typeName));
                employee.set(new EmployeeType(populationTypeId, amount, effectMultiplier));
            }

            for(var typeBuildingEntry : buildingTypesJson.get("types_buildings").object()) {
                String typeName = typeBuildingEntry.getKey();
                Entity buildingTypeEntity = ecsWorld.obtainEntity(ecsWorld.entity(typeName));
                JsonValue typeBuildingValue = typeBuildingEntry.getValue();
                int workforce = (int) typeBuildingValue.get("workforce").asLong();
                long ownerId = ecsWorld.lookup(typeBuildingValue.get("owner").get("poptype").asString());

                long[] employeeIds = new long[4];
                int i = 0;
                for(var employeeValue : typeBuildingValue.get("employees").array()) {
                    String employeeName = employeeValue.asString();
                    long employeeId = ecsWorld.lookup(employeeName);
                    employeeIds[i] = employeeId;
                    i++;
                }
                buildingTypeEntity.set(new ProductionType(workforce, ownerId, employeeIds));
            }

            for(var typeRgoEntry: buildingTypesJson.get("types_rgo").object()) {
                String typeName = typeRgoEntry.getKey();
                Entity buildingTypeEntity = ecsWorld.obtainEntity(ecsWorld.entity(typeName));
                JsonValue typeRgoValue = typeRgoEntry.getValue();
                int workforce = (int) typeRgoValue.get("workforce").asLong();
                long ownerId = ecsWorld.lookup(typeRgoValue.get("owner").get("poptype").asString());

                long[] employeeIds = new long[4];
                int i = 0;
                for(var employeeValue : typeRgoValue.get("employees").array()) {
                    String employeeName = employeeValue.asString();
                    long employeeId = ecsWorld.lookup(employeeName);
                    employeeIds[i] = employeeId;
                    i++;
                }
                buildingTypeEntity.set(new ProductionType(workforce, ownerId, employeeIds));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readBuildingsJson(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue buildingsValues = this.parseJsonFile(this.buildingsJsonFile);
            for(var economyBuilding : buildingsValues.get("economy_building").object()) {
                String buildingName = economyBuilding.getKey();
                Entity building = ecsWorld.obtainEntity(ecsWorld.entity(buildingName));
                JsonValue buildingValue = economyBuilding.getValue();
                int time = (int) buildingValue.get("time").asLong();
                long buildingTypeId = ecsWorld.lookup(buildingValue.get("base_type").asString());
                long artisansTypeId = 0;
                if(buildingValue.get("artisans_type") != null) {
                    artisansTypeId = ecsWorld.lookup(buildingValue.get("artisans_type").asString());
                }
                byte maxLevel = (byte) buildingValue.get("max_level").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostValues = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodValue = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostValues[goodCostIndex] = goodValue;
                    goodCostIndex++;
                }
                long[] inputGoodIds = new long[8];
                float[] inputGoodValues = new float[8];
                int inputGoodIndex = 0;
                for(var inputGoodEntry : buildingValue.get("input_goods").object()) {
                    long goodId = ecsWorld.lookup(inputGoodEntry.getKey());
                    float goodValue = (float) inputGoodEntry.getValue().asDouble();
                    inputGoodIds[inputGoodIndex] = goodId;
                    inputGoodValues[inputGoodIndex] = goodValue;
                    inputGoodIndex++;
                }
                long outputGoodId = 0;
                float outputGoodValue = 0;
                Iterator<Map.Entry<String, JsonValue>> outputGoodsEntryIterator = buildingValue.get("output_goods").objectIterator();
                if (outputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> outputGood = outputGoodsEntryIterator.next();
                    long goodId = ecsWorld.lookup(outputGood.getKey());
                    float goodValue = (float) outputGood.getValue().asDouble();
                    outputGoodId = goodId;
                    outputGoodValue = goodValue;
                }
                building.set(new EconomyBuilding(time, buildingTypeId, artisansTypeId, maxLevel, goodCostIds, goodCostValues, inputGoodIds, inputGoodValues, outputGoodId, outputGoodValue));
            }

            for(var specialBuildingEntry : buildingsValues.get("special_building").object()) {
                String buildingName = specialBuildingEntry.getKey();
                Entity building = ecsWorld.obtainEntity(ecsWorld.entity(buildingName));
                JsonValue buildingValue = specialBuildingEntry.getValue();
                int time = (int) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostValues = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodValue = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostValues[goodCostIndex] = goodValue;
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
                building.set(new SpecialBuilding(time, cost, goodCostIds, goodCostValues));
            }

            for(var developmentBuildingEntry : buildingsValues.get("development_building").object()) {
                String buildingName = developmentBuildingEntry.getKey();
                Entity building = ecsWorld.obtainEntity(ecsWorld.entity(buildingName));
                JsonValue buildingValue = developmentBuildingEntry.getValue();
                int time = (int) buildingValue.get("time").asLong();
                int cost = (int) buildingValue.get("cost").asLong();
                long[] goodCostIds = new long[8];
                float[] goodCostValues = new float[8];
                int goodCostIndex = 0;
                for(var goodCostEntry : buildingValue.get("goods_cost").object()) {
                    long goodId = ecsWorld.lookup(goodCostEntry.getKey());
                    float goodValue = (float) goodCostEntry.getValue().asDouble();
                    goodCostIds[goodCostIndex] = goodId;
                    goodCostValues[goodCostIndex] = goodValue;
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
                building.set(new DevelopmentBuilding(time, cost, goodCostIds, goodCostValues, maxLevel));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readResourceProductionsJson(World ecsWorld) {
        try {
            JsonValue resourceProductionsValues = this.parseJsonFile(this.resourceProductionsJsonFile);
            for(var resourceProductionEntry : resourceProductionsValues.object()) {
                long goodId = ecsWorld.lookup(resourceProductionEntry.getKey());
                Entity good = ecsWorld.obtainEntity(goodId);
                JsonValue productionValue = resourceProductionEntry.getValue();
                long productionTypeId = ecsWorld.lookup(productionValue.get("base_type").asString());
                good.set(new ResourceProduction(productionTypeId));
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
            for(var ministerTypeEntry : ministerTypesValues.object()) {
                long ministerTypeEntityId = ecsWorld.entity(ministerTypeEntry.getKey());
                Entity ministerTypeEntity = ecsWorld.obtainEntity(ministerTypeEntityId);
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readTerrainsJson(World ecsWorld) {
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
                Entity entityTerrain = ecsWorld.obtainEntity(entityTerrainId);
                entityTerrain.set(new Terrain(movementCost, temperature, humidity, precipitation, color));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLawsJson(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue lawsValues = this.parseJsonFile(this.lawsJsonFile);
            int baseEnactmentDaysLaw = (int) lawsValues.get("base_enactment_days").asLong();
            for(var lawGroupEntry : lawsValues.get("groups").object()) {
                String name = lawGroupEntry.getKey();
                JsonValue lawGroupValue = lawGroupEntry.getValue();
                int factorEnactmentDays = (int) lawGroupValue.get("factor_enactment_days").asLong();
                int enactmentDuration = baseEnactmentDaysLaw * factorEnactmentDays;
                long lawGroupEntityId = ecsWorld.entity(name);
                Entity lawGroupEntity = ecsWorld.obtainEntity(lawGroupEntityId);
                lawGroupEntity.add(ecsConstants.lawGroupTag());
                lawGroupEntity.set(new EnactmentDuration(enactmentDuration));
                for(var lawEntry : lawGroupValue.get("laws").object()) {
                    String lawName = lawEntry.getKey();
                    long lawEntityId = ecsWorld.entity(lawName);
                    Entity lawEntity = ecsWorld.obtainEntity(lawEntityId);
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readTraitsJson(World ecsWorld) {
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
                Entity traitEntity = ecsWorld.obtainEntity(traitEntityId);
                float[] modifierValues = new float[8];
                long[] modifierTagIds = new long[8];
                modifierValues[0] = modifierValue;
                modifierTagIds[0] = modifierTagId;
                traitEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadCountries(World ecsWorld, EcsConstants ecsConstants) {
        this.readCountriesJson(ecsWorld, ecsConstants);
        this.readRelationJson(ecsWorld);
        this.readAlliancesJson(ecsWorld, ecsConstants);
        this.readLeadersJson(ecsWorld);
    }

    private void readCountriesJson(World ecsWorld, EcsConstants ecsConstants) {
        Map<String, String> countriesPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesValues = this.parseJsonFile(this.countriesJsonFiles);
            for(var countryEntry: countriesValues.object()) {
                countriesPaths.put(countryEntry.getKey(), this.commonPath + countryEntry.getValue().asString());
            }

            for (Map.Entry<String, String> countryEntry : countriesPaths.entrySet()) {
                this.readCountryJson(ecsWorld, ecsConstants, countryEntry.getValue(), countryEntry.getKey());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readCountryJson(World ecsWorld, EcsConstants ecsConstants, String countryPath, String countryId) {
        try {
            JsonValue countryValues = this.parseJsonFile(countryPath);

            int color = this.parseColor(countryValues.get("color"));
            long countryEntityId = ecsWorld.entity(countryId);
            Entity countryEntity = ecsWorld.obtainEntity(countryEntityId);
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
                    Entity ministerEntity = ecsWorld.obtainEntity(ministerEntityId);
                    ministerEntity.set(new Minister(name, imageNameFile, loyalty, startDate, deathDate, countryEntityId, ideologyEntityId, typeEntityId));
                }
            }
        } catch (DateTimeParseException dateTimeParseException) {
            dateTimeParseException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readRelationJson(World ecsWorld) {
        try {
            JsonValue relationsValues = this.parseJsonFile(this.relationJsonFile);
            for(var relationValue : relationsValues.get("relation").array()) {
                String countryNameId1 = relationValue.get("country1").asString();
                Entity country1 = ecsWorld.obtainEntity(ecsWorld.lookup(countryNameId1));
                String countryNameId2 = relationValue.get("country2").asString();
                Entity country2 = ecsWorld.obtainEntity(ecsWorld.lookup(countryNameId2));
                int value = (int) relationValue.get("value").asLong();
                country1.set(new DiplomaticRelation(value), country2.id());
                country2.set(new DiplomaticRelation(value), country1.id());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAlliancesJson(World ecsWorld, EcsConstants ecsConstants) {
        try {
            JsonValue alliancesValues = this.parseJsonFile(this.alliancesJsonFile);
            for(var allianceValue : alliancesValues.get("alliances").array()) {
                String countryNameId1 = allianceValue.get("country1").asString();
                Entity country1 = ecsWorld.obtainEntity(ecsWorld.entity(countryNameId1));
                String countryNameId2 = allianceValue.get("country2").asString();
                Entity country2 = ecsWorld.obtainEntity(ecsWorld.entity(countryNameId2));
                String type = allianceValue.get("type").asString();
                country1.addRelation(ecsConstants.getAllianceRelation(type, true), country2.id());
                country2.addRelation(ecsConstants.getAllianceRelation(type, false), country1.id());
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLeadersJson(World ecsWorld) {
        List<String> leadersPaths = new ObjectList<>();
        try {
            JsonValue leadersValues = this.parseJsonFile(this.leadersJsonFiles);
            for(var leaderValue : leadersValues.array()) {
                leadersPaths.add(this.historyPath + leaderValue.asString());
            }

            for (var leaderPath : leadersPaths) {
                this.readLeaderJson(ecsWorld, leaderPath);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readLeaderJson(World ecsWorld, String filename) {
        try {
            JsonValue leaderValues = this.parseJsonFile(filename);
            String countryId = leaderValues.get("country").asString();
            long countryEntityId = ecsWorld.lookup(countryId);
            for(var leaderValue : leaderValues.get("leaders").array()) {
                String name = leaderValue.get("name").asString();
                byte skill = (byte) leaderValue.get("skill").asLong();
                byte forceType = ForceTypeUtils.fromString(leaderValue.get("force_type").asString());
                long traitId = ecsWorld.lookup(leaderValue.get("trait").asString());
                long leaderEntityId = ecsWorld.entity();
                Entity leaderEntity = ecsWorld.obtainEntity(leaderEntityId);
                leaderEntity.set(new Leader(name, skill, forceType, traitId, countryEntityId));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadProvinces(World ecsWorld, EcsConstants ecsConstants, IntLongMap provinces, Borders borders) {
        IntObjectMap<LongIntMap> regionBuildingsByProvince = new IntObjectMap<>(396, 1f);
        this.readPopulationTemplatesJson(ecsWorld);
        this.readProvincesJson(ecsWorld, regionBuildingsByProvince);
        this.readRegionJson(ecsWorld, ecsConstants, regionBuildingsByProvince);
        this.readDefinitionCsv(ecsWorld, provinces);
        this.readProvinceBitmap(ecsWorld, provinces, borders);
        this.readCountriesHistoryJson(ecsWorld);
        this.readContinentJsonFile(ecsWorld);
        this.readAdjacenciesJson(ecsWorld);
        this.readPositionsJson(ecsWorld);
    }

    private void readPopulationTemplatesJson(World ecsWorld) {
        try {
            JsonValue populationTemplatesValues = this.parseJsonFile(this.populationTemplatesJsonFile);
            for(var populationTemplateEntry : populationTemplatesValues.object()) {
                long populationTemplateId = ecsWorld.entity("population_template_" + populationTemplateEntry.getKey());
                Entity populationTemplate = ecsWorld.obtainEntity(populationTemplateId);
                JsonValue templateValue = populationTemplateEntry.getValue();
                Iterator<JsonValue> populationValuesIterator = templateValue.get("value").arrayIterator();
                float childrenRatio = (float) populationValuesIterator.next().asDouble();
                float adultsRatio = (float) populationValuesIterator.next().asDouble();
                float seniorsRatio = (float) populationValuesIterator.next().asDouble();
                populationTemplate.set(new PopulationTemplate(childrenRatio, adultsRatio, seniorsRatio));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readProvincesJson(World ecsWorld, IntObjectMap<LongIntMap> regionBuildingsByProvince) {
        IntObjectMap<String> provincesPaths = new IntObjectMap<>(15000, 1f);
        try {
            JsonValue provincesValues = this.parseJsonFile(this.provincesJsonFile);
            for(var provinceEntry : provincesValues.object()) {
                provincesPaths.put(Integer.parseInt(provinceEntry.getKey()), this.historyPath + provinceEntry.getValue().asString());
            }

            for (IntObjectMap.Entry<String> provinceEntry : provincesPaths.entrySet()) {
                int provinceId = provinceEntry.getKey();
                String provincePath = provinceEntry.getValue();
                this.readProvinceJson(ecsWorld, provincePath, provinceId, regionBuildingsByProvince);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readProvinceJson(World ecsWorld, String provincePath, int provinceNameId, IntObjectMap<LongIntMap> regionBuildingsByProvince) {
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
            int templateNumber = (int) populationValue.get("template").asLong();
            Entity template = ecsWorld.obtainEntity(ecsWorld.lookup("population_template_" + templateNumber));
            PopulationTemplate populationTemplateData = template.get(PopulationTemplate.class);
            int amountChildren = (int) (amount * populationTemplateData.childrenRatio());
            int amountAdults = (int) (amount * populationTemplateData.adultsRatio());
            int amountSeniors = (int) (amount * populationTemplateData.seniorsRatio());

            this.parseDistribution(ecsWorld, province, populationValue.get("populations"), amountAdults, "population", 12);
            this.parseDistribution(ecsWorld, province, populationValue.get("cultures"), amountAdults, "culture", 12);
            this.parseDistribution(ecsWorld, province, populationValue.get("religions"), amountAdults, "religion", 6);

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

            JsonValue goodValue = provinceValues.get("good");
            if(goodValue != null) {
                long goodId = ecsWorld.lookup(goodValue.asString());
                province.set(new ResourceGathering(goodId, 0, 0f, new int[12]));
            }

            JsonValue buildingsProvinceValue = provinceValues.get("buildings");
            if(buildingsProvinceValue != null) {
                for(var buildingValue : buildingsProvinceValue.array()) {
                    String buildingName = buildingValue.get("name").asString();
                    int size = (int) buildingValue.get("size").asLong();
                    long buildingTypeId = ecsWorld.lookup(buildingName);
                    Entity building = ecsWorld.obtainEntity(ecsWorld.entity());
                    building.set(new Building(provinceId, buildingTypeId, size));
                }
            }

            province.set(new Province(coreIds, countryOwnerId, countryControllerId, terrainId, amountChildren, amountAdults, amountSeniors));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void parseDistribution(World ecsWorld, Entity province, JsonValue distributionValue, int amountAdults, String distributionType, int length) {
        if (distributionValue == null) {
            return;
        }

        long[] ids = new long[length];
        int[] amounts = new int[length];
        int i = 0;
        for(var distributionEntry : distributionValue.object()) {
            String name = distributionEntry.getKey();
            float percentage = (float) distributionEntry.getValue().asDouble();
            int value = (int) (amountAdults * percentage);
            long id = ecsWorld.lookup(name);
            ids[i] = id;
            amounts[i] = value;
            i++;
        }
        switch (distributionType) {
            case "population" -> {
                province.set(new PopulationDistribution(ids, amounts));
            }
            case "culture" -> {
                province.set(new CultureDistribution(ids, amounts));
            }
            case "religion" -> {
                province.set(new ReligionDistribution(ids, amounts));
            }
        }
    }

    private void readRegionJson(World ecsWorld, EcsConstants ecsConstants, IntObjectMap<LongIntMap> regionBuildingsByProvince) {
        try {
            JsonValue regionValue = this.parseJsonFile(this.regionJsonFiles);
            for(var regionEntry : regionValue.object()) {
                String regionId = regionEntry.getKey();
                long regionEntityId = ecsWorld.entity(regionId);
                Entity regionEntity = ecsWorld.obtainEntity(regionEntityId);
                regionEntity.add(ecsConstants.regionTag());
                for(var provinceValue : regionEntry.getValue().array()) {
                    int provinceId = (int) provinceValue.asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    Entity provinceEntity = provinceEntityId != 0 ? ecsWorld.obtainEntity(provinceEntityId) : null;
                    if(provinceEntityId != 0 && provinceEntity.has(Province.class)) {
                        provinceEntity.set(new GeoHierarchy(regionEntityId, -1));
                        LongIntMap regionBuildingIds = regionBuildingsByProvince.get(provinceId);
                        if(regionBuildingIds != null) {
                            for(LongIntMap.Entry buildingEntry : regionBuildingIds) {
                                long buildingId = buildingEntry.key;
                                int size = buildingEntry.value;
                                Entity building = ecsWorld.obtainEntity(ecsWorld.entity());
                                building.set(new Building(regionEntityId, buildingId, size));
                            }
                        }
                    } else {
                        provinceEntityId = ecsWorld.entity(String.valueOf(provinceId));
                        provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                        provinceEntity.add(ecsConstants.seaProvinceTag());
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readDefinitionCsv(World ecsWorld, IntLongMap provinces) {
        try (BufferedReader bufferedReader = this.parseCsvFile(this.definitionCsvFile)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    int provinceId = Integer.parseInt(values[0]);
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
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

    private void readCountriesHistoryJson(World ecsWorld) {
        ObjectObjectMap<String, String> countriesHistoryPaths = new ObjectObjectMap<>(262, 1f);
        try {
            JsonValue countriesJson = this.parseJsonFile(this.countriesHistoryJsonFiles);
            for(var countryEntry : countriesJson.object()) {
                countriesHistoryPaths.put(countryEntry.getKey(), this.historyPath + countryEntry.getValue().asString());
            }
            for (var countryHistory : countriesHistoryPaths) {
                String countryNameId = countryHistory.getKey();
                String countryFileName = countryHistory.getValue();
                this.readCountryHistoryJson(ecsWorld, countryFileName, countryNameId);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readCountryHistoryJson(World ecsWorld, String countryFileName, String countryNameId) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            Entity country = ecsWorld.obtainEntity(ecsWorld.lookup(countryNameId));

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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readContinentJsonFile(World ecsWorld) {
        try {
            JsonValue continentValues = this.parseJsonFile(this.continentJsonFile);
            for(var continentEntry : continentValues.object()) {
                String continentName = continentEntry.getKey();
                long continentEntityId = ecsWorld.entity(continentName);
                for(var provinceEntry : continentEntry.getValue().array()) {
                    int provinceId = (int) provinceEntry.asLong();
                    long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                    Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
                    GeoHierarchy geoHierarchy = provinceEntity.get(GeoHierarchy.class);
                    provinceEntity.set(new GeoHierarchy(geoHierarchy.regionId(), continentEntityId));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAdjacenciesJson(World ecsWorld) {
        try {
            JsonValue adjenciesValues = this.parseJsonFile(this.adjacenciesJsonFile);
            for(var provinceEntry : adjenciesValues.object()) {
                int provinceId = Integer.parseInt(provinceEntry.getKey());
                long provinceEntityId = ecsWorld.lookup(String.valueOf(provinceId));
                Entity provinceEntity = ecsWorld.obtainEntity(provinceEntityId);
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
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readPositionsJson(World ecsWorld) {
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
                    Entity positionEntity = ecsWorld.obtainEntity(positionEntityId);
                    positionEntity.set(new Position(x, y, provinceEntityId));
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

