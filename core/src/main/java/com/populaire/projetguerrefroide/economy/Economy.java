package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntObjectMap;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;
import com.populaire.projetguerrefroide.map.LandProvince;

import java.util.Map;

public class Economy {
    private final short maxProvinceId;
    private final Map<String, GoodStore> goods;
    private final Map<String, BuildingStore> buildings;
    private final Map<String, PopulationTypeStore> populationTypes;
    private final Map<String, ProductionTypeStore> productionTypes;
    private final int[] resourceGoodsSize;
    private final float[] resourceGoodsProduction;

    public Economy(short maxProvinceId, Map<String, GoodStore> goods, Map<String, BuildingStore> buildings, Map<String, PopulationTypeStore> populationTypes, Map<String, ProductionTypeStore> productionTypes) {
        this.maxProvinceId = maxProvinceId;
        this.goods = goods;
        this.buildings = buildings;
        this.populationTypes = populationTypes;
        this.productionTypes = productionTypes;
        this.resourceGoodsSize = new int[this.maxProvinceId];
        this.resourceGoodsProduction = new float[this.maxProvinceId];
    }

    public Map<String, GoodStore> getGoods() {
        return this.goods;
    }

    public Map<String, BuildingStore> getBuildings() {
        return this.buildings;
    }

    public Map<String, PopulationTypeStore> getPopulationTypes() {
        return this.populationTypes;
    }

    public Map<String, ProductionTypeStore> getProductionTypes() {
        return this.productionTypes;
    }

    public void setResourceGoodsSize(IntObjectMap<LandProvince> provinces) {
        for(LandProvince province : provinces.values()) {
            if(province.getResourceGood() == null) {
                continue;
            }
            int workforce = province.getResourceGood().getProductionType().getWorkforce();
            PopulationTypeStore workerPopulation = province.getResourceGood().getProductionType().getEmployees().getFirst().getPopulationType();
            int workerInProvince = province.getPopulation().getPopulations().get(workerPopulation);
            int size = (workerInProvince + workforce - 1) / workforce;
            size = (int)(size * 1.5f);
            this.resourceGoodsSize[province.getId()] = size;
        }
    }
}
