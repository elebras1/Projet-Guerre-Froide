package com.populaire.projetguerrefroide.map;

import com.populaire.projetguerrefroide.economy.building.BuildingStore;
import com.populaire.projetguerrefroide.economy.building.EmployeeStore;
import com.populaire.projetguerrefroide.economy.building.ProductionTypeStore;

import java.util.List;

public interface WorldContext {
    long getPlayerCountryId();
    ProvinceStore getProvinceStore();
    RegionStore getRegionStore();
    BuildingStore getBuildingStore();
    ProductionTypeStore getProductionTypeStore();
    EmployeeStore getEmployeeStore();

}
