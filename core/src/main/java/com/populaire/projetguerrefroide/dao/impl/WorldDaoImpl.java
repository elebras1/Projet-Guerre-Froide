package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.dslplatform.json.runtime.TypeDefinition;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.dao.csv.ProvinceDefinitionCsv;
import com.populaire.projetguerrefroide.dao.json.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final String provincesBitmapFile = this.mapPath + "provinces.bmp";
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
    private final String unitsJsonFile = this.commonPath + "units.json";
    private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
    private final Type pathsType = new TypeDefinition<Map<String, String>>() {}.type;
    private final Type pathListType = new TypeDefinition<List<String>>() {}.type;
    private final Type modifiersType = new TypeDefinition<Map<String, Map<String, Double>>>() {}.type;
    private final Type governmentsType = new TypeDefinition<Map<String, Map<String, Object>>>() {}.type;
    private final Type ideologiesType = new TypeDefinition<Map<String, IdeologyJson>>() {}.type;
    private final Type terrainsType = new TypeDefinition<Map<String, TerrainJson>>() {}.type;
    private final Type resourceProductionsType = new TypeDefinition<Map<String, ResourceProductionJson>>() {}.type;
    private final Type populationTemplatesType = new TypeDefinition<Map<String, PopulationTemplateJson>>() {}.type;
    private final Type provinceListsType = new TypeDefinition<Map<String, int[]>>() {}.type;
    private final Type positionsType = new TypeDefinition<Map<String, Map<String, PositionJson>>>() {}.type;

    @Override
    public Map<String, IdeologyJson> readIdeologies() {
        return this.readJson(this.ideologiesJsonFile, this.ideologiesType);
    }

    @Override
    public LawsJson readLaws() {
        return this.readJson(this.lawsJsonFile, LawsJson.class);
    }

    @Override
    public Map<String, Map<String, Object>> readGovernments() {
        return this.readJson(this.governmentJsonFile, this.governmentsType);
    }

    @Override
    public NationalIdeasJson readNationalIdeas() {
        return this.readJson(this.nationalIdeasJsonFile, NationalIdeasJson.class);
    }

    @Override
    public Map<String, Map<String, Double>> readMinisterTypes() {
        return this.readJson(this.ministerTypesJsonFile, this.modifiersType);
    }

    @Override
    public GoodsJson readGoods() {
        return this.readJson(this.goodsJsonFile, GoodsJson.class);
    }

    @Override
    public Map<String, PopulationTypeJson> readPopulationTypes() {
        return this.readIndexedJson(this.populationTypesJsonFile, this.commonPath, PopulationTypeJson.class);
    }

    @Override
    public ProductionTypesJson readProductionTypes() {
        return this.readJson(this.productionTypesJsonFile, ProductionTypesJson.class);
    }

    @Override
    public BuildingsJson readBuildings() {
        return this.readJson(this.buildingsJsonFile, BuildingsJson.class);
    }

    @Override
    public Map<String, ResourceProductionJson> readResourceProductions() {
        return this.readJson(this.resourceProductionsJsonFile, this.resourceProductionsType);
    }

    @Override
    public Map<String, Map<String, Double>> readTraits() {
        return this.readJson(this.traitsJsonFile, this.modifiersType);
    }

    @Override
    public Map<String, UnitJson> readUnits() {
        return this.readIndexedJson(this.unitsJsonFile, this.commonPath, UnitJson.class);
    }

    @Override
    public Map<String, CountryJson> readCountries() {
        return this.readIndexedJson(this.countriesJsonFiles, this.commonPath, CountryJson.class);
    }

    @Override
    public RelationsJson readRelations() {
        return this.readJson(this.relationJsonFile, RelationsJson.class);
    }

    @Override
    public AlliancesJson readAlliances() {
        return this.readJson(this.alliancesJsonFile, AlliancesJson.class);
    }

    @Override
    public List<LeadersJson> readLeaders() {
        List<String> leadersPaths = this.readJson(this.leadersJsonFiles, this.pathListType);
        List<LeadersJson> leaders = new ArrayList<>(leadersPaths.size());
        for (String leadersPath : leadersPaths) {
            leaders.add(this.readJson(this.historyPath + leadersPath, LeadersJson.class));
        }
        return leaders;
    }

    @Override
    public Map<String, TerrainJson> readTerrains() {
        return this.readJson(this.terrainJsonFile, this.terrainsType);
    }

    @Override
    public Map<String, PopulationTemplateJson> readPopulationTemplates() {
        return this.readJson(this.populationTemplatesJsonFile, this.populationTemplatesType);
    }

    @Override
    public Map<String, ProvinceJson> readProvinces() {
        return this.readIndexedJson(this.provincesJsonFile, this.historyPath, ProvinceJson.class);
    }

    @Override
    public Map<String, int[]> readRegions() {
        return this.readJson(this.regionJsonFiles, this.provinceListsType);
    }

    @Override
    public List<ProvinceDefinitionCsv> readProvinceDefinitions() {
        List<ProvinceDefinitionCsv> definitions = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(Gdx.files.internal(this.definitionCsvFile).readString()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(";");
                if (!values[0].isEmpty()) {
                    definitions.add(new ProvinceDefinitionCsv(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3])));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return definitions;
    }

    @Override
    public Pixmap readProvincesBitmap() {
        return new Pixmap(Gdx.files.internal(this.provincesBitmapFile));
    }

    @Override
    public Map<String, CountryHistoryJson> readCountriesHistory() {
        return this.readIndexedJson(this.countriesHistoryJsonFiles, this.historyPath, CountryHistoryJson.class);
    }

    @Override
    public Map<String, int[]> readContinents() {
        return this.readJson(this.continentJsonFile, this.provinceListsType);
    }

    @Override
    public Map<String, int[]> readAdjacencies() {
        return this.readJson(this.adjacenciesJsonFile, this.provinceListsType);
    }

    @Override
    public Map<String, Map<String, PositionJson>> readPositions() {
        return this.readJson(this.positionsJsonFile, this.positionsType);
    }

    private <T> T readJson(String filePath, Class<T> type) {
        try {
            byte[] bytes = Gdx.files.internal(filePath).readBytes();
            return this.dslJson.deserialize(type, bytes, bytes.length);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T readJson(String filePath, Type type) {
        try {
            byte[] bytes = Gdx.files.internal(filePath).readBytes();
            return (T) this.dslJson.deserialize(type, bytes, bytes.length);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private <T> Map<String, T> readIndexedJson(String indexFilePath, String basePath, Class<T> type) {
        Map<String, String> paths = this.readJson(indexFilePath, this.pathsType);
        Map<String, T> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> path : paths.entrySet()) {
            values.put(path.getKey(), this.readJson(basePath + path.getValue(), type));
        }
        return values;
    }
}
