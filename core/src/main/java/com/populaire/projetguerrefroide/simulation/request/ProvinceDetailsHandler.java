package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.catalog.NamedTable;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.response.ProvinceDetails;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class ProvinceDetailsHandler extends RequestHandler<ProvinceDetails> {
    private final FlecsSystem regionProvincesSystem;
    private final FlecsSystem buildingsSystem;
    private long navalBaseTypeId;
    private long airBaseTypeId;
    private long radarStationTypeId;
    private long antiAirTypeId;
    private long storageBuildingTypeId;
    private float storageCapacityPerLevel;
    private ProvinceDetails response;
    private long provinceId;
    private long regionId;
    private long ownerId;
    private int populationRegion;
    private int workersRegion;
    private int industryRegion;
    private int navalBaseLevel;
    private int airBaseLevel;
    private int radarStationLevel;
    private int antiAircraftGunsLevel;

    public ProvinceDetailsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.regionProvincesSystem = this.ecsWorld.system("ProvinceDetailsRegionRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::scanRegionProvinces);
        this.buildingsSystem = this.ecsWorld.system("ProvinceDetailsBuildingsRequest")
            .kind(0)
            .with(Building.class)
            .iter(this::scanBuildings);
    }

    @Override
    public void setCatalog(Catalog catalog) {
        super.setCatalog(catalog);
        NamedTable buildingTypes = catalog.buildingTypes();
        for (int index = 0; index < buildingTypes.size(); index++) {
            switch (buildingTypes.nameId(index)) {
                case "naval_base" -> this.navalBaseTypeId = buildingTypes.entityId(index);
                case "air_base" -> this.airBaseTypeId = buildingTypes.entityId(index);
                case "radar_station" -> this.radarStationTypeId = buildingTypes.entityId(index);
                case "anti_air" -> this.antiAirTypeId = buildingTypes.entityId(index);
                default -> {
                }
            }
        }
        long globalStorageId = this.ecsWorld.lookup("global_strategic_storage");
        if (globalStorageId != 0) {
            EntityView globalStorage = this.ecsWorld.obtainEntityView(globalStorageId);
            if (globalStorage.has(GlobalStrategicStorage.class)) {
                GlobalStrategicStorageView strategicStorage = globalStorage.getView(GlobalStrategicStorage.class);
                this.storageBuildingTypeId = strategicStorage.buildingTypeId();
                this.storageCapacityPerLevel = strategicStorage.capacityPerLevel();
            }
        }
    }

    @Override
    protected boolean fill(RequestParams params, ProvinceDetails response) {
        int provinceIndex = params.index();
        response.clear(provinceIndex);
        if (provinceIndex < 0 || provinceIndex >= this.catalog.provinces().size()) {
            return false;
        }
        this.response = response;
        this.provinceId = this.catalog.provinces().entityId(provinceIndex);
        EntityView province = this.ecsWorld.obtainEntityView(this.provinceId);
        ProvinceView provinceData = province.getMutView(Province.class);
        this.regionId = provinceData.regionId();
        this.ownerId = provinceData.ownerId();
        int populationProvince = provinceData.childrenAmount() + provinceData.adultsAmount() + provinceData.seniorsAmount();
        for (int i = 0; i < provinceData.coreIdsLength(); i++) {
            long coreId = provinceData.coreIds(i);
            if (coreId != 0) {
                int coreIndex = this.countryIndex(coreId);
                if (coreIndex >= 0) {
                    response.addCore(coreIndex);
                }
            }
        }
        float resourceProduced = -1f;
        if (province.has(ResourceGathering.class)) {
            resourceProduced = province.<ResourceGatheringMutView>getMutView(ResourceGathering.class).production();
        }
        response.setProvince(this.catalog.provinces().regionIndex(provinceIndex), this.catalog.provinces().terrainIndex(provinceIndex), this.catalog.provinces().resourceGoodIndex(provinceIndex), this.countryIndex(this.ownerId), this.colonizerIndex(this.ownerId), resourceProduced, populationProvince);
        response.setLocal(0, 0, 0f, 0f);

        this.populationRegion = 0;
        this.workersRegion = 0;
        this.regionProvincesSystem.run();
        response.sortRegionProvincesByAdults();

        this.industryRegion = 0;
        this.navalBaseLevel = 0;
        this.airBaseLevel = 0;
        this.radarStationLevel = 0;
        this.antiAircraftGunsLevel = 0;
        this.buildingsSystem.run();
        response.sortEconomyBuildingsBySize();
        response.setDevelopmentLevels(this.navalBaseLevel, this.airBaseLevel, this.radarStationLevel, this.antiAircraftGunsLevel);
        response.setRegion(this.populationRegion, this.workersRegion, 0, 0, this.industryRegion);
        this.response = null;
        return true;
    }

    private void scanRegionProvinces(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.regionId() != this.regionId) {
                continue;
            }
            this.populationRegion += province.childrenAmount() + province.adultsAmount() + province.seniorsAmount();
            this.workersRegion += province.adultsAmount();
            int provinceIndex = this.catalog.provinces().indexOf(iter.entity(i));
            if (provinceIndex >= 0) {
                this.response.addRegionProvince(provinceIndex, province.adultsAmount());
            }
        }
    }

    private void scanBuildings(Iter iter) {
        Field<Building> buildingField = iter.field(Building.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            long parentId = building.parentId();
            int size = building.size();
            long buildingId = iter.entity(i);
            if (parentId == this.provinceId) {
                long typeId = this.ecsWorld.obtainEntityView(buildingId).target(Flecs.IsA, 0);
                if (typeId == this.navalBaseTypeId) {
                    this.navalBaseLevel = size;
                } else if (typeId == this.airBaseTypeId) {
                    this.airBaseLevel = size;
                } else if (typeId == this.radarStationTypeId) {
                    this.radarStationLevel = size;
                } else if (typeId == this.antiAirTypeId) {
                    this.antiAircraftGunsLevel = size;
                }
                continue;
            }
            if (parentId == 0) {
                continue;
            }
            EntityView parent = this.ecsWorld.obtainEntityView(parentId);
            if (!parent.has(RegionInstance.class)) {
                continue;
            }
            RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
            if (regionInstance.regionId() != this.regionId || regionInstance.ownerId() != this.ownerId) {
                continue;
            }
            float staffFill = parent.has(RegionSpecialBuildings.class) ? parent.<RegionSpecialBuildingsView>getView(RegionSpecialBuildings.class).staffFill() : 0f;
            EntityView buildingEntity = this.ecsWorld.obtainEntityView(buildingId);
            long typeId = buildingEntity.target(Flecs.IsA, 0);
            int typeIndex = this.catalog.buildingTypes().indexOf(typeId);
            if (typeIndex < 0) {
                continue;
            }
            int kind = this.catalog.buildingTypeKind(typeIndex);
            if (kind == Catalog.ECONOMY_BUILDING) {
                this.industryRegion++;
                this.response.addEconomyBuilding(typeIndex, size);
            } else if (kind == Catalog.SPECIAL_BUILDING && buildingEntity.has(SpecialBuilding.class)) {
                SpecialBuildingTypeView buildingType = this.ecsWorld.obtainEntityView(typeId).getView(SpecialBuildingType.class);
                int maxLevel = buildingType.maxLevel();
                int staffPosts = buildingType.staffPopTypeIndex() >= 0 ? (int) (size * buildingType.staffPosts()) : 0;
                float effectiveLevel = buildingEntity.<SpecialBuildingView>getView(SpecialBuilding.class).effectiveLevel();
                float storageCapacity = typeId == this.storageBuildingTypeId ? effectiveLevel * this.storageCapacityPerLevel : 0f;
                this.response.addSpecialBuilding(typeIndex, size, maxLevel, staffPosts, staffPosts > 0 ? Math.round(staffPosts * staffFill) : 0, effectiveLevel, storageCapacity);
            }
        }
    }
}
