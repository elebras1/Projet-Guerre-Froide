package com.populaire.projetguerrefroide.system;

import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOSizeSystem {

    public RGOSizeSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOSizeSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .multiThreaded()
            .iter(this::size);
    }

    private void size(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView resourceGatheringTypeData = iter.field(ResourceGatheringType.class, 2).getView(0);
        int workforce = resourceGatheringTypeData.workforce();
        int workerPopTypeIndex = resourceGatheringTypeData.workerPopTypeIndex();
        for(int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            int workerAmount = demographics.totalByPopType(workerPopTypeIndex);

            int size = (workerAmount + workforce - 1) / Math.max(1, workforce);
            size = (int) (size * 1.5f);

            resourceGathering.size(size);
        }
    }
}
