package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_YEAR;
import static com.populaire.projetguerrefroide.util.Constants.MAINTENANCE_DECAY_YEARS;

public class MaintenanceSystem {

    private final Query developmentQuery;
    private final Query specialQuery;

    public MaintenanceSystem(World ecsWorld, long phaseId) {
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuilding.class)
            .with(DevelopmentBuildingType.class)
            .build();
        this.specialQuery = ecsWorld.query()
            .with(Building.class)
            .with(SpecialBuilding.class)
            .with(SpecialBuildingType.class)
            .build();
        ecsWorld.system("MaintenanceSystem")
            .kind(phaseId)
            .with(CountryMaintenance.class)
            .with(CountryStrategicStock.class)
            .with(CountryMilitary.class)
            .run(this::maintain);
    }

    private void maintain(Iter iter) {
        World world = iter.world();
        while (iter.next()) {
            Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 0);
            Field<CountryStrategicStock> countryStrategicStockField = iter.field(CountryStrategicStock.class, 1);
            Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 2);
            for (int i = 0; i < iter.count(); i++) {
                CountryMilitaryMutView countryMilitaryReset = countryMilitaryField.getMutView(i);
                countryMilitaryReset.instructors(0f);
                CountryMaintenanceMutView countryMaintenance = countryMaintenanceField.getMutView(i);
                for (int g = 0; g < countryMaintenance.needsLength(); g++) {
                    countryMaintenance.needs(g, 0f);
                }
                CountryStrategicStockMutView countryStrategicStock = countryStrategicStockField.getMutView(i);
                countryStrategicStock.capacity(0.0);
                countryStrategicStock.levels(0f);
            }
        }

        this.developmentQuery.iter(developmentIter -> {
            DevelopmentBuildingTypeView buildingType = developmentIter.field(DevelopmentBuildingType.class, 2).getView(0);
            if (buildingType.maintenanceGoodIndexes(0) < 0) {
                return;
            }
            long buildingTypeId = developmentIter.src(2);
            Field<Building> buildingField = developmentIter.field(Building.class, 0);
            Field<DevelopmentBuilding> developmentBuildingField = developmentIter.field(DevelopmentBuilding.class, 1);
            long countryId = 0;
            CountryMaintenanceMutView countryMaintenance = null;
            for (int j = 0; j < developmentIter.count(); j++) {
                BuildingView building = buildingField.getMutView(j);
                if (building.size() <= 0 || building.countryId() == 0) {
                    continue;
                }
                if (building.countryId() != countryId) {
                    countryId = building.countryId();
                    countryMaintenance = world.obtainEntityView(countryId).getMutView(CountryMaintenance.class);
                }
                for (int k = 0; k < buildingType.maintenanceGoodIndexesLength(); k++) {
                    int goodIndex = buildingType.maintenanceGoodIndexes(k);
                    if (goodIndex < 0) {
                        break;
                    }
                    countryMaintenance.needs(goodIndex, countryMaintenance.needs(goodIndex) + building.size() * buildingType.maintenanceGoodAmounts(k) / DAYS_PER_YEAR);
                }
                DevelopmentBuildingMutView developmentBuilding = developmentBuildingField.getMutView(j);
                float decay = developmentBuilding.maintenanceDecay() + (1f - countryMaintenance.satisfaction()) / (MAINTENANCE_DECAY_YEARS * DAYS_PER_YEAR);
                if (decay >= 1f) {
                    InfrastructureUtils.levelDown(world, developmentIter.entity(j), buildingTypeId);
                    decay -= 1f;
                }
                developmentBuilding.maintenanceDecay(decay);
            }
        });

        EntityView globalStorage = world.obtainEntityView(world.lookup("global_strategic_storage"));
        GlobalStrategicStorageView strategicStorage = globalStorage.has(GlobalStrategicStorage.class) ? globalStorage.getView(GlobalStrategicStorage.class) : null;
        EntityView globalFacilities = world.obtainEntityView(world.lookup("global_military_facilities"));
        long academyTypeId = globalFacilities.has(GlobalMilitaryFacilities.class) ? globalFacilities.<GlobalMilitaryFacilitiesView>getView(GlobalMilitaryFacilities.class).academyTypeId() : 0;
        int statePostIndex = world.obtainEntityView(world.lookup("global_population_type")).<GlobalPopulationTypeView>getView(GlobalPopulationType.class).statePostPopTypeIndex();
        this.specialQuery.iter(specialIter -> {
            SpecialBuildingTypeView buildingType = specialIter.field(SpecialBuildingType.class, 2).getView(0);
            long buildingTypeId = specialIter.src(2);
            boolean storage = strategicStorage != null && strategicStorage.buildingTypeId() == buildingTypeId;
            boolean academy = academyTypeId != 0 && academyTypeId == buildingTypeId;
            boolean stateStaff = buildingType.staffPopTypeIndex() == statePostIndex;
            boolean staffed = buildingType.staffPopTypeIndex() >= 0 && buildingType.staffPosts() > 0f;
            boolean maintained = buildingType.maintenanceGoodIndexes(0) >= 0;
            Field<Building> buildingField = specialIter.field(Building.class, 0);
            Field<SpecialBuilding> specialBuildingField = specialIter.field(SpecialBuilding.class, 1);
            long countryId = 0;
            CountryMaintenanceMutView countryMaintenance = null;
            CountryStrategicStockMutView countryStrategicStock = null;
            CountryMilitaryMutView countryMilitary = null;
            for (int j = 0; j < specialIter.count(); j++) {
                BuildingView building = buildingField.getMutView(j);
                SpecialBuildingMutView specialBuilding = specialBuildingField.getMutView(j);
                if (building.size() <= 0 || building.countryId() == 0) {
                    specialBuilding.effectiveLevel(0f);
                    continue;
                }
                if (building.countryId() != countryId) {
                    countryId = building.countryId();
                    EntityView country = world.obtainEntityView(countryId);
                    countryMaintenance = country.getMutView(CountryMaintenance.class);
                    countryStrategicStock = country.getMutView(CountryStrategicStock.class);
                    countryMilitary = country.getMutView(CountryMilitary.class);
                }
                float served = 1f;
                if (maintained) {
                    for (int k = 0; k < buildingType.maintenanceGoodIndexesLength(); k++) {
                        int goodIndex = buildingType.maintenanceGoodIndexes(k);
                        if (goodIndex < 0) {
                            break;
                        }
                        countryMaintenance.needs(goodIndex, countryMaintenance.needs(goodIndex) + building.size() * buildingType.maintenanceGoodAmounts(k) / DAYS_PER_YEAR);
                    }
                    served = countryMaintenance.satisfaction();
                    float decay = specialBuilding.maintenanceDecay() + (1f - served) / (MAINTENANCE_DECAY_YEARS * DAYS_PER_YEAR);
                    if (decay >= 1f) {
                        InfrastructureUtils.levelDown(world, specialIter.entity(j), buildingTypeId);
                        decay -= 1f;
                    }
                    specialBuilding.maintenanceDecay(decay);
                }
                float staffFill = 1f;
                if (staffed && stateStaff) {
                    staffFill = world.obtainEntityView(building.parentId()).<RegionSpecialBuildingsView>getView(RegionSpecialBuildings.class).staffFill();
                } else if (staffed) {
                    staffFill = countryMilitary.academyPosts() > 0f ? countryMilitary.academyFilled() / countryMilitary.academyPosts() : 0f;
                }
                float effectiveLevel = building.size() * staffFill * served;
                specialBuilding.effectiveLevel(effectiveLevel);
                if (academy) {
                    countryMilitary.instructors(countryMilitary.instructors() + effectiveLevel * buildingType.staffPosts());
                }
                if (storage) {
                    countryStrategicStock.capacity(countryStrategicStock.capacity() + (double) effectiveLevel * strategicStorage.capacityPerLevel());
                    countryStrategicStock.levels(countryStrategicStock.levels() + building.size());
                }
            }
        });
    }
}
