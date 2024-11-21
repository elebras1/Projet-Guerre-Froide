package com.populaire.projetguerrefroide.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.entities.Minister;
import com.populaire.projetguerrefroide.entities.Population;
import com.populaire.projetguerrefroide.map.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataManager {
    private final String basePath = "common/";
    private final String mapPath = "map/";
    private final String historyPath = "history/";
    private final String localisationPath = "localisation/";
    private final String countriesJsonFiles = this.basePath + "countries.json";
    private final String countriesHistoryJsonFiles = this.historyPath + "countries.json";
    private final String regionJsonFiles = this.mapPath + "region.json";
    private final String provincesJsonFile = this.historyPath + "provinces.json";
    private final String definitionCsvFile = this.mapPath + "definition.csv";
    private final String continentJsonFile = this.mapPath + "continent.json";
    private final String positionsJsonFile = this.mapPath + "positions.json";
    private final String adjenciesJsonFile = this.mapPath + "adjacencies.json";
    private final String provinceNamesCsvFile = this.localisationPath + "province_names.csv";
    private final String mainmenuCsvFile = this.localisationPath + "mainmenu.csv";
    private final String newgameCsvFile = this.localisationPath + "newgame.csv";
    private final String bookmarkJsonFile = this.basePath + "bookmark.json";
    private final String bookmarkCsvFile = this.localisationPath + "bookmark.csv";
    private final ObjectMapper mapper = new ObjectMapper();
    private final String defaultDate = "1946.1.1";
    private String localisation = "ENGLISH";

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public World createWorld() {
        Map<String, Country> countries = this.loadCountries();
        IntObjectMap<Province> provinces = this.loadProvinces(countries);
        return new World(new ObjectList<>(countries.values()), provinces);
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
        return mapper.readTree(fileHandle.readString());
    }

    private Map<String, Country> readCountriesJson() {
        Map<String, Country> countries = new ObjectObjectMap<>();
        try {
            JsonNode countriesJson = openJson(this.countriesJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = this.basePath + entry.getValue().asText();
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
            JsonNode countryJson = openJson(countryFileName);
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
            JsonNode provincesJson = openJson(this.provincesJsonFile);
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
            JsonNode rootNode = openJson(provinceFileName);
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
            JsonNode rootNode = openJson(this.regionJsonFiles);
            rootNode.fields().forEachRemaining(regionData -> {
                Region region = new Region(regionData.getKey());
                regionData.getValue().forEach(provinceId -> {
                    LandProvince province = (LandProvince) provinces.get(provinceId.shortValue());
                    if(province != null) {
                        province.setRegion(region);
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
            JsonNode countriesJson = openJson(this.countriesHistoryJsonFiles);
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
            JsonNode continentJson = openJson(this.continentJsonFile);
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
            JsonNode countryJson = openJson(countryFileName);
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
            int localisationIndex = Arrays.asList(headers).indexOf(this.localisation);
            if (localisationIndex == -1) {
                throw new IllegalArgumentException("Localisation " + this.localisation + " not found in CSV headers.");
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

    private Map<Integer, Vector2> readPositionsJson() {
        Map<Integer, Vector2> unitPositions = new ObjectObjectMap<>();
        try {
            JsonNode positionsJson = openJson(this.positionsJsonFile);
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
            JsonNode rootNode = openJson(this.bookmarkJsonFile);
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

    private Map<String, String> readLocalisationCsv(String filename) {
        Map<String, String> localisation = new ObjectObjectMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Gdx.files.internal(filename).read(), StandardCharsets.UTF_8))) {
            String[] headers = br.readLine().split(";");
            int localisationIndex = Arrays.asList(headers).indexOf(this.localisation);
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
            JsonNode adjenciesJson = openJson(this.adjenciesJsonFile);
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
