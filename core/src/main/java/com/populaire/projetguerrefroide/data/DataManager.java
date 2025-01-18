package com.populaire.projetguerrefroide.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.economy.population.Population;
import com.populaire.projetguerrefroide.economy.population.PopulationDemands;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.building.DevelopmentBuilding;
import com.populaire.projetguerrefroide.economy.building.EconomyBuilding;
import com.populaire.projetguerrefroide.economy.building.SpecialBuilding;
import com.populaire.projetguerrefroide.economy.good.*;
import com.populaire.projetguerrefroide.economy.population.PopulationType;
import com.populaire.projetguerrefroide.entity.*;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.national.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DataManager {
    private final String commonPath = "common/";
    private final String mapPath = "map/";
    private final String historyPath = "history/";
    private final String countriesJsonFiles = this.commonPath + "countries.json";
    private final String countriesHistoryJsonFiles = this.historyPath + "countries.json";
    private final String regionJsonFiles = this.mapPath + "region.json";
    private final String provincesJsonFile = this.historyPath + "provinces.json";
    private final String definitionCsvFile = this.mapPath + "definition.csv";
    private final String continentJsonFile = this.mapPath + "continent.json";
    private final String positionsJsonFile = this.mapPath + "positions.json";
    private final String adjenciesJsonFile = this.mapPath + "adjacencies.json";
    private final String governmentJsonFile = this.commonPath + "governments.json";
    private final String ideologiesJsonFile = this.commonPath + "ideologies.json";
    private final String nationalIdeasJsonFile = this.commonPath + "national_ideas.json";
    private final String goodsJsonFile = this.commonPath + "goods.json";
    private final String populationTypesJsonFile = this.commonPath + "population_types.json";
    private final String populationDemandsJsonFile = this.commonPath + "population_demands.json";
    private final String ministerTypesJsonFile = this.commonPath + "minister_types.json";
    private final String buildingsJsonFile = this.commonPath + "buildings.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String defaultDate = "1946.1.1";

    public GameEntities createGameEntities() {
        NationalIdeas nationalIdeas = this.readNationalIdeasJson();
        Map<String, Government> governments = this.readGovernmentsJson();
        Map<String, Ideology> ideologies = this.readIdeologiesJson();
        Map<String, Good> goods = this.readGoodsJson();
        PopulationDemands populationDemands = this.readPopulationDemandsJson(goods);
        Map<String, Building> buildings = this.readBuildingsJson(goods);
        Map<String, MinisterType> ministerTypes = this.readMinisterTypesJson();
        return new GameEntities(nationalIdeas, governments, ideologies, goods, populationDemands, buildings, ministerTypes, this.readPopulationTypesJson());
    }

    public World createWorldThreadSafe(GameEntities gameEntities, AsyncExecutor asyncExecutor) {
        Map<String, Country> countries = this.loadCountries(gameEntities.getMinisterTypes(), gameEntities.getIdeologies());
        IntObjectMap<Province> provinces = this.loadProvinces(countries, gameEntities.getPopulationTypes(), gameEntities.getNationalIdeas(), gameEntities.getGovernments(), gameEntities.getIdeologies());

        AtomicReference<World> worldRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Gdx.app.postRunnable(() -> {
            worldRef.set(new World(new ObjectList<>(countries.values()), provinces, asyncExecutor));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return worldRef.get();
    }

    private Map<String, Country> loadCountries(Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        return this.readCountriesJson(ministerTypes, ideologies);
    }

    private IntObjectMap<Province> loadProvinces(Map<String, Country> countries, IntObjectMap<PopulationType> populationTypes, NationalIdeas nationalIdeas, Map<String, Government> governments, Map<String, Ideology> ideologies) {
        IntObjectMap<Province> provincesByColor = new IntObjectMap<>(20000);
        IntMap<Province> provinces = this.readProvincesJson(countries, populationTypes);
        this.readRegionJson(provinces);
        this.readDefinitionCsv(provinces, provincesByColor);
        this.readProvinceBitmap(provincesByColor);
        this.readCountriesHistoryJson(countries, provinces, nationalIdeas, governments, ideologies);
        this.readContinentJsonFile(provinces);
        this.readPositionsJson(provinces);
        this.readAdjenciesJson(provinces);

        return provincesByColor;
    }

    private JsonNode openJson(String fileName) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return this.mapper.readTree(fileHandle.readString());
    }

    private Map<String, Country> readCountriesJson(Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        Map<String, Country> countries = new ObjectObjectMap<>();
        try {
            JsonNode countriesJson = this.openJson(this.countriesJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.commonPath + entry.getValue().asText();
                countries.put(entry.getKey(), this.readCountryJson(countryFileName, entry.getKey(), parseFileName(countryFileName), ministerTypes, ideologies));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countries;
    }

    private String parseFileName(String path) {
        return path.replace("countries/", "").replace(".json", "");
    }

    private Country readCountryJson(String countryFileName, String countryId, String countryName, Map<String, MinisterType> ministerTypes, Map<String, Ideology> ideologies) {
        try {
            JsonNode countryJson = this.openJson(countryFileName);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Country country = new Country(countryId, countryName.replace("common/", ""), this.parseColor(countryJson.get("color")));

            JsonNode ministersNode = countryJson.get("ministers");
            if (ministersNode != null && ministersNode.isObject()) {
                ministersNode.fields().forEachRemaining(ministerEntry -> {
                    int ministerId = Integer.parseInt(ministerEntry.getKey());
                    JsonNode ministerNode = ministerEntry.getValue();
                    String name = ministerNode.get("name").asText();
                    String ideology = ministerNode.get("ideology").asText();
                    float loyalty = (float) ministerNode.get("loyalty").asDouble();
                    String imageNameFile = ministerNode.get("picture").asText();
                    String type = ministerNode.get("type").asText();
                    Date startDate = null;
                    Date deathDate = null;
                    try {
                        startDate = dateFormat.parse(ministerNode.get("start_date").asText());
                        deathDate = dateFormat.parse(ministerNode.get("death_date").asText());
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }

                    Minister minister = new Minister(name, ideologies.get(ideology), imageNameFile, loyalty, ministerTypes.get(type), startDate, deathDate);
                    country.addMinister(ministerId, minister);
                });
            }
            return country;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int parseColor(JsonNode colorNode) {
        int red = colorNode.get(0).intValue();
        int green = colorNode.get(1).intValue();
        int blue = colorNode.get(2).intValue();
        int alpha = 255;

        return (red << 24) | (green << 16) | (blue << 8) | alpha;
    }

    private IntMap<Province> readProvincesJson(Map<String, Country> countries, IntObjectMap<PopulationType> populationTypes) {
        IntMap<Province> provinces = new IntMap<>(20000);
        try {
            JsonNode provincesJson = this.openJson(this.provincesJsonFile);
            provincesJson.fields().forEachRemaining(entry -> {
                String provinceFileName = this.historyPath + entry.getValue().asText();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = this.readProvinceJson(countries, provinceFileName, provinceId, populationTypes);
                provinces.put(provinceId, province);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return provinces;
    }

    private LandProvince readProvinceJson(Map<String, Country> countries, String provinceFileName, short provinceId, IntObjectMap<PopulationType> populationTypes) {
        try {
            JsonNode rootNode = this.openJson(provinceFileName);
            String owner = rootNode.path("owner").asText();
            String controller = rootNode.path("controller").asText();
            Country countryOwner = countries.get(owner);
            Country countryController = countries.get(controller);
            JsonNode dateNode = rootNode.path(this.defaultDate);
            JsonNode populationNode = dateNode.path("population_total");
            int amount = populationNode.get("amount").intValue();
            short template = populationNode.get("template").shortValue();
            Population population = new Population(amount, populationTypes.get(template));
            LandProvince province = new LandProvince(provinceId, countryOwner, countryController, population);
            countryOwner.addProvince(province);
            return province;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readRegionJson(IntMap<Province> provinces) {
        try {
            JsonNode rootNode = this.openJson(this.regionJsonFiles);
            rootNode.fields().forEachRemaining(regionData -> {
                Region region = new Region(regionData.getKey());
                regionData.getValue().forEach(provinceId -> {
                    LandProvince province = (LandProvince) provinces.get(provinceId.shortValue());
                    if(province != null) {
                        province.getCountryController().addRegion(region);
                        province.setRegion(region);
                        region.addProvince(province);
                    } else {
                        WaterProvince waterProvince = new WaterProvince(provinceId.shortValue());
                        provinces.put(provinceId.shortValue(), waterProvince);
                    }
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readDefinitionCsv(IntMap<Province> provinces, IntObjectMap<Province> provincesByColor) {
        try (BufferedReader br = new BufferedReader(new StringReader(Gdx.files.internal(this.definitionCsvFile).readString()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    short provinceId = Short.parseShort(values[0]);
                    Province province = provinces.get(provinceId);
                    if(province instanceof LandProvince) {
                        int red = Integer.parseInt(values[1]);
                        int green = Integer.parseInt(values[2]);
                        int blue = Integer.parseInt(values[3]);
                        int alpha = 255;

                        int color =  (red << 24) | (green << 16) | (blue << 8) | alpha;
                        province.setColor(color);
                        provincesByColor.put(color, province);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCountriesHistoryJson(Map<String, Country> countries, IntMap<Province> provinces, NationalIdeas nationalIdeas, Map<String, Government> governments, Map<String, Ideology> ideologies) {
        try {
            JsonNode countriesJson = this.openJson(this.countriesHistoryJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.historyPath + entry.getValue().textValue();
                this.readCountryHistoryJson(countries, countryFileName, entry.getKey(), provinces, nationalIdeas, governments, ideologies);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readContinentJsonFile(IntMap<Province> provinces) {
        try {
            JsonNode continentJson = this.openJson(this.continentJsonFile);
            continentJson.fields().forEachRemaining(entry -> {
                Continent continent = new Continent(entry.getKey());
                entry.getValue().forEach(provinceId -> {
                    LandProvince province = (LandProvince) provinces.get(provinceId.shortValue());
                    province.setContinent(continent);
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCountryHistoryJson(Map<String, Country> countries, String countryFileName, String idCountry, IntMap<Province> provinces, NationalIdeas nationalIdeas, Map<String, Government> governments, Map<String, Ideology> ideologies) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            JsonNode countryJson = this.openJson(countryFileName);
            short idCapital = countryJson.get("capital").shortValue();
            Country country = countries.get(idCountry);
            LandProvince capital = (LandProvince) provinces.get(idCapital);
            country.setCapital(capital);
            String government = countryJson.get("government").textValue();
            country.setGovernment(governments.get(government));
            String ideology = countryJson.get("ideology").textValue();
            country.setIdeology(ideologies.get(ideology));
            String culture = countryJson.get("national_culture").textValue();
            country.setCulture(nationalIdeas.getCultures().get(culture));
            String identity = countryJson.get("national_identity").textValue();
            country.setIdentity(nationalIdeas.getIdentities().get(identity));
            String religion = countryJson.get("national_religion").textValue();
            country.setReligion(nationalIdeas.getReligions().get(religion));
            String attitude = countryJson.get("national_attitude").textValue();
            country.setAttitude(nationalIdeas.getAttitudes().get(attitude));
            JsonNode setupNode = countryJson.get(this.defaultDate);
            if (setupNode.has("head_of_state") && setupNode.get("head_of_state") != null &&
                    setupNode.has("head_of_government") && setupNode.get("head_of_government") != null) {
                int idMinisterHeadOfState = setupNode.get("head_of_state").intValue();
                int idMinisterHeadOfGovernment = setupNode.get("head_of_government").intValue();
                country.setHeadOfState(idMinisterHeadOfState);
                country.setHeadOfGovernment(idMinisterHeadOfGovernment);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readProvinceBitmap(IntObjectMap<Province> provincesByColor) {
        Pixmap bitmap = new Pixmap(Gdx.files.internal(this.mapPath + "provinces.bmp"));
        for (short y = 0; y < bitmap.getHeight(); y++) {
            for (short x = 0; x < bitmap.getWidth(); x++) {
                int color = bitmap.getPixel(x, y);
                Province province = provincesByColor.get(color);
                if(province instanceof LandProvince landProvince) {
                    landProvince.addPixel(x, y);
                }
            }
        }
        bitmap.dispose();
    }

    private Map<String, Government> readGovernmentsJson() {
        Map<String, Government> governments = new ObjectObjectMap<>();
        try {
            JsonNode governmentsJson = this.openJson(this.governmentJsonFile);
            governmentsJson.fields().forEachRemaining(entry -> {
                String governmentName = entry.getKey();
                List<String> ideologiesAcceptance = new ObjectList<>();
                entry.getValue().get("ideologies_acceptance").forEach(ideology -> ideologiesAcceptance.add(ideology.asText()));

                JsonNode electionNode = entry.getValue().path("election");
                if(!electionNode.isEmpty()) {
                    boolean headOfState = electionNode.get("head_of_state").asBoolean();
                    boolean headOfGovernment = electionNode.get("head_of_government").asBoolean();
                    short duration = electionNode.get("duration").shortValue();
                    Election election = new Election(headOfState, headOfGovernment, duration);
                    governments.put(governmentName, new Government(governmentName, ideologiesAcceptance, election));
                } else {
                    governments.put(governmentName, new Government(governmentName, ideologiesAcceptance));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return governments;
    }

    private Map<String, Ideology> readIdeologiesJson() {
        Map<String, Ideology> ideologies = new ObjectObjectMap<>();
        try {
            JsonNode ideologiesJson = this.openJson(this.ideologiesJsonFile);
            ideologiesJson.fields().forEachRemaining(entry -> {
                String ideologyName = entry.getKey();
                JsonNode ideologyNode = entry.getValue();
                int color = this.parseColor(ideologyNode.get("color"));
                short factionDriftingSpeed = ideologyNode.get("faction_drifting_speed").shortValue();
                ideologies.put(ideologyName, new Ideology(ideologyName, color, factionDriftingSpeed));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ideologies;
    }

    private NationalIdeas readNationalIdeasJson() {
        try {
            JsonNode nationalIdeasJson = this.openJson(this.nationalIdeasJsonFile);

            Map<String, Culture> cultures = new ObjectObjectMap<>();
            nationalIdeasJson.get("national_culture").fields().forEachRemaining(culture -> {
                String name = culture.getKey();
                JsonNode cultureValue = culture.getValue();
                int color = this.parseColor(cultureValue.get("color"));
                cultures.put(name, new Culture(name, color));
            });

            Map<String, Religion> religions = new ObjectObjectMap<>();
            nationalIdeasJson.get("national_religion").fields().forEachRemaining(religion -> {
                String name = religion.getKey();
                JsonNode religionValue = religion.getValue();
                int color = this.parseColor(religionValue.get("color"));
                religionValue = ((ObjectNode) religionValue).remove("color");
                List<Modifier> modifiers = new ObjectList<>();
                religionValue.fields().forEachRemaining(modifier -> {
                    String modifierName = modifier.getKey();
                    float modifierValue = modifier.getValue().floatValue();
                    modifiers.add(new Modifier(modifierName, modifierValue));
                });
                religions.put(name, new Religion(name, color, modifiers));
            });

            Map<String, Identity> identities = new ObjectObjectMap<>();
            nationalIdeasJson.get("national_identity").fields().forEachRemaining(identity -> {
                String name = identity.getKey();
                JsonNode identityValue = identity.getValue();
                List<Modifier> modifiers = new ObjectList<>();
                identityValue.fields().forEachRemaining(modifier -> {
                    String modifierName = modifier.getKey();
                    float modifierValue = modifier.getValue().floatValue();
                    modifiers.add(new Modifier(modifierName, modifierValue));
                });
                identities.put(name, new Identity(name, modifiers));
            });

            Map<String, Attitude> attitudes = new ObjectObjectMap<>();
            nationalIdeasJson.get("national_attitude").fields().forEachRemaining(attitude -> {
                String name = attitude.getKey();
                JsonNode attitudeValue = attitude.getValue();
                List<Modifier> modifiers = new ObjectList<>();
                attitudeValue.fields().forEachRemaining(modifier -> {
                    String modifierName = modifier.getKey();
                    float modifierValue = modifier.getValue().floatValue();
                    modifiers.add(new Modifier(modifierName, modifierValue));
                });
                attitudes.put(name, new Attitude(name, modifiers));
            });

            return new NationalIdeas(cultures, religions, identities, attitudes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, Good> readGoodsJson() {
        Map<String, Good> goods = new ObjectObjectMap<>();
        try {
            JsonNode goodsJson = this.openJson(this.goodsJsonFile);
            JsonNode foodJson = goodsJson.get("food");
            foodJson.fields().forEachRemaining(entry -> {
                String goodName = entry.getKey();
                JsonNode goodNode = entry.getValue();
                float production = goodNode.get("base_production").floatValue();
                float infraProduction = goodNode.get("infra_production").floatValue();
                short basePopulation = goodNode.get("base_pop").shortValue();
                short infraPopulation = goodNode.get("infra_pop").shortValue();
                float cost = goodNode.get("cost").floatValue();
                int color = this.parseColor(goodNode.get("color"));
                goods.put(goodName, new Food(goodName, production, infraProduction, basePopulation, infraPopulation, cost, color));
            });
            JsonNode naturalResourcesJson = goodsJson.get("natural_resources");
            naturalResourcesJson.fields().forEachRemaining(entry -> {
                String naturalResourcesName = entry.getKey();
                JsonNode naturalResourcesNode = entry.getValue();
                float production = naturalResourcesNode.get("base_production").floatValue();
                float infraProduction = naturalResourcesNode.get("infra_production").floatValue();
                short basePopulation = naturalResourcesNode.get("base_pop").shortValue();
                short infraPopulation = naturalResourcesNode.get("infra_pop").shortValue();
                float cost = naturalResourcesNode.get("cost").floatValue();
                int color = this.parseColor(naturalResourcesNode.get("color"));
                short priority = naturalResourcesNode.get("priority").shortValue();
                goods.put(naturalResourcesName, new NaturalResource(naturalResourcesName, production, infraProduction, basePopulation, infraPopulation, cost, color, priority));
            });

            JsonNode energyJson = goodsJson.get("energy");
            energyJson.fields().forEachRemaining(entry -> {
                String energyName = entry.getKey();
                JsonNode energyNode = entry.getValue();
                float cost = energyNode.get("cost").floatValue();
                int color = this.parseColor(energyNode.get("color"));
                goods.put(energyName, new Energy(energyName, cost, color));
            });

            JsonNode advancedGoodsJson = goodsJson.get("advanced_goods");
            advancedGoodsJson.fields().forEachRemaining(entry -> {
                String advancedGoodsName = entry.getKey();
                JsonNode advancedGoodsNode = entry.getValue();
                float cost = advancedGoodsNode.get("cost").floatValue();
                int color = this.parseColor(advancedGoodsNode.get("color"));
                goods.put(advancedGoodsName, new AdvancedGood(advancedGoodsName, cost, color));
            });

            JsonNode militaryGoodsJson = goodsJson.get("military_goods");
            militaryGoodsJson.fields().forEachRemaining(entry -> {
                String militaryGoodsName = entry.getKey();
                JsonNode militaryGoodsNode = entry.getValue();
                float cost = militaryGoodsNode.get("cost").floatValue();
                int color = this.parseColor(militaryGoodsNode.get("color"));
                goods.put(militaryGoodsName, new MilitaryGood(militaryGoodsName, cost, color));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return goods;
    }

    private IntObjectMap<PopulationType> readPopulationTypesJson() {
        IntObjectMap<PopulationType> populationTypes = new IntObjectMap<>();
        try {
            JsonNode populationTypesJson = this.openJson(this.populationTypesJsonFile);
            populationTypesJson.fields().forEachRemaining(entry -> {
                short template = Short.parseShort(entry.getKey());
                JsonNode populationValuesTypeNode = entry.getValue().get("value");
                float children = populationValuesTypeNode.get(0).floatValue();
                float adults = populationValuesTypeNode.get(1).floatValue();
                float seniors = populationValuesTypeNode.get(2).floatValue();
                populationTypes.put(template, new PopulationType(template, children, adults, seniors));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return populationTypes;
    }

    private PopulationDemands readPopulationDemandsJson(Map<String, Good> goods) {
        Map<Good, Float> populationDemands = new ObjectObjectMap<>();
        try {
            JsonNode populationDemandsJson = this.openJson(this.populationDemandsJsonFile);
            short amount = populationDemandsJson.get("amount").shortValue();
            ((ObjectNode) populationDemandsJson).remove("amount");
            populationDemandsJson.fields().forEachRemaining(entry -> {
                Good good = goods.get(entry.getKey());
                populationDemands.put(good, entry.getValue().floatValue());
            });

            return new PopulationDemands(amount, populationDemands);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Map<String, MinisterType> readMinisterTypesJson() {
        Map<String, MinisterType> ministerTypes = new ObjectObjectMap<>();
        try {
            JsonNode ministerTypesJson = this.openJson(this.ministerTypesJsonFile);
            ministerTypesJson.fields().forEachRemaining(entry -> {
                String ministerTypeName = entry.getKey();
                List<Modifier> modifiers = new ObjectList<>();
                entry.getValue().fields().forEachRemaining(modifierEntry -> {
                    String modifierName = modifierEntry.getKey();
                    float modifierValue = modifierEntry.getValue().floatValue();
                    modifiers.add(new Modifier(modifierName, modifierValue));
                });
                ministerTypes.put(ministerTypeName, new MinisterType(ministerTypeName, modifiers));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ministerTypes;
    }


    private Map<String, Building> readBuildingsJson(Map<String, Good> goods) {
        Map<String, Building> buildings = new ObjectObjectMap<>();
        try {
            JsonNode buildingsJson = this.openJson(this.buildingsJsonFile);
            JsonNode economyBuilding = buildingsJson.get("economy_building");
            economyBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").booleanValue();
                boolean visibility = entry.getValue().get("visibility").booleanValue();
                int workforce = entry.getValue().get("workforce").intValue();
                int color = this.parseColor(entry.getValue().get("color"));
                short maxLevel = entry.getValue().get("max_level").shortValue();
                JsonNode inputGoodsNode = entry.getValue().get("input_goods");
                Map<Good, Integer> inputGoods = new ObjectObjectMap<>();
                inputGoodsNode.fields().forEachRemaining(inputGood -> {
                    Good good = goods.get(inputGood.getKey());
                    inputGoods.put(good, inputGood.getValue().asInt());
                });
                JsonNode outputGoodsNode = entry.getValue().get("output_goods");
                Map<Good, Integer> outputGoods = new ObjectObjectMap<>();
                outputGoodsNode.fields().forEachRemaining(outputGood -> {
                    Good good = goods.get(outputGood.getKey());
                    outputGoods.put(good, outputGood.getValue().asInt());
                });
                buildings.put(buildingName, new EconomyBuilding(buildingName, cost, time, onMap, visibility, workforce, inputGoods, outputGoods, maxLevel, color));
            });

            JsonNode specialBuilding = buildingsJson.get("special_building");
            specialBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").booleanValue();
                boolean visibility = entry.getValue().get("visibility").booleanValue();
                JsonNode modifiersNode = entry.getValue().get("modifier");
                if(modifiersNode == null) {
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time, onMap, visibility));
                } else {
                    List<Modifier> modifiers = new ObjectList<>();
                    modifiersNode.fields().forEachRemaining(modifierEntry -> {
                        String modifierName = modifierEntry.getKey();
                        JsonNode modifierValue = modifierEntry.getValue();
                        if (modifierValue.isNumber()) {
                            modifiers.add(new Modifier(modifierName, modifierValue.floatValue()));
                        } else {
                            float value = modifierValue.get("value").floatValue();
                            String modifierType = modifierValue.get("type").asText();
                            modifiers.add(new Modifier(modifierName, value, modifierType));
                        }
                    });
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time, onMap, visibility, modifiers));
                }
            });

            JsonNode developmentBuilding = buildingsJson.get("development_building");
            developmentBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").booleanValue();
                boolean visibility = entry.getValue().get("visibility").booleanValue();
                short maxLevel = entry.getValue().get("max_level").shortValue();
                JsonNode modifierNode = entry.getValue().get("modifier");
                if(modifierNode == null) {
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, onMap, visibility, maxLevel));
                } else if(modifierNode.size() == 1) {
                    String modifierName = modifierNode.fieldNames().next();
                    float modifierValue = modifierNode.get(modifierName).floatValue();
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, onMap, visibility, maxLevel, new Modifier(modifierName, modifierValue)));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildings;
    }

    private void readPositionsJson(IntMap<Province> provinces) {
        try {
            JsonNode positionsJson = this.openJson(this.positionsJsonFile);
            positionsJson.fields().forEachRemaining(entry -> {
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                entry.getValue().fields().forEachRemaining(position -> {
                    String name = position.getKey();
                    JsonNode positionNode = position.getValue();
                    short x = positionNode.get("x").shortValue();
                    short y = positionNode.get("y").shortValue();
                    province.addPosition(name, (x << 16) | (y & 0xFFFF));
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAdjenciesJson(IntMap<Province> provinces) {
        try {
            JsonNode adjenciesJson = this.openJson(this.adjenciesJsonFile);
            adjenciesJson.fields().forEachRemaining(entry -> {
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                entry.getValue().forEach(adjacency -> province.addAdjacentProvinces(provinces.get(adjacency.shortValue())));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
