package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonMapper;
import com.populaire.projetguerrefroide.adapter.dsljson.JsonValue;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.economy.building.*;
import com.populaire.projetguerrefroide.economy.good.*;
import com.populaire.projetguerrefroide.economy.population.Population;
import com.populaire.projetguerrefroide.economy.population.PopulationTemplate;
import com.populaire.projetguerrefroide.economy.population.PopulationType;
import com.populaire.projetguerrefroide.entity.*;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.national.*;
import com.populaire.projetguerrefroide.service.GameContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WorldDaoImpl implements WorldDao {
    private final String commonPath = "common/";
    private final String mapPath = "map/";
    private final String historyPath = "history/";
    private final String diplomacyPath = historyPath + "diplomacy/";
    private final String countriesJsonFiles = this.commonPath + "countries.json";
    private final String countriesHistoryJsonFiles = this.historyPath + "countries.json";
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
    private final String relationJsonFile = this.diplomacyPath + "relation.json";
    private final String alliancesJsonFile = this.diplomacyPath + "alliances.json";
    private final String startDate;
    private final JsonMapper parser = new JsonMapper();

    public WorldDaoImpl(String startDate) {
        this.startDate = startDate;
    }

    @Override
    public GameEntities createGameEntities() {
        NationalIdeas nationalIdeas = this.readNationalIdeasJson();
        Map<String, Government> governments = this.readGovernmentsJson();
        Map<String, Ideology> ideologies = this.readIdeologiesJson();
        Map<String, Good> goods = this.readGoodsJson();
        Map<String, PopulationType> populationTypes = this.readPopulationTypesJson(goods);
        Map<String, ProductionType> productionTypes = this.readProductionTypesJson(populationTypes);
        Map<String, Building> buildings = this.readBuildingsJson(goods, productionTypes);
        this.readResourceProductionsJson(goods, productionTypes);
        Map<String, MinisterType> ministerTypes = this.readMinisterTypesJson();
        Map<String, Terrain> terrains = this.readTerrainsJson();
        return new GameEntities(nationalIdeas, governments, ideologies, goods, buildings, populationTypes, ministerTypes, terrains);
    }

    @Override
    public World createWorldThreadSafe(GameEntities gameEntities, GameContext gameContext) {
        Map<String, Country> countries = this.loadCountries(gameEntities.getMinisterTypes(), gameEntities.getIdeologies());
        IntObjectMap<LandProvince> provincesByColor = new IntObjectMap<>(15000);
        IntObjectMap<WaterProvince> waterProvincesByColor = new IntObjectMap<>(4000);
        this.loadProvinces(countries, provincesByColor, waterProvincesByColor, gameEntities);

        AtomicReference<World> worldRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Gdx.app.postRunnable(() -> {
            worldRef.set(new World(new ObjectList<>(countries.values()), provincesByColor, waterProvincesByColor, gameContext));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return worldRef.get();
    }

    private JsonValue parseJsonFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return parser.parse(fileHandle.readBytes());
    }

    private BufferedReader parseCsvFile(String filePath) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(filePath);
        return new BufferedReader(new StringReader(fileHandle.readString()));
    }

    private NationalIdeas readNationalIdeasJson() {
        try {
            JsonValue nationalIdeasValues = this.parseJsonFile(this.nationalIdeasJsonFile);

            Map<String, Culture> cultures = new ObjectObjectMap<>();
            Iterator<Map.Entry<String, JsonValue>> culturesEntryIterator = nationalIdeasValues.get("cultures").objectIterator();
            while (culturesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> cultureEntry = culturesEntryIterator.next();
                String name = cultureEntry.getKey();
                JsonValue cultureValue = cultureEntry.getValue();
                int color = this.parseColor(cultureValue.get("color"));
                cultures.put(name, new Culture(name, color));
            }

            Map<String, Religion> religions = new ObjectObjectMap<>();
            Iterator<Map.Entry<String, JsonValue>> religionsEntryIterator = nationalIdeasValues.get("religions").objectIterator();
            while (religionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> religionEntry = religionsEntryIterator.next();
                String name = religionEntry.getKey();
                JsonValue religionValue = religionEntry.getValue();
                int color = this.parseColor(religionValue.get("color"));
                religions.put(name, new Religion(name, color, null));
            }

            Map<String, Identity> identities = new ObjectObjectMap<>();
            Iterator<Map.Entry<String, JsonValue>> identitiesEntryIterator = nationalIdeasValues.get("national_identity").objectIterator();
            while (identitiesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> identityEntry = identitiesEntryIterator.next();
                String name = identityEntry.getKey();
                JsonValue identityValue = identityEntry.getValue();
                List<Modifier> modifiers = new ObjectList<>();
                Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = identityValue.objectIterator();
                while (modifiersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    float value = (float) modifierValue.asDouble();
                    modifiers.add(new Modifier(modifierName, value));
                }
                identities.put(name, new Identity(name, modifiers));
            }

            Map<String, Attitude> attitudes = new ObjectObjectMap<>();
            Iterator<Map.Entry<String, JsonValue>> attitudesEntryIterator = nationalIdeasValues.get("national_attitude").objectIterator();
            while (attitudesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> attitudeEntry = attitudesEntryIterator.next();
                String name = attitudeEntry.getKey();
                JsonValue attitudeValue = attitudeEntry.getValue();
                List<Modifier> modifiers = new ObjectList<>();
                Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = attitudeValue.objectIterator();
                while (modifiersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    JsonValue modifierValue = modifierEntry.getValue();
                    float value = (float) modifierValue.asDouble();
                    modifiers.add(new Modifier(modifierName, value));
                }
                attitudes.put(name, new Attitude(name, modifiers));
            }

            return new NationalIdeas(cultures, religions, identities, attitudes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return null;
    }

    private Map<String, Government> readGovernmentsJson() {
        Map<String, Government> governments = new ObjectObjectMap<>();
        try {
            JsonValue governmentsValues = this.parseJsonFile(this.governmentJsonFile);

            Iterator<Map.Entry<String, JsonValue>> governmentsEntryIterator = governmentsValues.objectIterator();
            while (governmentsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> governmentEntry = governmentsEntryIterator.next();
                String governmentName = governmentEntry.getKey();
                JsonValue governmentValue = governmentEntry.getValue();

                List<String> ideologiesAcceptance = new ObjectList<>();
                Iterator<JsonValue> ideologiesAcceptanceIterator = governmentValue.get("ideologies_acceptance").arrayIterator();
                while (ideologiesAcceptanceIterator.hasNext()) {
                    JsonValue idealogyAcceptanceValue = ideologiesAcceptanceIterator.next();
                    String ideologyName = idealogyAcceptanceValue.asString();
                    ideologiesAcceptance.add(ideologyName);
                }

                JsonValue electionValue = governmentValue.get("election");
                if(!electionValue.isNull() && electionValue.getSize() > 0) {
                    boolean headOfState = electionValue.get("head_of_state").asBoolean();
                    boolean headOfGovernment = electionValue.get("head_of_government").asBoolean();
                    short duration = (short) electionValue.get("duration").asLong();
                    Election election = new Election(headOfState, headOfGovernment, duration);
                    governments.put(governmentName, new Government(governmentName, ideologiesAcceptance, election));
                } else {
                    governments.put(governmentName, new Government(governmentName, ideologiesAcceptance));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return governments;
    }

    private Map<String, Ideology> readIdeologiesJson() {
        Map<String, Ideology> ideologies = new ObjectObjectMap<>();
        try {
            JsonValue ideologiesValues = this.parseJsonFile(this.ideologiesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> ideologiesEntryIterator = ideologiesValues.objectIterator();
            while (ideologiesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = ideologiesEntryIterator.next();
                String ideologyName = entry.getKey();
                JsonValue ideologyValue = entry.getValue();
                int color = this.parseColor(ideologyValue.get("color"));
                short factionDriftingSpeed = (short) ideologyValue.get("faction_drifting_speed").asLong();
                ideologies.put(ideologyName, new Ideology(ideologyName, color, factionDriftingSpeed));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return ideologies;
    }

    private Map<String, Good> readGoodsJson() {
        Map<String, Good> goods = new ObjectObjectMap<>();
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
                goods.put(goodName, new ResourceGood(goodName, cost, color, value));
            }

            Iterator<Map.Entry<String, JsonValue>> goodsEntryIterator = goodsValues.get("advanced_goods").objectIterator();
            while (goodsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = goodsEntryIterator.next();
                String advancedGoodName = entry.getKey();
                JsonValue advancedGoodValue = entry.getValue();
                float cost = (float) advancedGoodValue.get("cost").asDouble();
                int color = this.parseColor(advancedGoodValue.get("color"));
                goods.put(advancedGoodName, new AdvancedGood(advancedGoodName, cost, color));
            }

            Iterator<Map.Entry<String, JsonValue>> militaryGoodsEntryIterator = goodsValues.get("military_goods").objectIterator();
            while (militaryGoodsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = militaryGoodsEntryIterator.next();
                String militaryGoodsName = entry.getKey();
                JsonValue militaryGoodNode = entry.getValue();
                float cost = (float) militaryGoodNode.get("cost").asDouble();
                int color = this.parseColor(militaryGoodNode.get("color"));
                goods.put(militaryGoodsName, new MilitaryGood(militaryGoodsName, cost, color));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return goods;
    }

    private Map<String, PopulationType> readPopulationTypesJson(Map<String, Good> goods) {
        Map<String, PopulationType> populationTypes = new ObjectObjectMap<>();
        Map<String, String> populationPaths = new ObjectObjectMap<>();
        try {
            JsonValue populationTypesValues = this.parseJsonFile(this.populationTypesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> populationTypesEntryIterator = populationTypesValues.objectIterator();
            while (populationTypesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = populationTypesEntryIterator.next();
                populationPaths.put(entry.getKey(), this.commonPath + entry.getValue().asString());
            }

            for (Map.Entry<String, String> populationPath : populationPaths.entrySet()) {
                this.readPopulationTypeJson(populationPath.getValue(), populationPath.getKey(), goods, populationTypes);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return populationTypes;
    }

    private void readPopulationTypeJson(String populationTypePath, String name, Map<String, Good> goods, Map<String, PopulationType> populationTypes) {
        try {
            JsonValue populationTypeValue = this.parseJsonFile(populationTypePath);
            int color = this.parseColor(populationTypeValue.get("color"));
            ObjectFloatMap<Good> standardDemands = new ObjectFloatMap<>();
            Iterator<Map.Entry<String, JsonValue>> standardDemandsEntryIterator = populationTypeValue.get("standard_demands").objectIterator();
            while (standardDemandsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = standardDemandsEntryIterator.next();
                Good good = goods.get(entry.getKey());
                float value = (float) entry.getValue().asDouble();
                standardDemands.put(good, value);
            }

            ObjectFloatMap<Good> luxuryDemands = new ObjectFloatMap<>();
            Iterator<Map.Entry<String, JsonValue>> luxuryDemandsEntryIterator = populationTypeValue.get("luxury_demands").objectIterator();
            while (luxuryDemandsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = luxuryDemandsEntryIterator.next();
                Good good = goods.get(entry.getKey());
                float value = (float) entry.getValue().asDouble();
                luxuryDemands.put(good, value);
            }
            populationTypes.put(name, new PopulationType(color, name, standardDemands, luxuryDemands));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Map<String, ProductionType> readProductionTypesJson(Map<String, PopulationType> populationTypes) {
        Map<String, ProductionType> productionTypes = new ObjectObjectMap<>();
        try {
            JsonValue buildingTypesJson = this.parseJsonFile(this.productionTypesJsonFile);
            Map<String, Employee> typeEmployees = new ObjectObjectMap<>();
            Iterator<Map.Entry<String, JsonValue>> typesEmployeesValues = buildingTypesJson.get("types_employees").objectIterator();
            while (typesEmployeesValues.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesEmployeesValues.next();
                String typeName = entry.getKey();
                JsonValue typeEmployeeValue = entry.getValue();
                PopulationType populationType = populationTypes.get(typeEmployeeValue.get("poptype").asString());
                float amount = (float) typeEmployeeValue.get("amount").asDouble();
                float effectMultiplier = (float) typeEmployeeValue.get("effect_multiplier").asDouble();
                typeEmployees.put(typeName, new Employee(populationType, amount, effectMultiplier));
            }

            Iterator<Map.Entry<String, JsonValue>> typesBuildingsValues = buildingTypesJson.get("types_buildings").objectIterator();
            while (typesBuildingsValues.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesBuildingsValues.next();
                String typeName = entry.getKey();
                JsonValue typeBuildingsValue = entry.getValue();
                short workforce = (short) typeBuildingsValue.get("workforce").asLong();
                PopulationType owner = populationTypes.get(typeBuildingsValue.get("owner").get("poptype").asString());
                List<Employee> employees = new ObjectList<>();
                Iterator<JsonValue> employeesIterator = typeBuildingsValue.get("employees").arrayIterator();
                while (employeesIterator.hasNext()) {
                    JsonValue employee = employeesIterator.next();
                    String employeeName = employee.asString();
                    employees.add(typeEmployees.get(employeeName));
                }
                productionTypes.put(typeName, new ProductionType(workforce, owner, employees));
            }

            Iterator<Map.Entry<String, JsonValue>> typesRGOs = buildingTypesJson.get("types_rgo").objectIterator();
            while (typesRGOs.hasNext()) {
                Map.Entry<String, JsonValue> entry = typesRGOs.next();
                String typeName = entry.getKey();
                JsonValue typeRGOValue = entry.getValue();
                short workforce = (short) typeRGOValue.get("workforce").asLong();
                PopulationType owner = populationTypes.get(typeRGOValue.get("owner").get("poptype").asString());
                List<Employee> employees = new ObjectList<>();
                Iterator<JsonValue> employeesIterator = typeRGOValue.get("employees").arrayIterator();
                while (employeesIterator.hasNext()) {
                    JsonValue employee = employeesIterator.next();
                    String employeeName = employee.asString();
                    employees.add(typeEmployees.get(employeeName));
                }
                productionTypes.put(typeName, new ResourceProductionType(workforce, owner, employees));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return productionTypes;
    }

    private Map<String, Building> readBuildingsJson(Map<String, Good> goods, Map<String, ProductionType> productionTypes) {
        Map<String, Building> buildings = new ObjectObjectMap<>();
        try {
            JsonValue buildingsValues = this.parseJsonFile(this.buildingsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> economyBuildingEntryIterator = buildingsValues.get("economy_building").objectIterator();
            while (economyBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = economyBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                short time = (short) buildingValue.get("time").asLong();
                ProductionType baseType = productionTypes.get(buildingValue.get("base_type").asString());
                ProductionType artisansType = null;
                if(buildingValue.get("artisans_type") != null) {
                    artisansType = productionTypes.get(buildingValue.get("artisans_type").asString());
                }
                short maxLevel = (short) buildingValue.get("max_level").asLong();
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                ObjectFloatMap<Good> goodsCost = new ObjectFloatMap<>();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    Good good = goods.get(goodCost.getKey());
                    goodsCost.put(good, (float) goodCost.getValue().asDouble());
                }
                Iterator<Map.Entry<String, JsonValue>> inputGoodsEntryIterator = buildingValue.get("input_goods").objectIterator();
                ObjectFloatMap<Good> inputGoods = new ObjectFloatMap<>();
                while (inputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> inputGood = inputGoodsEntryIterator.next();
                    Good good = goods.get(inputGood.getKey());
                    inputGoods.put(good, (float) inputGood.getValue().asDouble());
                }
                Iterator<Map.Entry<String, JsonValue>> outputGoodsEntryIterator = buildingValue.get("output_goods").objectIterator();
                ObjectFloatMap<Good> outputGoods = new ObjectFloatMap<>();
                while (outputGoodsEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> outputGood = outputGoodsEntryIterator.next();
                    Good good = goods.get(outputGood.getKey());
                    outputGoods.put(good, (float) outputGood.getValue().asDouble());
                }
                buildings.put(buildingName, new EconomyBuilding(baseType, artisansType, buildingName, time, goodsCost, inputGoods, outputGoods, maxLevel));
            }

            Iterator<Map.Entry<String, JsonValue>> specialBuildingEntryIterator = buildingsValues.get("special_building").objectIterator();
            while (specialBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = specialBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                int cost = (int) buildingValue.get("cost").asLong();
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                ObjectFloatMap<Good> goodsCost = new ObjectFloatMap<>();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    Good good = goods.get(goodCost.getKey());
                    goodsCost.put(good, (float) goodCost.getValue().asDouble());
                }
                short time = (short) buildingValue.get("time").asLong();

                JsonValue modifiersValues = buildingValue.get("modifier");
                if (modifiersValues != null) {
                    Iterator<Map.Entry<String, JsonValue>> modifiersEntryIterator = modifiersValues.objectIterator();
                    List<Modifier> modifiers = new ObjectList<>();
                    while (modifiersEntryIterator.hasNext()) {
                        Map.Entry<String, JsonValue> modifierEntry = modifiersEntryIterator.next();
                        String modifierName = modifierEntry.getKey();
                        JsonValue modifierValue = modifierEntry.getValue();
                        if (modifierValue.isLong()) {
                            modifiers.add(new Modifier(modifierName, (float) modifierValue.asDouble()));
                        } else {
                            float value = (float) modifierValue.get("value").asDouble();
                            String modifierType = modifierValue.get("type").asString();
                            modifiers.add(new Modifier(modifierName, value, modifierType));
                        }
                    }
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time, goodsCost, modifiers));
                } else {
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time, goodsCost));
                }
            }

            Iterator<Map.Entry<String, JsonValue>> developmentBuildingEntryIterator = buildingsValues.get("development_building").objectIterator();
            while (developmentBuildingEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = developmentBuildingEntryIterator.next();
                String buildingName = entry.getKey();
                JsonValue buildingValue = entry.getValue();
                int cost = (int) buildingValue.get("cost").asLong();
                Iterator<Map.Entry<String, JsonValue>> goodsCostEntryIterator = buildingValue.get("goods_cost").objectIterator();
                ObjectFloatMap<Good> goodsCost = new ObjectFloatMap<>();
                while (goodsCostEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> goodCost = goodsCostEntryIterator.next();
                    Good good = goods.get(goodCost.getKey());
                    goodsCost.put(good, (float) goodCost.getValue().asDouble());
                }
                short time = (short) buildingValue.get("time").asLong();
                boolean onMap = buildingValue.get("onmap").asBoolean();
                short maxLevel = (short) buildingValue.get("max_level").asLong();
                JsonValue modifierValues = buildingValue.get("modifier");
                if(modifierValues != null) {
                    Iterator<Map.Entry<String, JsonValue>> modifierEntryIterator = modifierValues.objectIterator();
                    Map.Entry<String, JsonValue> modifierEntry = modifierEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    float modifierValue = (float) modifierEntry.getValue().asLong();
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, goodsCost, onMap, maxLevel, new Modifier(modifierName, modifierValue)));
                } else {
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, goodsCost, onMap, maxLevel));
                }

            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return buildings;
    }

    private void readResourceProductionsJson(Map<String, Good> goods, Map<String, ProductionType> productionTypes) {
        try {
            JsonValue resourceProductionsValues = this.parseJsonFile(this.resourceProductionsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> resourceProductionsEntryIterator = resourceProductionsValues.objectIterator();
            while (resourceProductionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = resourceProductionsEntryIterator.next();
                ResourceGood good = (ResourceGood) goods.get(entry.getKey());
                JsonValue productionValue = entry.getValue();
                ResourceProductionType productionType = (ResourceProductionType) productionTypes.get(productionValue.get("base_type").asString());
                good.setProductionType(productionType);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Map<String, MinisterType> readMinisterTypesJson() {
        Map<String, MinisterType> ministerTypes = new ObjectObjectMap<>();
        try {
            JsonValue ministerTypesValues = this.parseJsonFile(this.ministerTypesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> ministerTypesEntryIterator = ministerTypesValues.objectIterator();
            while (ministerTypesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = ministerTypesEntryIterator.next();
                String ministerTypeName = entry.getKey();
                List<Modifier> modifiers = new ObjectList<>();
                Iterator<Map.Entry<String, JsonValue>> modifierEntryIterator = entry.getValue().objectIterator();
                while (modifierEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> modifierEntry = modifierEntryIterator.next();
                    String modifierName = modifierEntry.getKey();
                    float modifierValue = (float) modifierEntry.getValue().asDouble();
                    modifiers.add(new Modifier(modifierName, modifierValue));
                }
                ministerTypes.put(ministerTypeName, new MinisterType(ministerTypeName, modifiers));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return ministerTypes;
    }

    private Map<String, Terrain> readTerrainsJson() {
        Map<String, Terrain> terrains = new ObjectObjectMap<>();
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
                terrains.put(terrainName, new Terrain(terrainName, movementCost, temperature, humidity, precipitation, color));
            }

            return terrains;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return terrains;
    }

    private Map<String, Country> loadCountries(Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        Map<String, Country> countries = this.readCountriesJson(ministerTypes, ideologies);
        this.readRelationJson(countries);
        this.readAlliancesJson(countries);
        return countries;
    }

    private Map<String, Country> readCountriesJson(Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        Map<String, Country> countries = new ObjectObjectMap<>();
        Map<String, String> countriesPaths = new ObjectObjectMap<>();
        try {
            JsonValue countriesValues = this.parseJsonFile(this.countriesJsonFiles);
            Iterator<Map.Entry<String, JsonValue>> countriesEntryIterator = countriesValues.objectIterator();
            while (countriesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = countriesEntryIterator.next();
                countriesPaths.put(entry.getKey(), this.commonPath + entry.getValue().asString());
            }

            for (Map.Entry<String, String> entry : countriesPaths.entrySet()) {
                Country country = this.readCountryJson(entry.getValue(), entry.getKey(), ministerTypes, ideologies);
                countries.put(entry.getKey(), country);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return countries;
    }

    private Country readCountryJson(String countryPath, String countryId, Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        try {
            JsonValue countryValues = this.parseJsonFile(countryPath);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Country country = new Country(countryId, this.parseColor(countryValues.get("color")));

            JsonValue ministersValues = countryValues.get("ministers");
            if (ministersValues != null && ministersValues.isObject()) {
                Iterator<Map.Entry<String, JsonValue>> ministersEntryIterator = ministersValues.objectIterator();
                while (ministersEntryIterator.hasNext()) {
                    Map.Entry<String, JsonValue> ministerEntry = ministersEntryIterator.next();
                    int ministerId = Integer.parseInt(ministerEntry.getKey());
                    JsonValue ministerNode = ministerEntry.getValue();
                    String name = ministerNode.get("name").asString();
                    String ideology = ministerNode.get("ideology").asString();
                    float loyalty = (float) ministerNode.get("loyalty").asDouble();
                    String imageNameFile = ministerNode.get("picture").asString();
                    String type = ministerNode.get("type").asString();
                    Date startDate = null;
                    Date deathDate = null;
                    try {
                        startDate = dateFormat.parse(ministerNode.get("start_date").asString());
                        deathDate = dateFormat.parse(ministerNode.get("death_date").asString());
                    } catch (ParseException parseException) {
                        parseException.printStackTrace();
                    }

                    Minister minister = new Minister(name, ideologies.get(ideology), imageNameFile, loyalty, ministerTypes.get(type), startDate, deathDate);
                    country.addMinister(ministerId, minister);
                }
            }
            return country;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private void readRelationJson(Map<String, Country> countries) {
        try {
            JsonValue relationsValues = this.parseJsonFile(this.relationJsonFile);
            Iterator<JsonValue> relationsIterator = relationsValues.get("relation").arrayIterator();
            while (relationsIterator.hasNext()) {
                JsonValue relation = relationsIterator.next();
                Country country1 = countries.get(relation.get("country1").asString());
                Country country2 = countries.get(relation.get("country2").asString());
                int relationValue = (int) relation.get("value").asLong();
                country1.addRelation(country2, relationValue);
                country2.addRelation(country1, relationValue);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAlliancesJson(Map<String, Country> countries) {
        try {
            JsonValue alliancesValues = this.parseJsonFile(this.alliancesJsonFile);
            Iterator<JsonValue> alliancesIterator = alliancesValues.get("alliances").arrayIterator();
            while (alliancesIterator.hasNext()) {
                JsonValue alliance = alliancesIterator.next();
                Country country1 = countries.get(alliance.get("country1").asString());
                Country country2 = countries.get(alliance.get("country2").asString());
                String type = alliance.get("type").asString();
                country1.addAlliance(country2, AllianceType.getAllianceType(type, true));
                country2.addAlliance(country1, AllianceType.getAllianceType(type, false));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadProvinces(Map<String, Country> countries, IntObjectMap<LandProvince> provincesByColor, IntObjectMap<WaterProvince> waterProvincesByColor, GameEntities gameEntities) {
        IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince = new IntObjectMap<>();
        IntObjectMap<PopulationTemplate> populationTemplates = this.readPopulationTemplatesJson();
        IntObjectMap<Province> provinces = this.readProvincesJson(countries, gameEntities, regionBuildingsByProvince, populationTemplates);
        this.readRegionJson(provinces, regionBuildingsByProvince);
        this.readDefinitionCsv(provinces, provincesByColor, waterProvincesByColor);
        this.readProvinceBitmap(provincesByColor);
        this.readCountriesHistoryJson(countries, provinces, gameEntities);
        this.readContinentJsonFile(provinces);
        this.readAdjenciesJson(provinces);
        this.readPositionsJson(provinces);
    }

    private IntObjectMap<PopulationTemplate> readPopulationTemplatesJson() {
        IntObjectMap<PopulationTemplate> populationTemplates = new IntObjectMap<>();
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
                populationTemplates.put(template, new PopulationTemplate(template, children, adults, seniors));
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return populationTemplates;
    }

    private IntObjectMap<Province> readProvincesJson(Map<String, Country> countries, GameEntities gameEntities, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince, IntObjectMap<PopulationTemplate> populationTemplates) {
        IntObjectMap<Province> provinces = new IntObjectMap<>(15000);
        IntObjectMap<String> provincesPaths = new IntObjectMap<>(15000);
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
                Province province = this.readProvinceJson(countries, provincePath, provinceId, gameEntities, regionBuildingsByProvince, populationTemplates);
                provinces.put(provinceId, province);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return provinces;
    }

    private LandProvince readProvinceJson(Map<String, Country> countries, String provincePath, short provinceId, GameEntities gameEntities, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince, IntObjectMap<PopulationTemplate> populationTemplates) {
        try {
            JsonValue provinceValues = this.parseJsonFile(provincePath);

            List<Country> countriesCore = new ObjectList<>();
            JsonValue addCoreValue = provinceValues.get("add_core");
            if (addCoreValue.isArray()) {
                Iterator<JsonValue> addCoreIterator = addCoreValue.arrayIterator();
                while (addCoreIterator.hasNext()) {
                    JsonValue countryCore = addCoreIterator.next();
                    countriesCore.add(countries.get(countryCore.asString()));
                }
            } else {
                countriesCore.add(countries.get(addCoreValue.asString()));
            }

            String owner = provinceValues.get("owner").asString();
            Country countryOwner = countries.get(owner);

            String controller = provinceValues.get("controller").asString();
            Country countryController = countries.get(controller);

            String terrain = provinceValues.get("terrain").asString();
            Terrain provinceTerrain = gameEntities.getTerrains().get(terrain);

            JsonValue populationValue = provinceValues.get("population_total");
            int amount = (int) populationValue.get("amount").asLong();
            short template = (short) populationValue.get("template").asLong();
            PopulationTemplate populationTemplate = populationTemplates.get(template);
            int amountChildren = (int) (amount * populationTemplate.getChildren());
            int amountSeniors = (int) (amount * populationTemplate.getSeniors());
            int amountAdults = (int) (amount * populationTemplate.getAdults());

            ObjectIntMap<PopulationType> populations = this.parseDistribution(populationValue.get("populations"), amountAdults, gameEntities.getPopulationTypes());
            ObjectIntMap<Culture> cultures = this.parseDistribution(populationValue.get("cultures"), amountAdults, gameEntities.getNationalIdeas().getCultures());
            ObjectIntMap<Religion> religions = this.parseDistribution(populationValue.get("religions"), amountAdults, gameEntities.getNationalIdeas().getReligions());

            Population population = new Population(amountChildren, amountAdults, amountSeniors, populations, cultures, religions);

            ObjectIntMap<Building> buildingsRegion;
            JsonValue buildingsValue = provinceValues.get("economy_buildings");
            if(buildingsValue != null) {
                Iterator<JsonValue> buildingsIterator = buildingsValue.arrayIterator();
                buildingsRegion = new ObjectIntMap<>();
                while (buildingsIterator.hasNext()) {
                    JsonValue building = buildingsIterator.next();
                    String buildingName = building.get("name").asString();
                    short size = (short) building.get("size").asLong();
                    buildingsRegion.put(gameEntities.getBuildings().get(buildingName), size);

                }
                regionBuildingsByProvince.put(provinceId, buildingsRegion);
            }

            ResourceGood resourceGood = null;
            JsonValue goodValue = provinceValues.get("good");
            if(goodValue != null) {
                resourceGood = (ResourceGood) gameEntities.getGoods().get(goodValue.asString());
            }

            ObjectIntMap<Building> buildingsProvince = new ObjectIntMap<>();
            JsonValue buildingsProvinceValue = provinceValues.get("buildings");
            if(buildingsProvinceValue != null) {
                Iterator<JsonValue> buildingsProvinceIterator = buildingsProvinceValue.arrayIterator();
                while (buildingsProvinceIterator.hasNext()) {
                    JsonValue building = buildingsProvinceIterator.next();
                    String buildingName = building.get("name").asString();
                    short size = (short) building.get("size").asLong();
                    buildingsProvince.put(gameEntities.getBuildings().get(buildingName), size);
                }
            }

            LandProvince province = new LandProvince(provinceId, countryOwner, countryController, population, provinceTerrain, countriesCore, resourceGood, buildingsProvince);
            countryOwner.addProvince(province);
            return province;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private <T> ObjectIntMap<T> parseDistribution(JsonValue jsonValue, int baseAmount, Map<String, T> lookupMap) {
        ObjectIntMap<T> result = new ObjectIntMap<>();
        int total = 0;
        T biggest = null;

        Iterator<Map.Entry<String, JsonValue>> fields = jsonValue.objectIterator();
        while (fields.hasNext()) {
            Map.Entry<String, JsonValue> entry = fields.next();
            T element = lookupMap.get(entry.getKey());

            int computed = (int) (baseAmount * entry.getValue().asDouble());
            result.put(element, computed);
            total += computed;

            if (biggest == null || computed > result.get(biggest)) {
                biggest = element;
            }
        }

        if (total != baseAmount && biggest != null) {
            result.put(biggest, result.get(biggest) + (baseAmount - total));
        }
        return result;
    }

    private void readRegionJson(IntObjectMap<Province> provinces, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince) {
        try {
            AtomicInteger total = new AtomicInteger();
            JsonValue regionValue = this.parseJsonFile(this.regionJsonFiles);
            Iterator<Map.Entry<String, JsonValue>> regionEntryIterator = regionValue.objectIterator();
            while (regionEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = regionEntryIterator.next();
                Region region = new Region(entry.getKey());
                Iterator<JsonValue> regionIterator = entry.getValue().arrayIterator();
                while (regionIterator.hasNext()) {
                    short provinceId = (short) regionIterator.next().asLong();
                    LandProvince province = (LandProvince) provinces.get(provinceId);
                    if(province != null) {
                        province.getCountryController().addRegion(region);
                        province.setRegion(region);
                        region.addProvince(province);
                        ObjectIntMap<Building> regionBuildings = regionBuildingsByProvince.get(province.getId());
                        if(regionBuildings != null) {
                            province.getRegion().addAllBuildings(regionBuildings);
                        }
                    } else {
                        WaterProvince waterProvince = new WaterProvince(provinceId);
                        provinces.put(provinceId, waterProvince);
                    }
                }
                if(!region.getBuildings().isEmpty()) {
                    total.set(total.get() + 1);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readDefinitionCsv(IntObjectMap<Province> provinces, IntObjectMap<LandProvince> provincesByColor, IntObjectMap<WaterProvince> waterProvincesByColor) {
        try (BufferedReader bufferedReader = this.parseCsvFile(this.definitionCsvFile)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    short provinceId = Short.parseShort(values[0]);
                    Province province = provinces.get(provinceId);
                    int red = Integer.parseInt(values[1]);
                    int green = Integer.parseInt(values[2]);
                    int blue = Integer.parseInt(values[3]);
                    int alpha = 255;

                    int color =  (red << 24) | (green << 16) | (blue << 8) | alpha;
                    province.setColor(color);
                    if(province instanceof LandProvince landProvince) {
                        provincesByColor.put(color, landProvince);
                    } else if(province instanceof WaterProvince waterProvince) {
                        waterProvincesByColor.put(color, waterProvince);
                    }
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readProvinceBitmap(IntObjectMap<LandProvince> provincesByColor) {
        Pixmap provincesPixmap = new Pixmap(Gdx.files.internal(this.mapPath + "provinces.bmp"));
        short height = (short) provincesPixmap.getHeight();
        short width = (short) provincesPixmap.getWidth();
        for (short y = 0; y < provincesPixmap.getHeight(); y++) {
            for (short x = 0; x < provincesPixmap.getWidth(); x++) {
                int color = provincesPixmap.getPixel(x, y);
                LandProvince province = provincesByColor.get(color);
                if(province != null) {
                    if(x + 1 < width && provincesPixmap.getPixel(x + 1, y) != color) {
                        province.addBorderPixel(x, y);
                    } else if(x > 0 && provincesPixmap.getPixel(x - 1, y) != color) {
                        province.addBorderPixel(x, y);
                    } else if(y + 1 < height && provincesPixmap.getPixel(x, y + 1) != color) {
                        province.addBorderPixel(x, y);
                    } else if(y > 0 && provincesPixmap.getPixel(x, y - 1) != color) {
                        province.addBorderPixel(x, y);
                    }
                }
            }
        }

        provincesPixmap.dispose();
    }

    private void readCountriesHistoryJson(Map<String, Country> countries, IntObjectMap<Province> provinces, GameEntities gameEntities) {
        ObjectObjectMap<String, String> countriesHistoryPaths = new ObjectObjectMap<>();
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
                this.readCountryHistoryJson(countries, countryFileName, countryId, provinces, gameEntities);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readCountryHistoryJson(Map<String, Country> countries, String countryFileName, String idCountry, IntObjectMap<Province> provinces, GameEntities gameEntities) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            JsonValue countryValues = this.parseJsonFile(countryFileName);
            short idCapital = (short) countryValues.get("capital").asLong();
            Country country = countries.get(idCountry);
            LandProvince capital = (LandProvince) provinces.get(idCapital);
            country.setCapital(capital);
            String government = countryValues.get("government").asString();
            country.setGovernment(gameEntities.getGovernments().get(government));
            String ideology = countryValues.get("ideology").asString();
            country.setIdeology(gameEntities.getIdeologies().get(ideology));
            String identity = countryValues.get("national_identity").asString();
            country.setIdentity(gameEntities.getNationalIdeas().getIdentities().get(identity));
            String attitude = countryValues.get("national_attitude").asString();
            country.setAttitude(gameEntities.getNationalIdeas().getAttitudes().get(attitude));
            JsonValue setupValues = countryValues.get(this.startDate);
            if (setupValues.get("head_of_state") != null && setupValues.get("head_of_government") != null) {
                int idMinisterHeadOfState = (int) setupValues.get("head_of_state").asLong();
                int idMinisterHeadOfGovernment = (int) setupValues.get("head_of_government").asLong();
                country.setHeadOfState(idMinisterHeadOfState);
                country.setHeadOfGovernment(idMinisterHeadOfGovernment);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readContinentJsonFile(IntObjectMap<Province> provinces) {
        try {
            JsonValue continentValues = this.parseJsonFile(this.continentJsonFile);
            Iterator<Map.Entry<String, JsonValue>> continentEntryIterator = continentValues.objectIterator();
            while (continentEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = continentEntryIterator.next();
                Continent continent = new Continent(entry.getKey());
                Iterator<JsonValue> provincesIterator = entry.getValue().arrayIterator();
                while (provincesIterator.hasNext()) {
                    short provinceId = (short) provincesIterator.next().asLong();
                    LandProvince province = (LandProvince) provinces.get(provinceId);
                    province.setContinent(continent);
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readAdjenciesJson(IntObjectMap<Province> provinces) {
        try {
            JsonValue adjenciesValues = this.parseJsonFile(this.adjenciesJsonFile);
            Iterator<Map.Entry<String, JsonValue>> adjenciesEntryIterator = adjenciesValues.objectIterator();
            while (adjenciesEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = adjenciesEntryIterator.next();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                Iterator<JsonValue> adjacenciesIterator = entry.getValue().arrayIterator();
                while (adjacenciesIterator.hasNext()) {
                    short adjacencyId = (short) adjacenciesIterator.next().asLong();
                    province.addAdjacentProvinces(provinces.get(adjacencyId));
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void readPositionsJson(IntObjectMap<Province> provinces) {
        try {
            JsonValue positionsValues = this.parseJsonFile(this.positionsJsonFile);
            Iterator<Map.Entry<String, JsonValue>> positionsEntryIterator = positionsValues.objectIterator();
            while (positionsEntryIterator.hasNext()) {
                Map.Entry<String, JsonValue> entry = positionsEntryIterator.next();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                Iterator<Map.Entry<String, JsonValue>> positionIterator = entry.getValue().objectIterator();
                while (positionIterator.hasNext()) {
                    Map.Entry<String, JsonValue> position = positionIterator.next();
                    String name = position.getKey();
                    JsonValue positionNode = position.getValue();
                    short x = (short) positionNode.get("x").asLong();
                    short y = (short) positionNode.get("y").asLong();
                    province.addPosition(name, (x << 16) | (y & 0xFFFF));
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

