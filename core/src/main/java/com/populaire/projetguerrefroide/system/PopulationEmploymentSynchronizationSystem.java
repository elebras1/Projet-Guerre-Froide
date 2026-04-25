package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PopulationEmploymentSynchronizationSystem {

    public PopulationEmploymentSynchronizationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationEmploymentSynchronizationSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::synchronize);
    }

    private void synchronize(Iter iter) {
        long provinceId = 0;
        GeoHierarchyView geoHierarchy = null;
        ResourceGatheringView resourceGathering = null;
        ResourceGatheringTypeView resourceGatheringType = null;
        long regionInstanceId = 0;
        RegionInstanceView regionInstanceData = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            long currentProvinceId = population.provinceId();

            if(currentProvinceId != provinceId) {
                provinceId = currentProvinceId;
                EntityView province = iter.world().obtainEntityView(provinceId);
                geoHierarchy = province.getMutView(GeoHierarchy.class);
                resourceGathering = province.getMutView(ResourceGathering.class);
                if(resourceGathering != null) {
                    resourceGatheringType = iter.world().obtainEntityView(resourceGathering.typeId()).getMutView(ResourceGatheringType.class);
                } else {
                    resourceGatheringType = null;
                }
            }

            long currentRegionInstanceId = geoHierarchy.regionInstanceId();
            if(currentRegionInstanceId != regionInstanceId) {
                regionInstanceId = currentRegionInstanceId;
                EntityView regionInstance = iter.world().obtainEntityView(currentRegionInstanceId);
                regionInstanceData = regionInstance.getMutView(RegionInstance.class);
            }

            if (resourceGatheringType != null && population.typeId() == resourceGatheringType.slavePopTypeId()) {
                population.employment(resourceGathering.slaveAmount());
                continue;
            }

            if(resourceGatheringType != null && population.typeId() == resourceGatheringType.workerPopTypeId()) {
                population.employment(resourceGathering.workerAmount());
                continue;
            }

            population.employment((int) (population.amount() * regionInstanceData.workerPopTypeEmploymentRatios(population.index())));
        }
    }
}
