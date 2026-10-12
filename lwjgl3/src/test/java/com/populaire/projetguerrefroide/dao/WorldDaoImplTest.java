package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.graphics.Pixmap;
import com.populaire.projetguerrefroide.TestGdxFiles;
import com.populaire.projetguerrefroide.dao.csv.ProvinceDefinitionCsv;
import com.populaire.projetguerrefroide.dao.impl.WorldDaoImpl;
import com.populaire.projetguerrefroide.dao.json.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WorldDaoImplTest {
    private WorldDao worldDao;

    @BeforeAll
    public void setUp() {
        TestGdxFiles.install();
        this.worldDao = new WorldDaoImpl();
    }

    @Test
    public void readsCommonData() {
        assertEquals(12, this.worldDao.readPopulationTypes().size());
        assertEquals(21, this.worldDao.readUnits().size());
        assertEquals(268, this.worldDao.readCountries().size());
        assertFalse(this.worldDao.readIdeologies().isEmpty());
        assertFalse(this.worldDao.readLaws().groups().isEmpty());
        assertFalse(this.worldDao.readGovernments().isEmpty());
        assertFalse(this.worldDao.readNationalIdeas().cultures().isEmpty());
        assertFalse(this.worldDao.readMinisterTypes().isEmpty());
        assertFalse(this.worldDao.readTraits().isEmpty());
        assertFalse(this.worldDao.readResourceProductions().isEmpty());
        assertFalse(this.worldDao.readPopulationTemplates().isEmpty());

        GoodsJson goods = this.worldDao.readGoods();
        assertEquals(41, goods.resourceGoods().size() + goods.advancedGoods().size() + goods.militaryGoods().size());

        ProductionTypesJson productionTypes = this.worldDao.readProductionTypes();
        assertFalse(productionTypes.typesWorkers().isEmpty());
        assertFalse(productionTypes.typesBuildings().isEmpty());
        assertFalse(productionTypes.typesRgo().isEmpty());

        BuildingsJson buildings = this.worldDao.readBuildings();
        assertEquals(32, buildings.economyBuilding().size());
        assertEquals(17, buildings.specialBuilding().size());
        assertEquals(9, buildings.developmentBuilding().size());
    }

    @Test
    public void keepsFileOrderOfIndexedFamilies() {
        List<String> units = List.copyOf(this.worldDao.readUnits().keySet());
        assertEquals("aircraft_carrier", units.get(0));
        assertEquals("tank", units.get(units.size() - 1));

        Map<String, ProvinceJson> provinces = this.worldDao.readProvinces();
        assertEquals(14796, provinces.size());
        assertEquals("10046", provinces.keySet().iterator().next());
    }

    @Test
    public void readsHistoryData() {
        Map<String, CountryHistoryJson> countriesHistory = this.worldDao.readCountriesHistory();
        assertEquals(267, countriesHistory.size());
        assertEquals(4629, countriesHistory.get("RIF").capital());
        assertEquals(255, this.worldDao.readLeaders().size());
        assertFalse(this.worldDao.readRelations().relation().isEmpty());
        assertFalse(this.worldDao.readAlliances().alliances().isEmpty());

        for (ProvinceJson province : this.worldDao.readProvinces().values()) {
            assertFalse(province.addCore().isEmpty());
        }
    }

    @Test
    public void readsMapData() {
        assertFalse(this.worldDao.readTerrains().isEmpty());
        assertFalse(this.worldDao.readRegions().isEmpty());
        assertFalse(this.worldDao.readContinents().isEmpty());
        assertFalse(this.worldDao.readAdjacencies().isEmpty());
        assertFalse(this.worldDao.readPositions().isEmpty());

        List<ProvinceDefinitionCsv> definitions = this.worldDao.readProvinceDefinitions();
        assertEquals(18184, definitions.size());
        assertEquals(1, definitions.get(0).id());
        assertEquals(118, definitions.get(0).red());

        Pixmap bitmap = this.worldDao.readProvincesBitmap();
        try {
            assertTrue(bitmap.getWidth() > 0 && bitmap.getHeight() > 0);
        } finally {
            bitmap.dispose();
        }
    }
}
