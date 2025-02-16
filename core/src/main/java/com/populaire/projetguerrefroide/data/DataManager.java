package com.populaire.projetguerrefroide.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.economy.building.*;
import com.populaire.projetguerrefroide.economy.population.Population;
import com.populaire.projetguerrefroide.economy.good.*;
import com.populaire.projetguerrefroide.economy.population.PopulationTemplate;
import com.populaire.projetguerrefroide.economy.population.PopulationType;
import com.populaire.projetguerrefroide.entity.*;
import com.populaire.projetguerrefroide.map.*;
import com.populaire.projetguerrefroide.national.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DataManager {
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
    private final ObjectMapper mapper = new ObjectMapper();
    private final String defaultDate = "1946.1.1";

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
        return new GameEntities(nationalIdeas, governments, ideologies, goods, buildings, populationTypes, ministerTypes, this.readPopulationTemplatesJson(), terrains);
    }

    public World createWorldThreadSafe(GameEntities gameEntities, AsyncExecutor asyncExecutor) {
        Map<String, Country> countries = this.loadCountries(gameEntities.getMinisterTypes(), gameEntities.getIdeologies());
        IntObjectMap<LandProvince> provincesByColor = new IntObjectMap<>(15000);
        IntObjectMap<WaterProvince> waterProvincesByColor = new IntObjectMap<>(4000);
        this.loadProvinces(countries, provincesByColor, waterProvincesByColor, gameEntities);

        AtomicReference<World> worldRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Gdx.app.postRunnable(() -> {
            worldRef.set(new World(new ObjectList<>(countries.values()), provincesByColor, waterProvincesByColor, asyncExecutor));
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
        Map<String, Country> countries = this.readCountriesJson(ministerTypes, ideologies);
        this.readRelationJson(countries);
        this.readAlliancesJson(countries);
        return countries;
    }

    private void loadProvinces(Map<String, Country> countries, IntObjectMap<LandProvince> provincesByColor, IntObjectMap<WaterProvince> waterProvincesByColor, GameEntities gameEntities) {
        IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince = new IntObjectMap<>();
        IntObjectMap<Province> provinces = this.readProvincesJson(countries, gameEntities, regionBuildingsByProvince);
        this.readRegionJson(provinces, regionBuildingsByProvince);
        this.readDefinitionCsv(provinces, provincesByColor, waterProvincesByColor);
        this.readProvinceBitmap(provincesByColor);
        this.readCountriesHistoryJson(countries, provinces, gameEntities);
        this.readContinentJsonFile(provinces);
        this.readPositionsJson(provinces);
        this.readAdjenciesJson(provinces);
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
        } catch (Exception e) {
            System.out.println("loadProvinces : " + e);
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
                        System.out.println("readCountryJson : " + e);
                    }

                    Minister minister = new Minister(name, ideologies.get(ideology), imageNameFile, loyalty, ministerTypes.get(type), startDate, deathDate);
                    country.addMinister(ministerId, minister);
                });
            }
            return country;
        } catch (Exception e) {
            System.out.println("readCountryJson : " + e);
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

    private IntObjectMap<Province> readProvincesJson(Map<String, Country> countries, GameEntities gameEntities, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince) {
        IntObjectMap<Province> provinces = new IntObjectMap<>(20000);
        try {
            JsonNode provincesJson = this.openJson(this.provincesJsonFile);
            provincesJson.fields().forEachRemaining(entry -> {
                String provinceFileName = this.historyPath + entry.getValue().asText();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = this.readProvinceJson(countries, provinceFileName, provinceId, gameEntities, regionBuildingsByProvince);
                provinces.put(provinceId, province);
            });
        } catch (Exception e) {
            System.out.println("readProvincesJson : " + e);
        }

        return provinces;
    }

    private LandProvince readProvinceJson(Map<String, Country> countries, String provinceFileName, short provinceId, GameEntities gameEntities, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince) {
        try {
            JsonNode rootNode = this.openJson(provinceFileName);

            List<Country> countriesCore = new ObjectList<>();
            JsonNode addCoreNode = rootNode.get("add_core");
            if (addCoreNode.isArray()) {
                addCoreNode.forEach(countryCore -> countriesCore.add(countries.get(countryCore.asText())));
            } else if (addCoreNode.isTextual()) {
                countriesCore.add(countries.get(addCoreNode.asText()));
            }

            String owner = rootNode.get("owner").textValue();
            Country countryOwner = countries.get(owner);

            String controller = rootNode.get("controller").textValue();
            Country countryController = countries.get(controller);

            String terrain = rootNode.get("terrain").textValue();
            Terrain provinceTerrain = gameEntities.getTerrains().get(terrain);

            JsonNode populationNode = rootNode.get("population_total");
            int amount = populationNode.get("amount").intValue();
            short template = populationNode.get("template").shortValue();
            Population population = new Population(amount, gameEntities.getPopulationTemplates().get(template));

            ObjectIntMap<Building> buildingsRegion;
            JsonNode buildingsNode = rootNode.get("economy_buildings");
            if(buildingsNode != null) {
                buildingsRegion = new ObjectIntMap<>();
                buildingsNode.forEach(building -> {
                    String buildingName = building.get("name").textValue();
                    short size = building.get("size").shortValue();
                    buildingsRegion.put(gameEntities.getBuildings().get(buildingName), size);

                });
                regionBuildingsByProvince.put(provinceId, buildingsRegion);
            }

            ResourceGood resourceGood = null;
            JsonNode goodNode = rootNode.get("good");
            if(goodNode != null) {
                resourceGood = (ResourceGood) gameEntities.getGoods().get(goodNode.textValue());
            }

            ObjectIntMap<Building> buildingsProvince = new ObjectIntMap<>();
            JsonNode buildingsProvinceNode = rootNode.get("buildings");
            if(buildingsProvinceNode != null) {
                buildingsProvinceNode.forEach(building -> {
                    String buildingName = building.get("name").textValue();
                    short size = building.get("size").shortValue();
                    buildingsProvince.put(gameEntities.getBuildings().get(buildingName), size);
                });
            }

            LandProvince province = new LandProvince(provinceId, countryOwner, countryController, population, provinceTerrain, countriesCore, resourceGood, buildingsProvince);
            countryOwner.addProvince(province);
            return province;
        } catch (Exception e) {
            System.out.println("readProvinceJson : " + e);
        }
        return null;
    }

    private void readRegionJson(IntObjectMap<Province> provinces, IntObjectMap<ObjectIntMap<Building>> regionBuildingsByProvince) {
        try {
            AtomicInteger total = new AtomicInteger();
            JsonNode rootNode = this.openJson(this.regionJsonFiles);
            rootNode.fields().forEachRemaining(regionData -> {
                Region region = new Region(regionData.getKey());
                regionData.getValue().forEach(provinceId -> {
                    LandProvince province = (LandProvince) provinces.get(provinceId.shortValue());
                    if(province != null) {
                        province.getCountryController().addRegion(region);
                        province.setRegion(region);
                        region.addProvince(province);
                        ObjectIntMap<Building> regionBuildings = regionBuildingsByProvince.get(province.getId());
                        if(regionBuildings != null) {
                            province.getRegion().addAllBuildings(regionBuildings);
                        }
                    } else {
                        WaterProvince waterProvince = new WaterProvince(provinceId.shortValue());
                        provinces.put(provinceId.shortValue(), waterProvince);
                    }
                });
                if(!region.getBuildings().isEmpty()) {
                    total.set(total.get() + 1);
                }
            });
        } catch (Exception e) {
            System.out.println("readRegionJson : " + e);
        }
    }

    private void readDefinitionCsv(IntObjectMap<Province> provinces, IntObjectMap<LandProvince> provincesByColor, IntObjectMap<WaterProvince> waterProvincesByColor) {
        try (BufferedReader br = new BufferedReader(new StringReader(Gdx.files.internal(this.definitionCsvFile).readString()))) {
            String line;
            while ((line = br.readLine()) != null) {
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
        } catch (Exception e) {
            System.out.println("readDefinitionCsv : " + e);
        }
    }

    private void readCountriesHistoryJson(Map<String, Country> countries, IntObjectMap<Province> provinces, GameEntities gameEntities) {
        try {
            JsonNode countriesJson = this.openJson(this.countriesHistoryJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.historyPath + entry.getValue().textValue();
                this.readCountryHistoryJson(countries, countryFileName, entry.getKey(), provinces, gameEntities);
            });
        } catch (Exception e) {
            System.out.println("readCountriesHistoryJson : " + e);
        }
    }

    private void readContinentJsonFile(IntObjectMap<Province> provinces) {
        try {
            JsonNode continentJson = this.openJson(this.continentJsonFile);
            continentJson.fields().forEachRemaining(entry -> {
                Continent continent = new Continent(entry.getKey());
                entry.getValue().forEach(provinceId -> {
                    LandProvince province = (LandProvince) provinces.get(provinceId.shortValue());
                    province.setContinent(continent);
                });
            });
        } catch (Exception e) {
            System.out.println("readContinentJsonFile : " + e);
        }
    }

    private void readCountryHistoryJson(Map<String, Country> countries, String countryFileName, String idCountry, IntObjectMap<Province> provinces, GameEntities gameEntities) {
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
            country.setGovernment(gameEntities.getGovernments().get(government));
            String ideology = countryJson.get("ideology").textValue();
            country.setIdeology(gameEntities.getIdeologies().get(ideology));
            String culture = countryJson.get("national_culture").textValue();
            country.setCulture(gameEntities.getNationalIdeas().getCultures().get(culture));
            String identity = countryJson.get("national_identity").textValue();
            country.setIdentity(gameEntities.getNationalIdeas().getIdentities().get(identity));
            String religion = countryJson.get("national_religion").textValue();
            country.setReligion(gameEntities.getNationalIdeas().getReligions().get(religion));
            String attitude = countryJson.get("national_attitude").textValue();
            country.setAttitude(gameEntities.getNationalIdeas().getAttitudes().get(attitude));
            JsonNode setupNode = countryJson.get(this.defaultDate);
            if (setupNode.has("head_of_state") && setupNode.get("head_of_state") != null &&
                    setupNode.has("head_of_government") && setupNode.get("head_of_government") != null) {
                int idMinisterHeadOfState = setupNode.get("head_of_state").intValue();
                int idMinisterHeadOfGovernment = setupNode.get("head_of_government").intValue();
                country.setHeadOfState(idMinisterHeadOfState);
                country.setHeadOfGovernment(idMinisterHeadOfGovernment);
            }
        } catch (Exception e) {
            System.out.println("readCountryHistoryJson : " + e);
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

    private Map<String, Government> readGovernmentsJson() {
        Map<String, Government> governments = new ObjectObjectMap<>();
        try {
            JsonNode governmentsJson = this.openJson(this.governmentJsonFile);
            governmentsJson.fields().forEachRemaining(entry -> {
                String governmentName = entry.getKey();
                List<String> ideologiesAcceptance = new ObjectList<>();
                entry.getValue().get("ideologies_acceptance").forEach(ideology -> ideologiesAcceptance.add(ideology.asText()));

                JsonNode electionNode = entry.getValue().get("election");
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
        } catch (Exception e) {
            System.out.println("readGovernmentsJson : " + e);
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
        } catch (Exception e) {
            System.out.println("readIdeologiesJson : " + e);
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
        } catch (Exception e) {
            System.out.println("readNationalIdeasJson : " + e);
        }

        return null;
    }

    private Map<String, Good> readGoodsJson() {
        Map<String, Good> goods = new ObjectObjectMap<>();
        try {
            JsonNode goodsJson = this.openJson(this.goodsJsonFile);
            JsonNode resourceGoods = goodsJson.get("resource_goods");
            resourceGoods.fields().forEachRemaining(entry -> {
                String goodName = entry.getKey();
                JsonNode goodNode = entry.getValue();
                float cost = goodNode.get("cost").floatValue();
                float value = goodNode.get("value").floatValue();
                int color = this.parseColor(goodNode.get("color"));
                goods.put(goodName, new ResourceGood(goodName, cost, color, value));
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
        } catch (Exception e) {
            System.out.println("readGoodsJson : " + e);
        }

        return goods;
    }

    private IntObjectMap<PopulationTemplate> readPopulationTemplatesJson() {
        IntObjectMap<PopulationTemplate> populationTemplates = new IntObjectMap<>();
        try {
            JsonNode populationTemplatesJson = this.openJson(this.populationTemplatesJsonFile);
            populationTemplatesJson.fields().forEachRemaining(entry -> {
                short template = Short.parseShort(entry.getKey());
                JsonNode populationValuesNode = entry.getValue().get("value");
                float children = populationValuesNode.get(0).floatValue();
                float adults = populationValuesNode.get(1).floatValue();
                float seniors = populationValuesNode.get(2).floatValue();
                populationTemplates.put(template, new PopulationTemplate(template, children, adults, seniors));
            });
        } catch (Exception e) {
            System.out.println("readPopulationTemplatesJson : " + e);
        }

        return populationTemplates;
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
        } catch (Exception e) {
            System.out.println("readMinisterTypesJson : " + e);
        }

        return ministerTypes;
    }

    private Map<String, Terrain> readTerrainsJson() {
        Map<String, Terrain> terrains = new ObjectObjectMap<>();
        try {
            JsonNode terrainsJson = this.openJson(this.terrainJsonFile);
            terrainsJson.fields().forEachRemaining(entry -> {
                String terrainName = entry.getKey();
                byte movementCost = (byte) entry.getValue().get("movement_cost").shortValue();
                byte humidity = (byte) entry.getValue().get("humidity").shortValue();
                byte temperature = (byte) entry.getValue().get("temperature").shortValue();
                byte precipitation = (byte) entry.getValue().get("precipitation").shortValue();
                int color = this.parseColor(entry.getValue().get("color"));
                terrains.put(terrainName, new Terrain(terrainName, movementCost, temperature, humidity, precipitation, color));
            });

            return terrains;
        } catch (Exception e) {
            System.out.println("readTerrainsJson : " + e);
        }

        return terrains;
    }

    private Map<String, ProductionType> readProductionTypesJson(Map<String, PopulationType> populationTypes) {
        Map<String, ProductionType> productionTypes = new ObjectObjectMap<>();
        try {
            JsonNode buildingTypesJson = this.openJson(this.productionTypesJsonFile);
            JsonNode typesEmployeesNode = buildingTypesJson.get("types_employees");
            Map<String, Employee> typeEmployees = new ObjectObjectMap<>();
            typesEmployeesNode.fields().forEachRemaining(entry -> {
                String typeName = entry.getKey();
                PopulationType populationType = populationTypes.get(entry.getValue().get("poptype").asText());
                float amount = entry.getValue().get("amount").floatValue();
                float effectMultiplier = entry.getValue().get("effect_multiplier").floatValue();
                typeEmployees.put(typeName, new Employee(populationType, amount, effectMultiplier));
            });

            JsonNode typesBuildings = buildingTypesJson.get("types_buildings");
            typesBuildings.fields().forEachRemaining(entry -> {
                String typeName = entry.getKey();
                short workforce = entry.getValue().get("workforce").shortValue();
                PopulationType owner = populationTypes.get(entry.getValue().get("owner").get("poptype").asText());
                List<Employee> employees = new ObjectList<>();
                entry.getValue().get("employees").forEach(employee -> {
                    String employeeName = employee.asText();
                    employees.add(typeEmployees.get(employeeName));
                });
                productionTypes.put(typeName, new ProductionType(workforce, owner, employees));
            });

            JsonNode typesRGOs = buildingTypesJson.get("types_rgo");
            typesRGOs.fields().forEachRemaining(entry -> {
                String typeName = entry.getKey();
                short workforce = entry.getValue().get("workforce").shortValue();
                PopulationType owner = populationTypes.get(entry.getValue().get("owner").get("poptype").asText());
                List<Employee> employees = new ObjectList<>();
                entry.getValue().get("employees").forEach(employee -> {
                    String employeeName = employee.asText();
                    employees.add(typeEmployees.get(employeeName));
                });
                productionTypes.put(typeName, new ResourceProductionType(workforce, owner, employees));
            });
        } catch (Exception e) {
            System.out.println("readProductionTypesJson : " + e);
        }

        return productionTypes;
    }

    private Map<String, Building> readBuildingsJson(Map<String, Good> goods, Map<String, ProductionType> productionTypes) {
        Map<String, Building> buildings = new ObjectObjectMap<>();
        try {
            JsonNode buildingsJson = this.openJson(this.buildingsJsonFile);
            JsonNode economyBuilding = buildingsJson.get("economy_building");
            economyBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();
                ProductionType baseType = productionTypes.get(entry.getValue().get("base_type").asText());
                ProductionType artisansType = null;
                if(entry.getValue().has("artisans_type")) {
                    artisansType = productionTypes.get(entry.getValue().get("artisans_type").asText());
                }
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
                buildings.put(buildingName, new EconomyBuilding(baseType, artisansType, buildingName, cost, time, inputGoods, outputGoods, maxLevel, color));
            });

            JsonNode specialBuilding = buildingsJson.get("special_building");
            specialBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();

                JsonNode modifiersNode = entry.getValue().get("modifier");
                if(modifiersNode == null) {
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time));
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
                    buildings.put(buildingName, new SpecialBuilding(buildingName, cost, time, modifiers));
                }
            });

            JsonNode developmentBuilding = buildingsJson.get("development_building");
            developmentBuilding.fields().forEachRemaining(entry -> {
                String buildingName = entry.getKey();
                int cost = entry.getValue().get("cost").intValue();
                short time = entry.getValue().get("time").shortValue();
                boolean onMap = entry.getValue().get("onmap").booleanValue();
                short maxLevel = entry.getValue().get("max_level").shortValue();
                JsonNode modifierNode = entry.getValue().get("modifier");
                if(modifierNode == null) {
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, onMap, maxLevel));
                } else if(modifierNode.size() == 1) {
                    String modifierName = modifierNode.fieldNames().next();
                    float modifierValue = modifierNode.get(modifierName).floatValue();
                    buildings.put(buildingName, new DevelopmentBuilding(buildingName, cost, time, onMap, maxLevel, new Modifier(modifierName, modifierValue)));
                }
            });
        } catch (Exception e) {
            System.out.println("readBuildingsJson : " + e);
        }

        return buildings;
    }

    private void readResourceProductionsJson(Map<String, Good> goods, Map<String, ProductionType> productionTypes) {
        try {
            JsonNode resourceProductionsJson = this.openJson(this.resourceProductionsJsonFile);
            resourceProductionsJson.fields().forEachRemaining(entry -> {
                ResourceGood good = (ResourceGood) goods.get(entry.getKey());
                ResourceProductionType productionType = (ResourceProductionType) productionTypes.get(entry.getValue().get("base_type").asText());
                good.setProductionType(productionType);
            });
        } catch (Exception e) {
            System.out.println("readResourceProductionsJson : " + e);
        }
    }

    private Map<String, PopulationType> readPopulationTypesJson(Map<String, Good> goods) {
        Map<String, PopulationType> populationTypes = new ObjectObjectMap<>();
        try {
            JsonNode populationTypesJson = this.openJson(this.populationTypesJsonFile);
            populationTypesJson.fields().forEachRemaining(entry -> {
                this.readPopulationTypeJson(this.commonPath + entry.getValue().asText(), entry.getKey(), goods, populationTypes);
            });
        } catch (Exception e) {
            System.out.println("readPopulationTypesJson : " + e);
        }

        return populationTypes;
    }

    private void readPopulationTypeJson(String populationTypeFileName, String name, Map<String, Good> goods, Map<String, PopulationType> populationTypes) {
        try {
            JsonNode populationTypeJson = this.openJson(populationTypeFileName);
            int color = this.parseColor(populationTypeJson.get("color"));
            ObjectFloatMap<Good> standardDemands = new ObjectFloatMap<>();
            JsonNode standardDemandsNode = populationTypeJson.get("standard_demands");
            standardDemandsNode.fields().forEachRemaining(standardDemand -> {
                Good good = goods.get(standardDemand.getKey());
                float value = standardDemand.getValue().floatValue();
                standardDemands.put(good, value);
            });
            ObjectFloatMap<Good> luxuryDemands = new ObjectFloatMap<>();
            JsonNode luxuryDemandsNode = populationTypeJson.get("luxury_demands");
            luxuryDemandsNode.fields().forEachRemaining(luxuryDemand -> {
                Good good = goods.get(luxuryDemand.getKey());
                float value = luxuryDemand.getValue().floatValue();
                luxuryDemands.put(good, value);
            });
            populationTypes.put(name, new PopulationType(color, name, standardDemands, luxuryDemands));
        } catch (Exception e) {
            System.out.println("readPopulationTypeJson : " + e);
        }
    }

    private void readRelationJson(Map<String, Country> countries) {
        try {
            JsonNode relationsJson = this.openJson(this.relationJsonFile);
            JsonNode relationArray = relationsJson.get("relation");
            for(JsonNode relation : relationArray) {
                Country country1 = countries.get(relation.get("country1").textValue());
                Country country2 = countries.get(relation.get("country2").textValue());
                int relationValue = relation.get("value").intValue();
                country1.addRelation(country2, relationValue);
                country2.addRelation(country1, relationValue);
            }
        } catch (Exception e) {
            System.out.println("readRelationJson : " + e);
        }
    }

    private void readAlliancesJson(Map<String, Country> countries) {
        try {
            JsonNode alliancesJson = this.openJson(this.alliancesJsonFile);
            JsonNode alliancesArray = alliancesJson.get("alliances");
            for(JsonNode alliance : alliancesArray) {
                Country country1 = countries.get(alliance.get("country1").textValue());
                Country country2 = countries.get(alliance.get("country2").textValue());
                String type = alliance.get("type").textValue();
                country1.addAlliance(country2, AllianceType.getAllianceType(type, true));
                country2.addAlliance(country1, AllianceType.getAllianceType(type, false));
            }
        } catch (Exception e) {
            System.out.println("readAlliancesJson : " + e);
        }
    }

    private void readPositionsJson(IntObjectMap<Province> provinces) {
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
        } catch (Exception e) {
            System.out.println("readPositionsJson : " + e);
        }
    }

    private void readAdjenciesJson(IntObjectMap<Province> provinces) {
        try {
            JsonNode adjenciesJson = this.openJson(this.adjenciesJsonFile);
            adjenciesJson.fields().forEachRemaining(entry -> {
                short provinceId = Short.parseShort(entry.getKey());
                Province province = provinces.get(provinceId);
                entry.getValue().forEach(adjacency -> province.addAdjacentProvinces(provinces.get(adjacency.shortValue())));
            });
        } catch (Exception e) {
            System.out.println("readAdjenciesJson : " + e);
        }
    }
}
