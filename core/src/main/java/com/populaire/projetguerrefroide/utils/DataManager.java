package com.populaire.projetguerrefroide.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.populaire.projetguerrefroide.entities.Minister;
import com.populaire.projetguerrefroide.entities.Population;
import com.populaire.projetguerrefroide.map.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataManager {
    private static final String basePath = "common/";
    private static final String mapPath = "map/";
    private static final String historyPath = "history/";
    private static final String localisationPath = "localisation/";
    private static final String countriesJsonFiles = basePath + "countries.json";
    private static final String countriesHistoryJsonFiles = historyPath + "countries.json";
    private static final String regionJsonFiles = mapPath + "region.json";
    private static final String provincesJsonFile = historyPath + "provinces.json";
    private static final String definitionCsvFile = mapPath + "definition.csv";
    private static final String continentJsonFile = mapPath + "continent.json";
    private static final String positionsJsonFile = mapPath + "positions.json";
    private static final String provinceNamesCsvFile = localisationPath + "province_names.csv";
    private static final String newgameCsvFile = localisationPath + "newgame.csv";
    private static final String bookmarkJsonFile = basePath + "bookmark.json";
    private static final String bookmarkCsvFile = localisationPath + "bookmark.csv";
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Country> countries = new HashMap<>(300);
    private final String defaultDate = "1946.1.1";
    private String localisation = "ENGLISH";

    public DataManager() { }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public List<Country> loadCountries() {
        return new ArrayList<>(this.readCountriesJson().values());
    }

    public Map<Color, Province> loadProvinces() {
        Map<Color, Province> provincesByColor = new HashMap<>(20000);
        Map<Short, Province> provinces = this.readProvincesJson();
        this.readRegionJson(provinces);
        this.readDefinitionCsv(provinces, provincesByColor);
        this.readProvinceBitmap(provincesByColor);
        this.readProvinceNamesCsv(provinces);
        this.readCountriesHistoryJson(provinces);
        this.readContinentJsonFile(provinces);

        return provincesByColor;
    }

    private JsonNode openJson(String fileName) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return mapper.readTree(fileHandle.readString());
    }

    private Map<String, Country> readCountriesJson() {
        try {
            JsonNode countriesJson = openJson(countriesJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = basePath + entry.getValue().asText();
                this.countries.put(entry.getKey(), readCountryJson(countryFileName, entry.getKey(), parseFileName(countryFileName)));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.countries;
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

    private Color parseColor(JsonNode colorNode) {
        return new Color(
                colorNode.get(0).floatValue() / 255,
                colorNode.get(1).floatValue() / 255,
                colorNode.get(2).floatValue() / 255,
                1
        );
    }

    private Map<Short, Province> readProvincesJson() {
        Map<Short, Province> provinces = new HashMap<>(20000);
        try {
            JsonNode provincesJson = openJson(provincesJsonFile);
            provincesJson.fields().forEachRemaining(entry -> {
                String provinceFileName = historyPath + entry.getValue().asText();
                short provinceId = Short.parseShort(entry.getKey());
                Province province = readProvinceJson(provinceFileName, provinceId);
                provinces.put(provinceId, province);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return provinces;
    }

    private LandProvince readProvinceJson(String provinceFileName, short provinceId) {
        try {
            JsonNode rootNode = openJson(provinceFileName);
            String owner = rootNode.path("owner").asText();
            String controller = rootNode.path("controller").asText();
            Country countryOwner = this.countries.get(owner);
            Country countryController = this.countries.get(controller);
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

    private void readRegionJson(Map<Short, Province> provinces) {
        try {
            JsonNode rootNode = openJson(regionJsonFiles);
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

    private void readDefinitionCsv(Map<Short, Province> provinces, Map<Color, Province> provincesByColor) {
        try (BufferedReader br = new BufferedReader(new StringReader(Gdx.files.internal(definitionCsvFile).readString()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    short provinceId = Short.parseShort(values[0]);
                    Province province = provinces.get(provinceId);
                    Color color = new Color(Float.parseFloat(values[1]) / 255, Float.parseFloat(values[2]) / 255, Float.parseFloat(values[3]) / 255, 1);
                    province.setColor(color);
                    provincesByColor.put(color, province);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCountriesHistoryJson(Map<Short, Province> provinces) {
        try {
            JsonNode countriesJson = openJson(countriesHistoryJsonFiles);
            countriesJson.fields().forEachRemaining(entry -> {
                String countryFileName = historyPath + entry.getValue().textValue();
                this.readCountryHistoryJson(countryFileName, entry.getKey(), provinces);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readContinentJsonFile(Map<Short, Province> provinces) {
        try {
            JsonNode continentJson = openJson(continentJsonFile);
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

    private void readCountryHistoryJson(String countryFileName, String idCountry, Map<Short, Province> provinces) {
        try {
            if(countryFileName.equals("history/countries/REB - Rebels.json")) {
                return;
            }
            JsonNode countryJson = openJson(countryFileName);
            short idCapital = countryJson.get("capital").shortValue();
            Country country = this.countries.get(idCountry);
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

    private void readProvinceBitmap(Map<Color, Province> provincesByColor) {
        Pixmap bitmap = new Pixmap(Gdx.files.internal(mapPath + "provinces.bmp"));
        for (short y = 0; y < bitmap.getHeight(); y++) {
            for (short x = 0; x < bitmap.getWidth(); x++) {
                Color color = new Color(bitmap.getPixel(x, y));
                Province province = provincesByColor.get(color);
                if(province instanceof LandProvince) {
                    province.addPixel(x, y);
                }
            }
        }
        bitmap.dispose();
    }

    private void readProvinceNamesCsv(Map<Short, Province> provinces) {
        try (BufferedReader br = new BufferedReader(new StringReader(Gdx.files.internal(provinceNamesCsvFile).readString()))) {
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
        Map<Integer, Vector2> unitPositions = new HashMap<>();
        try {
            JsonNode positionsJson = openJson(positionsJsonFile);
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
            JsonNode rootNode = openJson(bookmarkJsonFile);
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

            List<String> countriesId = new ArrayList<>();
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

    public Map<String, String> readBookmarkLocalisationCsv() {
        return readLocalisationCsv(bookmarkCsvFile);
    }

    public Map<String, String> readNewgameLocalisationCsv() {
        return readLocalisationCsv(newgameCsvFile);
    }

    private Map<String, String> readLocalisationCsv(String filename) {
        Map<String, String> localisation = new HashMap<>();
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


}
