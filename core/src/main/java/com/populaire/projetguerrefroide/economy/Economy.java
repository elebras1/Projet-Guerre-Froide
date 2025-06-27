package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.economy.population.PopulationTypeStore;
import com.populaire.projetguerrefroide.map.LandProvince;

import java.util.Map;

public class Economy {
    private final short maxProvinceId;
    private final ObjectIntMap<String> goodIds;
    private final ObjectIntMap<String>  buildingIds;
    private final ObjectIntMap<String>  populationTypeIds;
    private final ObjectIntMap<String>  productionTypeIds;

    public Economy(short maxProvinceId, ObjectIntMap<String>  goodIds, ObjectIntMap<String>  buildingIds, ObjectIntMap<String>  populationTypeIds, ObjectIntMap<String>  productionTypeIds) {
        this.maxProvinceId = maxProvinceId;
        this.goodIds = goodIds;
        this.buildingIds = buildingIds;
        this.populationTypeIds = populationTypeIds;
        this.productionTypeIds = productionTypeIds;
    }

    public short getMaxProvinceId() {
        return this.maxProvinceId;
    }

    public ObjectIntMap<String> getGoodIds() {
        return this.goodIds;
    }

    public ObjectIntMap<String> getBuildingIds() {
        return this.buildingIds;
    }

    public ObjectIntMap<String> getPopulationTypeIds() {
        return this.populationTypeIds;
    }

    public ObjectIntMap<String> getProductionTypeIds() {
        return this.productionTypeIds;
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
