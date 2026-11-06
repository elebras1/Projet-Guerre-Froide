package com.populaire.projetguerrefroide.simulation.request;

import com.github.tommyettinger.ds.LongIntMap;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.request.SortType;
import com.populaire.projetguerrefroide.protocol.response.Columns;
import com.populaire.projetguerrefroide.protocol.response.RegionsBuildings;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Flecs;
import io.github.elebras1.flecs.FlecsSystem;
import io.github.elebras1.flecs.Iter;

public final class RegionsBuildingsHandler extends RequestHandler<RegionsBuildings> {
    private static final SortType[] SORT_TYPES = SortType.values();
    private final FlecsSystem constructionSitesSystem;
    private final FlecsSystem provincesSystem;
    private final FlecsSystem buildingsSystem;
    private final LongIntMap levelsQueued;
    private final LongIntMap regionSlots;
    private long countryId;
    private int regionCount;
    private long[] regionIds = new long[64];
    private int[] regionPopulations = new int[64];
    private int[] regionWorkers = new int[64];
    private int[] regionOrder = new int[64];
    private int[] regionBuildingCounts = new int[64];
    private int[] regionBuildingStarts = new int[64];
    private int buildingCount;
    private int[] buildingRegionSlots = new int[512];
    private long[] buildingIds = new long[512];
    private int[] buildingTypes = new int[512];
    private int[] buildingSizes = new int[512];
    private int[] buildingMaxLevels = new int[512];
    private int[] buildingWorkers = new int[512];
    private float[] buildingProductions = new float[512];
    private boolean[] buildingSuspended = new boolean[512];
    private int[] buildingOrder = new int[512];

