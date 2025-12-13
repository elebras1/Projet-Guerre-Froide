package com.populaire.projetguerrefroide.map;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.politics.*;
import com.populaire.projetguerrefroide.national.Attitude;
import com.populaire.projetguerrefroide.national.Identity;

import java.util.*;
import java.util.List;

public class Country {
    private final String id;
    private final int color;
    private Set<Region> regions;
    private List<LandProvince> provinces;
    private ObjectIntMap<Country> relations;
    private Map<Country, AllianceType> alliances;
    private LandProvince capital;
    private Government government;
    private Ideology ideology;
    private short headOfGovernmentId;
    private short headOfStateId;
    private ShortList ministerIds;
    private Identity identity;
    private Attitude attitude;
    private String name;
    private Map<LawGroup, Law> laws;
    private IntList leadersIds;

    public Country(String id, int color) {
        this.id = id;
        this.color = color;
        this.regions = new ObjectSet<>();
        this.provinces = new ObjectList<>();
        this.capital = null;
        this.government = null;
        this.ideology = null;
        this.headOfGovernmentId = -1;
        this.headOfStateId = -1;
        this.identity = null;
        this.attitude = null;
    }

    public String getId() {
        return this.id;
    }

    public int getColor() {
        return this.color;
    }

    public void addRegion(Region region) {
        this.regions.add(region);
    }

    public Set<Region> getRegions() {
        return this.regions;
    }

    public void addProvince(LandProvince province) {
        this.provinces.add(province);
    }

    public List<LandProvince> getProvinces() {
        return this.provinces;
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

    public void setCapital(LandProvince capital) {
        this.capital = capital;
    }

    public LandProvince getCapital() {
        return this.capital;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    public Government getGovernment() {
        return this.government;
    }

    public void setIdeology(Ideology ideology) {
        this.ideology = ideology;
    }

    public Ideology getIdeology() {
        return this.ideology;
    }

    public void setHeadOfGovernmentId(short idMinister) {
        this.headOfGovernmentId = idMinister;
    }

    public short getHeadOfStateId() {
        return this.headOfStateId;
    }

    public void setMinisterIds(ShortList ministerIds) {
        this.ministerIds = ministerIds;
    }

    public ShortList getMinisterIds() {
        return this.ministerIds;
    }

    public void setHeadOfStateId(short idMinister) {
        this.headOfStateId = idMinister;
    }

    public short getHeadOfGovernmentId() {
        return this.headOfGovernmentId;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setAttitude(Attitude attitude) {
        this.attitude = attitude;
    }

    public Attitude getAttitude() {
        return this.attitude;
    }

    public void setLaws(Map<LawGroup, Law> laws) {
        this.laws = laws;
    }

    public Map<LawGroup, Law> getLaws() {
        return this.laws;
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
                ", color='" + this.color + '\'' +
                '}';
    }
}
