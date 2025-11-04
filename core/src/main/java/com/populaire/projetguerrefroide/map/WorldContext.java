package com.populaire.projetguerrefroide.map;

import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;
import com.populaire.projetguerrefroide.economy.good.GoodStore;
import com.populaire.projetguerrefroide.national.NationalIdeas;
import com.populaire.projetguerrefroide.politics.Politics;

public interface WorldContext {
    Country getPlayerCountry();
    ProvinceStore getProvinceStore();
    RegionStore getRegionStore();
    BuildingStore getBuildingStore();
    GoodStore getGoodStore();
    ProductionTypeStore getProductionTypeStore();
    EmployeeStore getEmployeeStore();
    Politics getPolitics();
    NationalIdeas getNationalIdeas();

}
