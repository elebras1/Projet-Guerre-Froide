package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.ObjectList;

import java.util.List;

public class Region {
    private final String id;
    private final List<LandProvince> provinces;

    public Region(String id) {
        this.id = id;
        this.provinces = new ObjectList<>();
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

    @Override
    public String toString() {
        return "Region{" +
            "id='" + this.id + '\'' +
            ", provinces=" + this.provinces +
            '}';
    }
}
