package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOSizeSystem {
    private final World ecsWorld;

    public RGOSizeSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("RGOSizeSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .iter(this::size);
    }

    private void size(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            EntityView resourceGatheringType = this.ecsWorld.obtainEntityView(resourceGathering.rgoTypeId());
            ResourceGatheringTypeView resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);

            int workforce = resourceGatheringTypeData.workforce();
            int freeWorkerIndex = resourceGatheringTypeData.workerPopTypeIndexes(0);
            int freeWorkerAmounts = demographics.totalByPopType(freeWorkerIndex);

            int size = (freeWorkerAmounts + workforce - 1) / workforce;
            size = (int) (size * 1.5f);

            resourceGathering.size(size);
        }
    }
}
