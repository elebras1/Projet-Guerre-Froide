package com.populaire.projetguerrefroide.system;

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

    public MaintenanceSystem(World ecsWorld, long phaseId) {
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuilding.class)
            .with(DevelopmentBuildingType.class)
            .build();
        ecsWorld.system("MaintenanceSystem")
            .kind(phaseId)
            .with(CountryMaintenance.class)
            .run(this::maintain);
    }

    private void maintain(Iter iter) {
        World world = iter.world();
        while (iter.next()) {
            Field<CountryMaintenance> countryMaintenanceField = iter.field(CountryMaintenance.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMaintenanceMutView countryMaintenance = countryMaintenanceField.getMutView(i);
                for (int g = 0; g < countryMaintenance.needsLength(); g++) {
                    countryMaintenance.needs(g, 0f);
                }
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
    }
}