    public RegionsBuildingsHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        super(simulationContext, state, header);
        this.levelsQueued = new LongIntMap();
        this.regionSlots = new LongIntMap();
        this.regionSlots.setDefaultValue(-1);
        this.constructionSitesSystem = this.ecsWorld.system("RegionsBuildingsSitesRequest")
            .kind(0)
            .with(ConstructionSite.class)
            .iter(this::countSites);
        this.provincesSystem = this.ecsWorld.system("RegionsBuildingsProvincesRequest")
            .kind(0)
            .with(Province.class)
            .iter(this::collectRegions);
        this.buildingsSystem = this.ecsWorld.system("RegionsBuildingsBuildingsRequest")
            .kind(0)
            .with(Building.class)
            .iter(this::collectBuildings);
    }

    @Override
    protected boolean fill(RequestParams params, RegionsBuildings response) {
        response.clear();
        this.countryId = this.state.playerCountryId();
        if (this.countryId == 0) {
            return false;
        }
        this.levelsQueued.clear();
        this.regionSlots.clear();
        this.regionCount = 0;
        this.buildingCount = 0;
        this.constructionSitesSystem.run();
        this.provincesSystem.run();
        this.buildingsSystem.run();

        for (int slot = 0; slot < this.regionCount; slot++) {
            this.regionBuildingCounts[slot] = 0;
            this.regionWorkers[slot] = 0;
        }
        for (int building = 0; building < this.buildingCount; building++) {
            int slot = this.buildingRegionSlots[building];
            this.regionBuildingCounts[slot]++;
            this.regionWorkers[slot] += this.buildingWorkers[building];
        }
        int start = 0;
        for (int slot = 0; slot < this.regionCount; slot++) {
            this.regionBuildingStarts[slot] = start;
            start += this.regionBuildingCounts[slot];
            this.regionBuildingCounts[slot] = 0;
        }
        for (int building = 0; building < this.buildingCount; building++) {
            int slot = this.buildingRegionSlots[building];
            this.buildingOrder[this.regionBuildingStarts[slot] + this.regionBuildingCounts[slot]] = building;
            this.regionBuildingCounts[slot]++;
        }
        for (int slot = 0; slot < this.regionCount; slot++) {
            this.sortBuildingsById(this.regionBuildingStarts[slot], this.regionBuildingCounts[slot]);
        }

        int option = params.option();
        SortType sortType = option >= 0 && option < SORT_TYPES.length ? SORT_TYPES[option] : SortType.DEFAULT;
        for (int slot = 0; slot < this.regionCount; slot++) {
            this.regionOrder[slot] = slot;
        }
        this.sortRegions(sortType);

        for (int position = 0; position < this.regionCount; position++) {
            int slot = this.regionOrder[position];
            int population = this.regionPopulations[slot];
            int workers = this.regionWorkers[slot];
            int workerRatio = population > 0 ? (int) ((workers * 100.0f) / population) : 0;
            response.addRegion(this.catalog.regions().indexOf(this.regionIds[slot]), population, workers, workerRatio, 0);
            int first = this.regionBuildingStarts[slot];
            for (int i = 0; i < this.regionBuildingCounts[slot]; i++) {
                int building = this.buildingOrder[first + i];
                long buildingId = this.buildingIds[building];
                response.addBuilding(buildingId, this.buildingTypes[building], this.buildingSizes[building], this.buildingMaxLevels[building], this.buildingWorkers[building], this.buildingProductions[building], this.levelsQueued.get(buildingId), this.buildingSuspended[building]);
            }
        }
        return true;
    }

    private void countSites(Iter iter) {
        Field<ConstructionSite> siteField = iter.field(ConstructionSite.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ConstructionSiteView site = siteField.getMutView(i);
            this.levelsQueued.getAndIncrement(site.buildingId(), 0, 1);
        }
    }

    private void collectRegions(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != this.countryId) {
                continue;
            }
            long regionId = province.regionId();
            int slot = this.regionSlots.get(regionId);
            if (slot < 0) {
                slot = this.regionCount++;
                this.fitRegions(this.regionCount);
                this.regionSlots.put(regionId, slot);
                this.regionIds[slot] = regionId;
                this.regionPopulations[slot] = 0;
            }
            this.regionPopulations[slot] += province.adultsAmount();
        }
    }

    private void collectBuildings(Iter iter) {
        Field<Building> buildingField = iter.field(Building.class, 0);
        long suspendedTag = this.ecsConstants.suspended();
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            long parentId = building.parentId();
            if (parentId == 0) {
                continue;
            }
            EntityView parent = this.ecsWorld.obtainEntityView(parentId);
            if (!parent.has(RegionInstance.class)) {
                continue;
            }
            RegionInstanceView regionInstance = parent.getMutView(RegionInstance.class);
            if (regionInstance.ownerId() != this.countryId) {
                continue;
            }
            int slot = this.regionSlots.get(regionInstance.regionId());
            if (slot < 0) {
                continue;
            }
            long buildingId = iter.entity(i);
            EntityView buildingEntity = this.ecsWorld.obtainEntityView(buildingId);
            long typeId = buildingEntity.target(Flecs.IsA, 0);
            int typeIndex = this.catalog.buildingTypes().indexOf(typeId);
            if (typeIndex < 0 || this.catalog.buildingTypeKind(typeIndex) != Catalog.ECONOMY_BUILDING) {
                continue;
            }
            int position = this.buildingCount++;
            this.fitBuildings(this.buildingCount);
            EconomyBuildingView economyBuilding = buildingEntity.getMutView(EconomyBuilding.class);
            this.buildingRegionSlots[position] = slot;
            this.buildingIds[position] = buildingId;
            this.buildingTypes[position] = typeIndex;
            this.buildingSizes[position] = building.size();
            this.buildingWorkers[position] = economyBuilding.primaryWorkerAmount() + economyBuilding.secondaryWorkerAmount();
            this.buildingProductions[position] = economyBuilding.production();
            this.buildingSuspended[position] = buildingEntity.has(suspendedTag) || economyBuilding.suspended();
            this.buildingMaxLevels[position] = this.ecsWorld.obtainEntityView(typeId).<EconomyBuildingTypeView>getView(EconomyBuildingType.class).maxLevel();
        }
    }

    private void sortBuildingsById(int first, int count) {
        for (int i = first + 1; i < first + count; i++) {
            int building = this.buildingOrder[i];
            long id = this.buildingIds[building];
            int j = i - 1;
            while (j >= first && this.buildingIds[this.buildingOrder[j]] > id) {
                this.buildingOrder[j + 1] = this.buildingOrder[j];
                j--;
            }
            this.buildingOrder[j + 1] = building;
        }
    }

    private void sortRegions(SortType sortType) {
        if (sortType == SortType.DEFAULT || sortType == SortType.DEVELOPPEMENT_INDEX) {
            return;
        }
        for (int i = 1; i < this.regionCount; i++) {
            int slot = this.regionOrder[i];
            int key = this.regionKey(slot, sortType);
            int j = i - 1;
            while (j >= 0 && this.regionKey(this.regionOrder[j], sortType) < key) {
                this.regionOrder[j + 1] = this.regionOrder[j];
                j--;
            }
            this.regionOrder[j + 1] = slot;
        }
    }

    private int regionKey(int slot, SortType sortType) {
        return sortType == SortType.POPULATION ? this.regionPopulations[slot] : this.regionWorkers[slot];
    }

    private void fitRegions(int size) {
        this.regionIds = Columns.fit(this.regionIds, size);
        this.regionPopulations = Columns.fit(this.regionPopulations, size);
        this.regionWorkers = Columns.fit(this.regionWorkers, size);
        this.regionOrder = Columns.fit(this.regionOrder, size);
        this.regionBuildingCounts = Columns.fit(this.regionBuildingCounts, size);
        this.regionBuildingStarts = Columns.fit(this.regionBuildingStarts, size);
    }

    private void fitBuildings(int size) {
        this.buildingRegionSlots = Columns.fit(this.buildingRegionSlots, size);
        this.buildingIds = Columns.fit(this.buildingIds, size);
        this.buildingTypes = Columns.fit(this.buildingTypes, size);
        this.buildingSizes = Columns.fit(this.buildingSizes, size);
        this.buildingMaxLevels = Columns.fit(this.buildingMaxLevels, size);
        this.buildingWorkers = Columns.fit(this.buildingWorkers, size);
        this.buildingProductions = Columns.fit(this.buildingProductions, size);
        this.buildingSuspended = Columns.fit(this.buildingSuspended, size);
        this.buildingOrder = Columns.fit(this.buildingOrder, size);
    }
}
