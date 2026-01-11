package com.populaire.projetguerrefroide.map;

import com.populaire.projetguerrefroide.economy.building.BuildingStore;

public interface WorldContext {
    long getPlayerCountryId();
    ProvinceStore getProvinceStore();
    RegionStore getRegionStore();
    BuildingStore getBuildingStore();

}
