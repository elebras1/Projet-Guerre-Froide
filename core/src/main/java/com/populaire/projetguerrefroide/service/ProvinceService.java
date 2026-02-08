package com.populaire.projetguerrefroide.service;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Query;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dto.DevelopementBuildingLevelDto;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.pojo.MutableInt;
import com.populaire.projetguerrefroide.repository.QueryRepository;

import java.util.List;

public class ProvinceService {
    private final GameContext gameContext;
    private final QueryRepository queryRepository;
    private final CountryService countryService;
    private final RegionService regionService;

    public ProvinceService(GameContext gameContext, QueryRepository queryRepository, CountryService countryService, RegionService regionService) {
        this.gameContext = gameContext;
        this.queryRepository = queryRepository;
        this.countryService = countryService;
        this.regionService = regionService;
    }

    public ProvinceDto buildDetails(long provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity province = ecsWorld.obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        GeoHierarchy provinceGeoHierarchy = province.get(GeoHierarchy.class);
        Entity region = ecsWorld.obtainEntity(provinceGeoHierarchy.regionId());
        String provinceNameId = province.getName();
        String regionNameId = region.getName();
        Entity terrain = ecsWorld.obtainEntity(provinceData.terrainId());
        String terrainImage = terrain.getName();
        String resourceImage = this.getResourceGoodName(province.id());
        int populationRegion = this.getPopulationAmount(provinceId);
        int workersRegion = this.regionService.getWorkerAmount(provinceGeoHierarchy.regionId());
        int populationProvince = this.getPopulationAmount(province.id());
        int developmentIndexRegion = 0;
        int incomeRegion = 0;
        int numberIndustryRegion = this.regionService.getNumberIndustry(region.id(), provinceData.ownerId());
        Entity country = ecsWorld.obtainEntity(provinceData.ownerId());
        String countryNameId = country.getName();
        String colonizerId = this.countryService.getColonizerNameId(country.id());
        List<String> flagCountriesCore = this.getCountryCoreNameIds(provinceId);
        float resourceProduced = this.getResourceGatheringProduction(provinceNameId);
        List<String> provinceIdsRegion = this.regionService.getProvinceNameIdsOrderByPopulation(region.id());
        DevelopementBuildingLevelDto developmentBuildingLevel = this.getDevelopementBuildingLevel(province.id());
        List<String> specialBuildings = this.regionService.getSpecialBuildingNames(region.id(), provinceData.ownerId());
        List<String> colorsBuilding = this.regionService.getColorBuildingsOrderByLevel(region.id(), provinceData.ownerId());

        return new ProvinceDto(provinceNameId, regionNameId, terrainImage, resourceImage, populationRegion, workersRegion, developmentIndexRegion, incomeRegion, numberIndustryRegion, countryNameId, colonizerId, flagCountriesCore, resourceProduced, 0, 0, populationProvince, 0f, 0f, provinceIdsRegion, developmentBuildingLevel, specialBuildings, colorsBuilding);
    }

