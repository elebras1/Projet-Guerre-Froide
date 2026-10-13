package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.graphics.Pixmap;
import com.populaire.projetguerrefroide.dao.csv.ProvinceDefinitionCsv;
import com.populaire.projetguerrefroide.dao.json.*;

import java.util.List;
import java.util.Map;

public interface WorldDao {
    Map<String, IdeologyJson> readIdeologies();

    LawsJson readLaws();

    Map<String, GovernmentJson> readGovernments();

    NationalIdeasJson readNationalIdeas();

    Map<String, Map<String, Double>> readMinisterTypes();

    GoodsJson readGoods();

    Map<String, PopulationTypeJson> readPopulationTypes();

    ProductionTypesJson readProductionTypes();

    BuildingsJson readBuildings();

    Map<String, ResourceProductionJson> readResourceProductions();

    Map<String, Map<String, Double>> readTraits();

    Map<String, UnitJson> readUnits();

    Map<String, CountryJson> readCountries();

    RelationsJson readRelations();

    AlliancesJson readAlliances();

    List<LeadersJson> readLeaders();

    Map<String, TerrainJson> readTerrains();

    Map<String, PopulationTemplateJson> readPopulationTemplates();

    Map<String, ProvinceJson> readProvinces();

    Map<String, int[]> readRegions();

    List<ProvinceDefinitionCsv> readProvinceDefinitions();

    Pixmap readProvincesBitmap();

    Map<String, CountryHistoryJson> readCountriesHistory();

    Map<String, int[]> readContinents();

    Map<String, int[]> readAdjacencies();

    Map<String, Map<String, PositionJson>> readPositions();
}
