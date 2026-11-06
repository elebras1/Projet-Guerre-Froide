package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class MapBuildings extends Response {
    private int[] provinces = new int[256];
    private int[] buildingTypes = new int[256];
    private int count;

    public void clear() {
        this.count = 0;
    }

    public void add(int provinceIndex, int buildingTypeIndex) {
        this.provinces = Columns.fit(this.provinces, this.count + 1);
        this.buildingTypes = Columns.fit(this.buildingTypes, this.count + 1);
        this.provinces[this.count] = provinceIndex;
        this.buildingTypes[this.count] = buildingTypeIndex;
        this.count++;
    }

    public int count() {
        return this.count;
    }

    public int province(int position) {
        return this.provinces[position];
    }

    public int buildingType(int position) {
        return this.buildingTypes[position];
    }

    public boolean sameContent(MapBuildings other) {
        if (other.count != this.count) {
            return false;
        }
        for (int i = 0; i < this.count; i++) {
            if (other.provinces[i] != this.provinces[i] || other.buildingTypes[i] != this.buildingTypes[i]) {
                return false;
            }
        }
        return true;
    }

    public void copyFrom(MapBuildings other) {
        this.clear();
        for (int i = 0; i < other.count; i++) {
            this.add(other.provinces[i], other.buildingTypes[i]);
        }
    }
}
