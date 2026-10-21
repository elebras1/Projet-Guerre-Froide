package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.EmploymentUtils;

import static com.populaire.projetguerrefroide.util.EmploymentUtils.activeWorkers;
import static com.populaire.projetguerrefroide.util.EmploymentUtils.activeShare;

public class RGOEmploymentInitializationSystem {

    public RGOEmploymentInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOEmploymentInitializationSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .with(Province.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoTypeData = iter.field(ResourceGatheringType.class, 2).getView(0);
        Field<Province> provinceField = iter.field(Province.class, 3);
        long countryId = 0;
        float workforceShare = 0f;
        int workforce = rgoTypeData.workforce();
        float workerPopTypeRatio = rgoTypeData.workerPopTypeRatio();
        float slavePopTypeRatio = rgoTypeData.slavePopTypeRatio();
        int workerIndex = rgoTypeData.workerPopTypeIndex();
        int slaveIndex = rgoTypeData.slavePopTypeIndex();
        float workerBaseShare = EmploymentUtils.baseWorkforceShares(iter.world())[workerIndex];
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                CountryPopulationPolicyView populationPolicy = iter.world().obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                workforceShare = activeShare(workerBaseShare, populationPolicy.workforceShareModifier());
            }
            int maxCapacity = resourceGathering.size() * workforce;
            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);
            resourceGathering.workerAmount(Math.min(targetWorkers, activeWorkers(demographics.totalByPopType(workerIndex), workforceShare)));
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}
