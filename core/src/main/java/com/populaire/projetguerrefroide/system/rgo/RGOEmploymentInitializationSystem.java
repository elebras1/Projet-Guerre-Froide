package com.populaire.projetguerrefroide.system.rgo;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeWorkers;
import static com.populaire.projetguerrefroide.system.population.EmploymentUtils.activeShare;

public class RGOEmploymentInitializationSystem {
    private final EcsConstants ecsConstants;

    public RGOEmploymentInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
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
        float workerBaseShare = this.ecsConstants.baseWorkforceShares()[workerIndex];
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                CountryPopulationPolicyView populationPolicy = iter.stage().obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                workforceShare = activeShare(workerBaseShare, populationPolicy.workforceShareModifier());
            }
            float capacity = resourceGathering.endowment() * workforce;
            int targetWorkers = (int) (capacity * workerPopTypeRatio);
            int targetSlaves = (int) (capacity * slavePopTypeRatio);
            int active = activeWorkers(demographics.totalByPopType(workerIndex), workforceShare);
            // endowment RGOs (decision 185 a): no hiring cap, diminishing returns limit employment
            resourceGathering.workerAmount(resourceGathering.endowmentShare() > 0f ? active : Math.min(targetWorkers, active));
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}
