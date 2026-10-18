package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class PopulationEmploymentSynchronizationSystem {

    private final long rgoType;

    public PopulationEmploymentSynchronizationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.rgoType = ecsConstants.rgoType();
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
        boolean hasResourceGathering = false;
        int rgoWorkerAmount = 0;
        int rgoSlaveAmount = 0;
        long rgoWorkerPopTypeId = 0;
        long rgoSlavePopTypeId = 0;
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
                hasResourceGathering = province.has(ResourceGathering.class);
                if(hasResourceGathering) {
                    ResourceGatheringView resourceGathering = province.getMutView(ResourceGathering.class);
                    rgoWorkerAmount = resourceGathering.workerAmount();
                    rgoSlaveAmount = resourceGathering.slaveAmount();
                    ResourceGatheringTypeView resourceGatheringType = iter.world().obtainEntityView(province.target(this.rgoType, 0)).getView(ResourceGatheringType.class);
                    rgoWorkerPopTypeId = resourceGatheringType.workerPopTypeId();
                    rgoSlavePopTypeId = resourceGatheringType.slavePopTypeId();
                }
            }

            long currentRegionInstanceId = population.regionInstanceId();
            if(currentRegionInstanceId != regionInstanceId) {
                regionInstanceId = currentRegionInstanceId;
                EntityView regionInstance = iter.world().obtainEntityView(currentRegionInstanceId);
                regionInstanceData = regionInstance.getMutView(RegionInstance.class);
            }

            if (hasResourceGathering && popTypeId == rgoSlavePopTypeId) {
                population.employment(rgoSlaveAmount);
                continue;
            }

            if(hasResourceGathering && popTypeId == rgoWorkerPopTypeId) {
                population.employment(rgoWorkerAmount);
                continue;
            }

            population.employment((int) (population.amount() * regionInstanceData.workerPopTypeEmploymentRatios(popTypeIndex)));
        }
    }
}
