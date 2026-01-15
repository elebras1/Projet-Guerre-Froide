package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.Entity;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationSizeSystem {
    private final World ecsWorld;

    public ResourceGatheringOperationSizeSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("RGOSizeSystem").kind(FlecsConstants.EcsOnStart).with(Province.class).with(ResourceGathering.class).with(PopulationDistribution.class).multiThreaded().iter(this::process);
    }

    private void process(Iter iter) {
        for (int i = 0; i < iter.count(); i++) {
            long provinceEntityId = iter.entity(i);
            Entity provinceEntity = this.ecsWorld.obtainEntity(provinceEntityId);

            long resourceGoodId = iter.fieldLong(ResourceGathering.class, 1, "goodId", i);

            Entity resourceGoodEntity = this.ecsWorld.obtainEntity(resourceGoodId);
            ResourceProduction resourceProduction = resourceGoodEntity.get(ResourceProduction.class);
            if (resourceProduction == null) {
                continue;
            }

            Entity productionTypeEntity = this.ecsWorld.obtainEntity(resourceProduction.productionTypeId());
            ProductionType productionTypeData = productionTypeEntity.get(ProductionType.class);
            if (productionTypeData == null || productionTypeData.employeeTypes()[0] == 0) {
                continue;
            }

            long firstEmployeeEntityId = productionTypeData.employeeTypes()[0];
            Entity employeeEntity = this.ecsWorld.obtainEntity(firstEmployeeEntityId);
            EmployeeType employeeTypeData = employeeEntity.get(EmployeeType.class);
            long workerPopulationTypeId = employeeTypeData.populationTypeId();

            PopulationDistribution popDistribution = provinceEntity.get(PopulationDistribution.class);
            long[] populationIds = popDistribution.populationIds();
            int[] populationAmounts = popDistribution.populationAmounts();

            int workerInProvince = 0;
            for (int j = 0; j < populationIds.length; j++) {
                if (populationIds[j] == workerPopulationTypeId) {
                    workerInProvince = populationAmounts[j];
                    break;
                }
            }

            int size = (workerInProvince + productionTypeData.workforce() - 1) / productionTypeData.workforce();
            size = (int) (size * 1.5f);
            iter.setFieldInt(ResourceGathering.class, 1, "size", i, size);
        }
    }
}
