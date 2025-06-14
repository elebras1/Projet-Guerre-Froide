package com.populaire.projetguerrefroide.economy;

import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.building.ProductionType;
import com.populaire.projetguerrefroide.economy.good.Good;
import com.populaire.projetguerrefroide.economy.population.PopulationType;

import java.util.Map;

public class Economy {
    private final Map<String, Good> goods;
    private final Map<String, Building> buildings;
    private final Map<String, PopulationType> populationTypes;
    private final Map<String, ProductionType> productionTypes;

    public Economy(Map<String, Good> goods, Map<String, Building> buildings, Map<String, PopulationType> populationTypes, Map<String, ProductionType> productionTypes) {
        this.goods = goods;
        this.buildings = buildings;
        this.populationTypes = populationTypes;
        this.productionTypes = productionTypes;
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
}
