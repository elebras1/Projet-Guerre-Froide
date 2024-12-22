package com.populaire.projetguerrefroide.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.economy.PopulationDemands;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.building.DevelopmentBuilding;
import com.populaire.projetguerrefroide.economy.building.EconomyBuilding;
import com.populaire.projetguerrefroide.economy.building.SpecialBuilding;
import com.populaire.projetguerrefroide.economy.good.*;
import com.populaire.projetguerrefroide.entity.*;
import com.populaire.projetguerrefroide.map.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class DataManager {
    private final String commonPath = "common/";
    private final String mapPath = "map/";
    private final String historyPath = "history/";
    private final String localisationPath = "localisation/";
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
    private final String goodsJsonFile = this.commonPath + "goods.json";
    private final String populationDemandsJsonFile = this.commonPath + "population_demands.json";
    private final String ministerTypesJsonFile = this.commonPath + "minister_types.json";
    private final String buildingsJsonFile = this.commonPath + "buildings.json";
    private final String bookmarkJsonFile = this.commonPath + "bookmark.json";
    private final String provinceNamesCsvFile = this.localisationPath + "province_names.csv";
    private final String mainmenuCsvFile = this.localisationPath + "mainmenu.csv";
    private final String mainemenuInGameCsvFile = this.localisationPath + "mainmenu_ig.csv";
    private final String newgameCsvFile = this.localisationPath + "newgame.csv";
    private final String bookmarkCsvFile = this.localisationPath + "bookmark.csv";
    private final String politicsCsvFile = this.localisationPath + "politics.csv";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String defaultDate = "1946.1.1";

    public World createWorldAsync() {
        Map<String, Country> countries = this.loadCountries();
        IntObjectMap<Province> provinces = this.loadProvinces(countries);
        Map<String, Government> governments = this.readGovernmentsJson();
        Map<String, Ideology> ideologies = this.readIdeologiesJson();
        Map<String, Good> goods = this.readGoodsJson();
        PopulationDemands populationDemands = this.readPopulationDemandsJson(goods);
        Map<String, Building> buildings = this.readBuildingsJson(goods);
        Map<String, MinisterType> ministerTypes = this.readMinisterTypesJson();

        AtomicReference<World> worldRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Gdx.app.postRunnable(() -> {
            worldRef.set(new World(new ObjectList<>(countries.values()), provinces));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return worldRef.get();
    }

    private Map<String, Country> loadCountries() {
        return this.readCountriesJson();
    }

    private IntObjectMap<Province> loadProvinces(Map<String, Country> countries) {
        IntObjectMap<Province> provincesByColor = new IntObjectMap<>(20000);
        IntMap<Province> provinces = this.readProvincesJson(countries);
        this.readRegionJson(provinces);
        this.readDefinitionCsv(provinces, provincesByColor);
        this.readProvinceBitmap(provincesByColor);
        this.readProvinceNamesCsv(provinces);
        this.readCountriesHistoryJson(countries, provinces);
        this.readContinentJsonFile(provinces);
        this.readAdjenciesJson(provinces);

        return provincesByColor;
    }

    private JsonNode openJson(String fileName) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return this.mapper.readTree(fileHandle.readString());
    }

    private Map<String, Country> readCountriesJson() {
        Map<String, Country> countries = new ObjectObjectMap<>();
        try {
            JsonNode countriesJson = this.openJson(this.countriesJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.commonPath + entry.getValue().asText();
                countries.put(entry.getKey(), readCountryJson(countryFileName, entry.getKey(), parseFileName(countryFileName)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countries;
    }

    private String parseFileName(String path) {
        return path.replace("countries/", "").replace(".json", "");
    }

    private Country readCountryJson(String countryFileName, String countryId, String countryName) {
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
                    String headOfState = null;
                    if(ministerNode.has("head_of_state") && !ministerNode.get("head_of_state").isNull()) {
                        headOfState = ministerNode.get("head_of_state").asText();
                    }
                    String headOfGovernment = null;
                    if(ministerNode.has("head_of_government") && !ministerNode.get("head_of_government").isNull()) {
                        headOfGovernment = ministerNode.get("head_of_government").asText();
                    }
                    Date startDate = null;
                    Date deathDate = null;
                    try {
                        startDate = dateFormat.parse(ministerNode.get("start_date").asText());
                        deathDate = dateFormat.parse(ministerNode.get("death_date").asText());
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                    int base = -1;
                    if(ministerNode.has("base") && !ministerNode.get("base").isNull()) {
                        base = ministerNode.get("base").asInt();
                    }

                    Minister minister = new Minister(name, ideology, imageNameFile, loyalty, headOfState, headOfGovernment, startDate, deathDate, base);
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

    private IntMap<Province> readProvincesJson(Map<String, Country> countries) {
        IntMap<Province> provinces = new IntMap<>(20000);
        try {
            JsonNode provincesJson = this.openJson(this.provincesJsonFile);
            provincesJson.fields().forEachRemaining(entry -> {
                String provinceFileName = this.historyPath + entry.getValue().asText();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = readProvinceJson(countries, provinceFileName, provinceId);
                provinces.put(provinceId, province);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return provinces;
    }

    private LandProvince readProvinceJson(Map<String, Country> countries, String provinceFileName, short provinceId) {
        try {
            JsonNode rootNode = this.openJson(provinceFileName);
            String owner = rootNode.path("owner").asText();
            String controller = rootNode.path("controller").asText();
            Country countryOwner = countries.get(owner);
            Country countryController = countries.get(controller);
            JsonNode dateNode = rootNode.path(this.defaultDate);
            JsonNode populationNode = dateNode.path("population_total");
            int amount = populationNode.get("amount").asInt();
            int template = populationNode.get("template").asInt();
            Population population = new Population(amount, template);
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

    private void readCountriesHistoryJson(Map<String, Country> countries, IntMap<Province> provinces) {
        try {
            JsonNode countriesJson = this.openJson(this.countriesHistoryJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.historyPath + entry.getValue().textValue();
                this.readCountryHistoryJson(countries, countryFileName, entry.getKey(), provinces);
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

    private void readCountryHistoryJson(Map<String, Country> countries, String countryFileName, String idCountry, IntMap<Province> provinces) {
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
            country.setGovernment(government);
            String ideology = countryJson.get("ideology").textValue();
            country.setIdeology(ideology);
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

    private void readProvinceNamesCsv(IntMap<Province> provinces) {
        try (BufferedReader br = new BufferedReader(new StringReader(Gdx.files.internal(this.provinceNamesCsvFile).readString()))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = Arrays.asList(headers).indexOf("ENGLISH");
            if (localisationIndex == -1) {
                throw new IllegalArgumentException("Localisation " + "ENGLISH" + " not found in CSV headers.");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (values.length > localisationIndex && !values[0].isEmpty()) {
                    short provinceId = Short.parseShort(values[0]);
                    Province province = provinces.get(provinceId);
                    String provinceName = values[localisationIndex];
                    province.setName(provinceName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                int cost = entry.getValue().get("cost").asInt();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").asBoolean();
                boolean visibility = entry.getValue().get("visibility").asBoolean();
                int workforce = entry.getValue().get("workforce").asInt();
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
                int cost = entry.getValue().get("cost").asInt();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").asBoolean();
                boolean visibility = entry.getValue().get("visibility").asBoolean();
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
                int cost = entry.getValue().get("cost").asInt();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").asBoolean();
                boolean visibility = entry.getValue().get("visibility").asBoolean();
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

    private Map<Integer, Vector2> readPositionsJson() {
        Map<Integer, Vector2> unitPositions = new ObjectObjectMap<>();
        try {
            JsonNode positionsJson = this.openJson(this.positionsJsonFile);
            positionsJson.fields().forEachRemaining(entry -> {
                JsonNode buildingNode = entry.getValue().path("building_construction");
                unitPositions.put(Integer.parseInt(entry.getKey()), new Vector2(buildingNode.get("x").asInt(), buildingNode.get("y").asInt()));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return unitPositions;
    }

    public Bookmark readBookmarkJson() {
        Bookmark bookmark = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            JsonNode rootNode = this.openJson(this.bookmarkJsonFile);
            JsonNode bookmarkNode = rootNode.get("bookmark");
            String iconNameFile = bookmarkNode.get("icon").asText();
            String nameId = bookmarkNode.get("name").asText();
            String descriptionId = bookmarkNode.get("desc").asText();
            Date date = null;
            try {
                date = dateFormat.parse(bookmarkNode.get("date").asText());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            List<String> countriesId = new ObjectList<>();
            JsonNode countriesNode = bookmarkNode.get("country");
            if (countriesNode != null && countriesNode.isArray()) {
                countriesNode.forEach(countryId -> countriesId.add(countryId.asText()));
            }

            bookmark = new Bookmark(iconNameFile, nameId, descriptionId, date, countriesId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookmark;
    }

    public Map<String, String> readMainMenuLocalisationCsv() {
        return readLocalisationCsv(this.mainmenuCsvFile);
    }

    public Map<String, String> readNewgameLocalisationCsv() {
        return readLocalisationCsv(this.newgameCsvFile);
    }

    public Map<String, String> readBookmarkLocalisationCsv() {
        return readLocalisationCsv(this.bookmarkCsvFile);
    }

    public Map<String, String> readPoliticsLocalisationCsv() {
        return readLocalisationCsv(this.politicsCsvFile);
    }

    public Map<String, String> readMainMenuInGameCsv() { return readLocalisationCsv(this.mainemenuInGameCsvFile); }

    private Map<String, String> readLocalisationCsv(String filename) {
        Map<String, String> localisation = new ObjectObjectMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Gdx.files.internal(filename).read(), StandardCharsets.UTF_8))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = Arrays.asList(headers).indexOf("ENGLISH");
            if (localisationIndex == -1) {
                throw new IllegalArgumentException("Localisation not found in CSV headers.");
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                String code = values[0];
                String translation = values[localisationIndex];
                localisation.put(code, translation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localisation;
    }

    private void readAdjenciesJson(IntMap<Province> provinces) {
        try {
            JsonNode adjenciesJson = this.openJson(this.adjenciesJsonFile);
            adjenciesJson.fields().forEachRemaining(entry -> {
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                List<Province> adjacencies = new ObjectList<>();
                entry.getValue().forEach(adjacency -> adjacencies.add(provinces.get(adjacency.shortValue())));
                province.setAdjacentProvinces(adjacencies);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
