package com.populaire.projetguerrefroide.loader;

import com.github.tommyettinger.ds.IntLongMap;
import com.populaire.projetguerrefroide.dao.WorldDao;
import com.populaire.projetguerrefroide.pojo.Borders;
import com.populaire.projetguerrefroide.pojo.ProductionType;
import com.populaire.projetguerrefroide.pojo.WorldData;
import com.populaire.projetguerrefroide.service.GameContext;

import java.util.Map;

public final class WorldLoader {
    private final WorldDao worldDao;

    public WorldLoader(WorldDao worldDao) {
        this.worldDao = worldDao;
    }

    public WorldData createWorld(GameContext gameContext) {
        WorldLoadingContext context = new WorldLoadingContext(gameContext.getEcsWorld(), gameContext.getEcsConstants());
        PoliticsLoader politics = new PoliticsLoader(context);
        EconomyLoader economy = new EconomyLoader(context);
        MilitaryLoader military = new MilitaryLoader(context);
        CountryLoader country = new CountryLoader(context);
        MapLoader map = new MapLoader(context);

        economy.createGlobalMarket();
        politics.loadIdeologies(this.worldDao.readIdeologies());
        politics.loadLaws(this.worldDao.readLaws());
        politics.loadGovernments(this.worldDao.readGovernments());
        politics.loadNationalIdeas(this.worldDao.readNationalIdeas());
        politics.loadMinisterTypes(this.worldDao.readMinisterTypes());
        economy.loadGoods(this.worldDao.readGoods());
        economy.loadPopulationTypes(this.worldDao.readPopulationTypes());
        Map<String, ProductionType> productionTypes = economy.loadProductionTypes(this.worldDao.readProductionTypes());
        economy.loadBuildings(this.worldDao.readBuildings(), productionTypes);
        economy.loadResourceProductions(this.worldDao.readResourceProductions(), productionTypes);
        military.loadTraits(this.worldDao.readTraits());
        military.loadUnits(this.worldDao.readUnits());
        country.loadCountries(this.worldDao.readCountries());
        country.loadRelations(this.worldDao.readRelations());
        country.loadAlliances(this.worldDao.readAlliances());
        military.loadLeaders(this.worldDao.readLeaders());
        map.loadTerrains(this.worldDao.readTerrains());
        ProvinceBuildings provinceBuildings = map.loadProvinces(this.worldDao.readProvinces(), this.worldDao.readPopulationTemplates());
        map.loadRegions(this.worldDao.readRegions(), provinceBuildings);
        IntLongMap provinces = map.loadProvinceDefinitions(this.worldDao.readProvinceDefinitions());
        Borders borders = map.loadBorders(this.worldDao.readProvincesBitmap(), provinces);
        country.loadCountriesHistory(this.worldDao.readCountriesHistory());
        map.loadContinents(this.worldDao.readContinents());
        map.loadAdjacencies(this.worldDao.readAdjacencies());
        map.loadPositions(this.worldDao.readPositions());

        return new WorldData(provinces, borders);
    }
}
