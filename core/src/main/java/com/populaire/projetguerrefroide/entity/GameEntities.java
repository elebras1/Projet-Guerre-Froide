package com.populaire.projetguerrefroide.entity;

import com.github.tommyettinger.ds.IntObjectMap;
import com.populaire.projetguerrefroide.economy.building.Building;
import com.populaire.projetguerrefroide.economy.good.Good;
import com.populaire.projetguerrefroide.economy.population.PopulationTemplate;
import com.populaire.projetguerrefroide.national.NationalIdeas;

import java.util.Map;

public class GameEntities {
    private final NationalIdeas nationalIdeas;
    private final Map<String, Government> governments;
    private final Map<String, Ideology> ideologies;
    private final Map<String, Good> goods;
    private final Map<String, Building> buildings;
    private final Map<String, MinisterType> ministerTypes;
    private final IntObjectMap<PopulationTemplate> populationTemplates;
    private final Map<String, Terrain> terrains;

    public GameEntities(NationalIdeas nationalIdeas, Map<String, Government> governments, Map<String, Ideology> ideologies, Map<String, Good> goods, Map<String, Building> buildings, Map<String, MinisterType> ministerTypes, IntObjectMap<PopulationTemplate> populationTemplates, Map<String, Terrain> terrains) {
        this.nationalIdeas = nationalIdeas;
        this.governments = governments;
        this.ideologies = ideologies;
        this.goods = goods;
        this.buildings = buildings;
        this.ministerTypes = ministerTypes;
        this.populationTemplates = populationTemplates;
        this.terrains = terrains;
    }

    public NationalIdeas getNationalIdeas() {
        return nationalIdeas;
    }

    public Map<String, Government> getGovernments() {
        return governments;
    }

    public Map<String, Ideology> getIdeologies() {
        return ideologies;
    }

    public Map<String, Good> getGoods() {
        return goods;
    }

    public Map<String, Building> getBuildings() {
        return buildings;
    }

    public Map<String, MinisterType> getMinisterTypes() {
        return ministerTypes;
    }

    public IntObjectMap<PopulationTemplate> getPopulationTemplates() {
        return populationTemplates;
    }

    public Map<String, Terrain> getTerrains() {
        return terrains;
    }
}
