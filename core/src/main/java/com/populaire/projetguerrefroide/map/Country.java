package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.util.AllianceType;

import java.util.*;
import java.util.List;

public class Country {
    private final String id;
    private LongList provinceIds;
    private ObjectIntMap<Country> relations;
    private Map<Country, AllianceType> alliances;
    private long capitalId;
    private long governmentId;
    private long ideologyId;
    private long headOfGovernmentId;
    private long headOfStateId;
    private LongList ministerIds;
    private long identityId;
    private long attitudeId;
    private String name;
    private LongList lawIds;
    private IntList leadersIds;

    public Country(String id) {
        this.id = id;
        this.provinceIds = new LongList();
        this.capitalId = -1;
        this.governmentId = -1;
        this.ideologyId = -1;
        this.headOfGovernmentId = -1;
        this.headOfStateId = -1;
        this.identityId = -1;
        this.attitudeId = -1;
    }

    public String getId() {
        return this.id;
    }

    public void addProvinceId(long provinceId) {
        this.provinceIds.add(provinceId);
    }

    public LongList getProvinceIds() {
        return this.provinceIds;
    }

    public void addRelation(Country country, int value) {
        if(this.relations == null) {
            this.relations = new ObjectIntMap<>();
        }

        this.relations.put(country, value);
    }

    public void removeRelation(Country country) {
        this.relations.remove(country);

        if(this.relations.isEmpty()) {
            this.relations = null;
        }
    }

    public ObjectIntMap<Country> getRelations() {
        return this.relations;
    }

    public void addAlliance(Country country, AllianceType allianceType) {
        if(this.alliances == null) {
            this.alliances = new ObjectObjectMap<>();
        }

        this.alliances.put(country, allianceType);
    }

    public void removeAlliance(Country country) {
        this.alliances.remove(country);

        if(this.alliances.isEmpty()) {
            this.alliances = null;
        }
    }

    public Map<Country, AllianceType> getAlliances() {
        return this.alliances;
    }

    public void setCapitalId(long capitalId) {
        this.capitalId = capitalId;
    }

    public long getCapitalId() {
        return this.capitalId;
    }

    public void setGovernmentId(long governmentId) {
        this.governmentId = governmentId;
    }

    public long getGovernmentId() {
        return this.governmentId;
    }

    public void setIdeologyId(long ideologyId) {
        this.ideologyId = ideologyId;
    }

    public long getIdeologyId() {
        return this.ideologyId;
    }

    public void setHeadOfGovernmentId(long idMinister) {
        this.headOfGovernmentId = idMinister;
    }

    public long getHeadOfStateId() {
        return this.headOfStateId;
    }

    public void setMinisterIds(LongList ministerIds) {
        this.ministerIds = ministerIds;
    }

    public LongList getMinisterIds() {
        return this.ministerIds;
    }

    public void setHeadOfStateId(long idMinister) {
        this.headOfStateId = idMinister;
    }

    public long getHeadOfGovernmentId() {
        return this.headOfGovernmentId;
    }

    public void setIdentityId(long identityId) {
        this.identityId = identityId;
    }

    public long getIdentityId() {
        return this.identityId;
    }

    public void setAttitudeId(long attitudeId) {
        this.attitudeId = attitudeId;
    }

    public long getAttitudeId() {
        return this.attitudeId;
    }

    public void setLawIds(LongList lawIds) {
        this.lawIds = lawIds;
    }

    public LongList getLawIds() {
        return this.lawIds;
    }

    public void setLeadersIds(IntList leadersIds) {
        this.leadersIds = leadersIds;
    }

    public IntList getLeadersIds() {
        return this.leadersIds;
    }

    public String getName() {
        return this.name;
    }

    public void getLabelsData(String name, MapLabel mapLabel, FloatList vertices, ShortList indices) {
        this.name = name;
        Set<LandProvince> visitedProvinces = new ObjectSet<>();

        for (LandProvince province : this.provinces) {
            if (!visitedProvinces.contains(province)) {
                List<LandProvince> connectedProvinces = new ObjectList<>();
                this.getConnectedProvinces(province, visitedProvinces, connectedProvinces);
                if(connectedProvinces.size() > 5 || (connectedProvinces.size() == this.provinces.size() && !connectedProvinces.isEmpty())) {
                    IntList positionsProvinces = new IntList();
                    IntList pixelsBorderProvinces = new IntList();
                    for(LandProvince connectedProvince : connectedProvinces) {
                        positionsProvinces.add(connectedProvince.getPosition("default"));
                        pixelsBorderProvinces.addAll(connectedProvince.getBorderPixels());
                    }
                    mapLabel.generateData(name, pixelsBorderProvinces, positionsProvinces, vertices, indices);
                }
            }
        }
    }

    public void getConnectedProvinces(LandProvince province, Set<LandProvince> visitedProvinces, List<LandProvince> connectedProvinces) {
        visitedProvinces.add(province);
        connectedProvinces.add(province);
        for(Province adjacentProvince : province.getAdjacentProvinces()) {
            if(adjacentProvince instanceof LandProvince adjacentLandProvince) {
                if(!visitedProvinces.contains(adjacentProvince) && adjacentLandProvince.getCountryOwner().equals(this)) {
                    this.getConnectedProvinces(adjacentLandProvince, visitedProvinces, connectedProvinces);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Country country = (Country) o;
        return this.id.equals(country.id);
    }

    @Override
    public String toString() {
        return "Country{" +
                "id='" + this.id + '\'' +
                '}';
    }
}
