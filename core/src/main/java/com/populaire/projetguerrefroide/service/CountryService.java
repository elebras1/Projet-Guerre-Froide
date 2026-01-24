package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.LongOrderedSet;
import com.github.tommyettinger.ds.LongSet;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.CountryDto;
import com.populaire.projetguerrefroide.dto.CountrySummaryDto;
import com.populaire.projetguerrefroide.dto.RegionDto;
import com.populaire.projetguerrefroide.dto.RegionsBuildingsDto;
import com.populaire.projetguerrefroide.pojo.MutableInt;
import com.populaire.projetguerrefroide.repository.QueryRepository;
import com.populaire.projetguerrefroide.ui.view.SortType;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.util.Comparator;
import java.util.List;

public class CountryService {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final RegionService regionService;

    public CountryService(GameContext gameContext, QueryRepository queryRepository, RegionService regionService) {
        this.gameContext = gameContext;
        this.queryRepository = queryRepository;
        this.regionService = regionService;
    }

    public CountrySummaryDto buildSummary(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedCountry = ecsWorld.obtainEntity(countryId);
        Country countryData = selectedCountry.get(Country.class);
        Minister headOfState = this.getHeadOfState(selectedCountry.id());
        String portraitNameFile = "admin_type";
        if(headOfState.imageFileName() != null) {
            portraitNameFile = headOfState.imageFileName();
        }
        int population = this.getPopulationAmount(selectedCountry.id());
        List<String> allies = this.getAlliesOfSelectedCountry(selectedCountry.id());
        String government = ecsWorld.obtainEntity(countryData.governmentId()).getName();

        return new CountrySummaryDto(selectedCountry.getName(), population, government, portraitNameFile, headOfState.name(), this.getColonizerNameId(selectedCountry.id()), allies);
    }

    public CountryDto buildDetails(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity selectedCountry = ecsWorld.obtainEntity(countryId);
        int population = this.getPopulationAmount(selectedCountry.id());
        int manpower = 0;
        int grossDomesticProduct = 0;
        int money = 0;
        int supplies = 0;
        int fuel = 0;
        float diplomaticInfluence = 0;
        int uranium = 0;
        int dissent = 0;
        int nationalUnity = 0;
        int ranking = 0;

        return new CountryDto(population, manpower, grossDomesticProduct, money, supplies, fuel, diplomaticInfluence, uranium, dissent, nationalUnity, ranking);
    }

    public RegionsBuildingsDto buildRegionsBuildings(long countryId, SortType sortType) {
        World ecsWorld = this.gameContext.getEcsWorld();

        LongOrderedSet regionIds = new LongOrderedSet();
        Query query = this.queryRepository.getProvincesWithGeoHierarchy();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 1);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(countryId == provinceView.ownerId()) {
                    GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);
                    regionIds.add(geoHierarchyView.regionId());
                }
            }
        });

        List<RegionDto> regions = new ObjectList<>();
        LongSet.LongSetIterator iterator = regionIds.iterator();
        while(iterator.hasNext()) {
            Entity region = ecsWorld.obtainEntity(iterator.nextLong());
            RegionDto regionData = this.regionService.buildDetails(countryId, region.id(), region.getName());
            regions.add(regionData);
        }

        this.sortRegions(regions, sortType);

        return new RegionsBuildingsDto(regions);
    }

    public String getName(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity country = ecsWorld.obtainEntity(countryId);
        return country.getName();
    }

    public int getPopulationAmount(long countryId) {
        MutableInt population = new MutableInt(0);
        Query query = this.queryRepository.getProvinces();
        query.iter(iter -> {
            Field<Province> provinceField = iter.field(Province.class, 0);
            for(int i = 0; i < iter.count(); i++) {
                ProvinceView provinceView = provinceField.getMutView(i);
                if(provinceView.ownerId() == countryId) {
                    population.increment(provinceView.amountChildren() + provinceView.amountAdults() + provinceView.amountSeniors());
                }
            }
        });

        return population.getValue();
    }

    public String getColonizerNameId(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();
        Entity country = ecsWorld.obtainEntity(countryId);
        long countryColonizerId = country.target(ecsConstants.isColonyOf());
        if(countryColonizerId != 0) {
            Entity colony = ecsWorld.obtainEntity(countryColonizerId);
            return colony.getName();
        }

        return null;
    }

    public Position getCapitalPosition(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity country = ecsWorld.obtainEntity(countryId);
        Country countryData = country.get(Country.class);
        Entity capitalProvince = ecsWorld.obtainEntity(countryData.capitalId());
        long capitalPositionId = ecsWorld.lookup("province_" + capitalProvince.getName() + "_pos_default");
        Entity capitalPosition = ecsWorld.obtainEntity(capitalPositionId);
        return capitalPosition.get(Position.class);
    }

    public List<String> getAlliesOfSelectedCountry(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        List<String> allies = new ObjectList<>();

        Entity country = ecsWorld.obtainEntity(countryId);
        if (country == null) {
            return allies;
        }

        long[] relations = new long[] {
            ecsConstants.alliedWith(),
            ecsConstants.guarantees(),
            ecsConstants.isGuaranteedBy(),
            ecsConstants.isPuppetMasterOf(),
            ecsConstants.isPuppetOf(),
            ecsConstants.colonizes(),
            ecsConstants.isColonyOf()
        };

        for (long relation : relations) {
            int i = 0;
            long alliedCountryId = country.target(relation, i);

            while (alliedCountryId != 0) {

                if (relation != ecsConstants.isColonyOf()) {
                    Entity alliedCountry = ecsWorld.obtainEntity(alliedCountryId);
                    if (alliedCountry != null) {
                        String alliedCountryNameId = alliedCountry.getName();
                        if (!allies.contains(alliedCountryNameId)) {
                            allies.add(alliedCountryNameId);
                        }
                    }
                }

                i++;
                alliedCountryId = country.target(relation, i);
            }
        }

        return allies;
    }

    public Minister getHeadOfState(long countryId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        EcsConstants ecsConstants = this.gameContext.getEcsConstants();

        Entity country = ecsWorld.obtainEntity(countryId);
        Country countryData = country.get(Country.class);
        long headOfStateId = countryData.headOfStateId();

        long countryColonizerId = country.target(ecsConstants.isColonyOf());
        if(countryColonizerId != 0) {
            Entity colonizerCountry = ecsWorld.obtainEntity(countryColonizerId);
            Country colonizerCountryData = colonizerCountry.get(Country.class);
            headOfStateId = colonizerCountryData.headOfStateId();
        }

        Minister headOfState = null;
        if(headOfStateId != 0) {
            Entity entityMinister = ecsWorld.obtainEntity(headOfStateId);
            headOfState = entityMinister.get(Minister.class);
        }

        return headOfState;
    }

    private void sortRegions(List<RegionDto> regions, SortType sortType) {
        switch (sortType) {
            case DEVELOPPEMENT_INDEX -> regions.sort(Comparator.comparingInt(RegionDto::developpementIndexValue).reversed());
            case POPULATION -> regions.sort(Comparator.comparingInt(RegionDto::populationAmount).reversed());
            case WORKFORCE -> regions.sort(Comparator.comparingInt(RegionDto::buildingWorkerAmount).reversed());
        }
    }
}