    public String getName(long provinceId) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        return province.getName();
    }

    public String getCountryNameId(long provinceId) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        Entity country = this.gameContext.getEcsWorld().obtainEntity(provinceData.ownerId());
        return country.getName();
    }

    public int getPopulationAmount(long provinceId) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        return provinceData.amountChildren() + provinceData.amountAdults() + provinceData.amountSeniors();
    }

    public ObjectIntMap<String> getCultures(long provinceId) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        CultureDistribution cultureDistribution = province.get(CultureDistribution.class);
        long[] provinceCultureIds = cultureDistribution.populationIds();
        int[] provinceCultureValues = cultureDistribution.populationAmounts();
        return this.calculatePercentageDistributionFromProvinceData(provinceCultureIds, provinceCultureValues, amountAdults);
    }

    public ObjectIntMap<String> getReligions(long provinceId) {
        Entity province = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        int amountAdults = provinceData.amountAdults();
        ReligionDistribution religionDistribution = province.get(ReligionDistribution.class);
        long[] provinceReligionIds = religionDistribution.populationIds();
        int[] provinceReligionValues = religionDistribution.populationAmounts();

        return this.calculatePercentageDistributionFromProvinceData(provinceReligionIds, provinceReligionValues, amountAdults);
    }

    public DevelopementBuildingLevelDto getDevelopementBuildingLevel(long provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        MutableInt navalBaseLevel = new MutableInt(0);
        MutableInt airBaseLevel = new MutableInt(0);
        MutableInt radarStationLevel = new MutableInt(0);
        MutableInt antiAircraftGunsLevel = new MutableInt(0);

        try(Query query = ecsWorld.query().with(Building.class).build()) {
            query.iter(iter -> {
                Field<Building> buildingField = iter.field(Building.class, 0);
                for(int i = 0; i < iter.count(); i++) {
                    BuildingView buildingView = buildingField.getMutView(i);
                    if(buildingView.parentId() != provinceId) {
                        continue;
                    }

                    Entity buildingType = ecsWorld.obtainEntity(buildingView.typeId());

                    switch (buildingType.getName()) {
                        case "naval_base" -> navalBaseLevel.setValue(buildingView.size());
                        case "air_base" -> airBaseLevel.setValue(buildingView.size());
                        case "radar_station" -> radarStationLevel.setValue(buildingView.size());
                        case "anti_air" -> antiAircraftGunsLevel.setValue(buildingView.size());
                    }
                }
            });
        }

        return new DevelopementBuildingLevelDto((byte) navalBaseLevel.getValue(), (byte)  airBaseLevel.getValue(), (byte)  radarStationLevel.getValue(), (byte)  antiAircraftGunsLevel.getValue());
    }

    public List<String> getCountryCoreNameIds(long provinceId) {
        World ecsWorld = this.gameContext.getEcsWorld();
        Entity province = ecsWorld.obtainEntity(provinceId);
        Province provinceData = province.get(Province.class);
        List<String> countriesCore = new ObjectList<>();
        for (int i = 0; i < provinceData.coreIds().length; i++) {
            long coreCountryId = provinceData.coreIds()[i];
            if (coreCountryId == 0) {
                continue;
            }

            String countryNameId = ecsWorld.obtainEntity(coreCountryId).getName();
            countriesCore.add(countryNameId);
        }

        return countriesCore;
    }

    public String getResourceGoodName(long provinceId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceId);
        ResourceGathering resourceGathering = provinceEntity.get(ResourceGathering.class);
        if(resourceGathering != null) {
            return this.gameContext.getEcsWorld().obtainEntity(resourceGathering.goodId()).getName();
        }
        return null;
    }

    public float getResourceGatheringProduction(String provinceNameId) {
        long provinceEntityId = this.gameContext.getEcsWorld().lookup(provinceNameId);
        if (provinceEntityId == 0) {
            return -1f;
        }
        return this.getProduction(provinceEntityId);
    }

    public float getProduction(long provinceEntityId) {
        Entity provinceEntity = this.gameContext.getEcsWorld().obtainEntity(provinceEntityId);
        ResourceGathering state = provinceEntity.get(ResourceGathering.class);
        if (state != null) {
            return state.production();
        }
        return -1f;
    }

    private ObjectIntMap<String> calculatePercentageDistributionFromProvinceData(long[] provinceElementIds, int[] provinceElementValues, int amountAdults) {
        ObjectIntOrderedMap<String> elementPercentages = new ObjectIntOrderedMap<>();
        World ecsWorld = this.gameContext.getEcsWorld();
        int total = 0;
        int biggestElementIndex = -1;
        for(int elementIndex = 0; elementIndex < provinceElementIds.length && provinceElementValues[elementIndex] != 0; elementIndex++) {
            int amount = provinceElementValues[elementIndex];
            long elementId = provinceElementIds[elementIndex];
            if(biggestElementIndex == -1 || amount > provinceElementValues[biggestElementIndex]) {
                biggestElementIndex = elementIndex;
            }
            if(amountAdults != 0) {
                int percentage = (int) ((amount / (float) amountAdults) * 100);
                total += percentage;
                elementPercentages.put(ecsWorld.obtainEntity(elementId).getName(), percentage);
            } else {
                elementPercentages.put(ecsWorld.obtainEntity(elementId).getName(), 0);
            }
        }

        if(total != 100 && biggestElementIndex != -1) {
            int difference = 100 - total;
            String biggestElementName = ecsWorld.obtainEntity(provinceElementIds[biggestElementIndex]).getName();
            elementPercentages.put(biggestElementName, elementPercentages.get(biggestElementName) + difference);
        }

        this.sortByValueDescending(elementPercentages);

        return elementPercentages;
    }

    private void sortByValueDescending(ObjectIntOrderedMap<String> map) {
        ObjectList<String> keys = map.order();
        int size = map.size();
        for(int i = 1; i < size; i++){
            String key = keys.get(i);
            int value = map.get(key);
            int j = i - 1;
            while(j >= 0 && map.get(keys.get(j)) < value){
                keys.set(j + 1, keys.get(j));
                j--;
            }
            keys.set(j + 1, key);
        }
    }
}
