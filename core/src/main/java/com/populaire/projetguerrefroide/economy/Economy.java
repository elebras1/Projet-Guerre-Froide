package com.populaire.projetguerrefroide.economy;

import com.github.tommyettinger.ds.IntObjectMap;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.building.ProductionType;
import com.populaire.projetguerrefroide.economy.good.Good;
import com.populaire.projetguerrefroide.economy.population.PopulationType;
import com.populaire.projetguerrefroide.map.LandProvince;

import java.util.Map;

public class Economy {
    private final int numberProvinces = 20000; // todo change that whith the sum of land + water provinces, or max id of province
    private final Map<String, Good> goods;
    private final Map<String, Building> buildings;
    private final Map<String, PopulationType> populationTypes;
    private final Map<String, ProductionType> productionTypes;
    private final int[] resourceGoodsSize;
    private final float[] resourceGoodsProduction;

    public Economy(Map<String, Good> goods, Map<String, Building> buildings, Map<String, PopulationType> populationTypes, Map<String, ProductionType> productionTypes) {
        this.goods = goods;
        this.buildings = buildings;
        this.populationTypes = populationTypes;
        this.productionTypes = productionTypes;
        this.resourceGoodsSize = new int[this.numberProvinces];
        this.resourceGoodsProduction = new float[this.numberProvinces];
    }

    public Map<String, Good> getGoods() {
        return this.goods;
    }

    public Map<String, Building> getBuildings() {
        return this.buildings;
    }

    public Map<String, PopulationType> getPopulationTypes() {
        return this.populationTypes;
    }

    public Map<String, ProductionType> getProductionTypes() {
        return this.productionTypes;
    }

    public void setResourceGoodsSize(IntObjectMap<LandProvince> provinces) {
        for(LandProvince province : provinces.values()) {
            if(province.getResourceGood() == null) {
                continue;
            }
            int workforce = province.getResourceGood().getProductionType().getWorkforce();
            PopulationType workerPopulation = province.getResourceGood().getProductionType().getEmployees().getFirst().getPopulationType();
            int workerInProvince = province.getPopulation().getPopulations().get(workerPopulation);
            int size = (workerInProvince + workforce - 1) / workforce;
            size = (int)(size * 1.5f);
            this.resourceGoodsSize[province.getId()] = size;
        }
    }
}
