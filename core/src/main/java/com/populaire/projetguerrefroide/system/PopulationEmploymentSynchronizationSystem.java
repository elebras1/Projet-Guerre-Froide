package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class PopulationEmploymentSynchronizationSystem {

    public PopulationEmploymentSynchronizationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("PopulationEmploymentSynchronizationSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .with(ecsConstants.ruralTag()).optional()
            .multiThreaded()
            .iter(this::synchronize);
    }

    private void synchronize(Iter iter) {
        long provinceId = 0;
        ResourceGatheringView resourceGathering = null;
        ResourceGatheringTypeView resourceGatheringType = null;
        long regionInstanceId = 0;
        RegionInstanceView regionInstanceData = null;

        Field<PopulationType> populationTypeField = iter.field(PopulationType.class, 1);
        PopulationTypeView populationTypeData = populationTypeField.getView(0);
        int popTypeIndex = populationTypeData.index();
        long popTypeId = iter.src(1);
        boolean rural = iter.isSet(2);

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if(rural && population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView province = iter.world().obtainEntityView(provinceId);
                if(province.has(ResourceGathering.class)) {
                    resourceGathering = province.getMutView(ResourceGathering.class);
                    resourceGatheringType = iter.world().obtainEntityView(resourceGathering.typeId()).getMutView(ResourceGatheringType.class);
                } else {
                    resourceGatheringType = null;
                }
            }

            long currentRegionInstanceId = population.regionInstanceId();
            if(currentRegionInstanceId != regionInstanceId) {
                regionInstanceId = currentRegionInstanceId;
                EntityView regionInstance = iter.world().obtainEntityView(currentRegionInstanceId);
                regionInstanceData = regionInstance.getMutView(RegionInstance.class);
            }

            if (resourceGatheringType != null && popTypeId == resourceGatheringType.slavePopTypeId()) {
                population.employment(resourceGathering.slaveAmount());
                continue;
            }

            if(resourceGatheringType != null && popTypeId == resourceGatheringType.workerPopTypeId()) {
                population.employment(resourceGathering.workerAmount());
                continue;
            }

            population.employment((int) (population.amount() * regionInstanceData.workerPopTypeEmploymentRatios(popTypeIndex)));
        }
    }
}
