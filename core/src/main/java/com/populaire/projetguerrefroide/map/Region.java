package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.building.BuildingStore;

import java.util.List;

public class Region {
    private final String id;
    private final List<LandProvince> provinces;
    private final ObjectIntMap<BuildingStore> buildings;

    public Region(String id) {
        this.id = id;
        this.provinces = new ObjectList<>();
        this.buildings = new ObjectIntMap<>();
    }

    public String getId() {
        return this.id;
    }

    public void addProvince(LandProvince province) {
        this.provinces.add(province);
    }

    public List<LandProvince> getProvinces() {
        return this.provinces;
    }

    public ObjectIntMap<BuildingStore> getBuildings() {
        return this.buildings;
    }

    public void addBuilding(BuildingStore building, int size) {
        int currentSize = this.buildings.get(building);
        this.buildings.put(building, currentSize + size);
    }

    public void addAllBuildings(ObjectIntMap<BuildingStore> buildings) {
        for(ObjectIntMap.Entry<BuildingStore> entry : buildings.entrySet()) {
            this.addBuilding(entry.key, entry.value);
        }
    }

    @Override
    public String toString() {
        return "Region{" +
            "id='" + this.id + '\'' +
            ", provinces=" + this.provinces +
            '}';
    }
}
