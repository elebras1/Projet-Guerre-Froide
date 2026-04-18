package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOSizeSystem {

    public RGOSizeSystem(World ecsWorld, long phaseId) {
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

            EntityView resourceGatheringType = iter.world().obtainEntityView(resourceGathering.typeId());
            ResourceGatheringTypeView resourceGatheringTypeData = resourceGatheringType.getMutView(ResourceGatheringType.class);

            int workforce = resourceGatheringTypeData.workforce();
            int workerPopTypeIndex = resourceGatheringTypeData.workerPopTypeIndex();
            int workerAmount = demographics.totalByPopType(workerPopTypeIndex);

            int size = (workerAmount + workforce - 1) / workforce;
            size = (int) (size * 1.5f);

            resourceGathering.size(size);
        }
    }
}
