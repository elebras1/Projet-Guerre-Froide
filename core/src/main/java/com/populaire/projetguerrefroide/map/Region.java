package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntSet;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.economy.building.Building;

import java.util.List;

public class Region {
    private final String id;
    private final List<LandProvince> provinces;
    private final ObjectIntMap<Building> buildings;

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

    public ObjectIntMap<Building> getBuildings() {
        return this.buildings;
    }

    public void addBuilding(Building building, int size) {
        int currentSize = this.buildings.get(building);
        this.buildings.put(building, currentSize + size);
    }

    public void addAllBuildings(ObjectIntMap<Building> buildings) {
        for(ObjectIntMap.Entry<Building> entry : buildings.entrySet()) {
            this.addBuilding(entry.key, entry.value);
        }
    }


    public IntList getPixelsBorder() {
        IntSet pixels = new IntSet();
        IntList pixelsBorder = new IntList();
        for(LandProvince province : this.provinces) {
            pixels.addAll(province.getPixels());
        }

        for(IntSet.IntSetIterator iterator = pixels.iterator(); iterator.hasNext();) {
            int pixelInt = iterator.nextInt();
            if(this.isPixelBorder((short)(pixelInt >> 16), (short) (pixelInt & 0xFFFF), pixels)) {
                pixelsBorder.add(pixelInt);
            }
        }

        return pixelsBorder;
    }

    public boolean isPixelBorder(short x, short y, IntSet pixels) {
        return !pixels.contains((x + 1 << 16) | (y & 0xFFFF))
            || !pixels.contains((x - 1 << 16) | (y & 0xFFFF))
            || !pixels.contains((x << 16) | (y + 1 & 0xFFFF))
            || !pixels.contains((x << 16) | (y - 1 & 0xFFFF));
    }

    @Override
    public String toString() {
        return "Region{" +
            "id='" + this.id + '\'' +
            ", provinces=" + this.provinces +
            '}';
    }
}
