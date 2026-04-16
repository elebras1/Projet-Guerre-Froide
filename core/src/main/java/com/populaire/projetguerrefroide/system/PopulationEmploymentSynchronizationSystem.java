package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PopulationEmploymentSynchronizationSystem {
    private final World ecsWorld;

    public PopulationEmploymentSynchronizationSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("PopulationEmploymentSynchronizationSystem")
            .kind(phaseId)
            .with(Population.class)
            .orderBy(Population.class, (PopulationView popA, PopulationView popB) -> {
                int compareProvinceId = Long.compare(popA.provinceId(), popB.provinceId());
                if(compareProvinceId != 0) {
                    return compareProvinceId;
                }

                EntityView provinceA = this.ecsWorld.obtainEntityView(popA.provinceId());
                EntityView provinceB = this.ecsWorld.obtainEntityView(popB.provinceId());
                ProvinceView provinceDataA = provinceA.getMutView(Province.class);
                ProvinceView provinceDataB = provinceB.getMutView(Province.class);

                return Long.compare(provinceDataA.ownerId(), provinceDataB.ownerId());
            })
            .iter(this::synchronize);
    }

    private void synchronize(Iter iter) {
        long provinceId = 0;
        GeoHierarchyView geoHierarchy = null;
        ResourceGatheringView resourceGathering = null;
        ResourceGatheringTypeView resourceGatheringType = null;
        long localMarketId = 0;
        LocalMarketView localMarketData = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);

            if(population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView province = this.ecsWorld.obtainEntityView(provinceId);
                geoHierarchy = province.getMutView(GeoHierarchy.class);
                resourceGathering = province.getMutView(ResourceGathering.class);
                if(resourceGathering != null) {
                    resourceGatheringType = this.ecsWorld.obtainEntityView(resourceGathering.typeId()).getMutView(ResourceGatheringType.class);
                } else {
                    resourceGatheringType = null;
                }
            }

            if(geoHierarchy.localMarketId() != localMarketId) {
                EntityView localMarket = this.ecsWorld.obtainEntityView(geoHierarchy.localMarketId());
                localMarketData = localMarket.getMutView(LocalMarket.class);
            }

            if (resourceGatheringType != null && population.typeId() == resourceGatheringType.slavePopTypeId()) {
                population.employment(resourceGathering.slaveAmount());
                continue;
            }

            if(resourceGatheringType != null && population.typeId() == resourceGatheringType.workerPopTypeId()) {
                population.employment(resourceGathering.workerAmount());
                continue;
            }

            population.employment((int) (population.amount() * localMarketData.workerPopTypeEmploymentRatios(population.index())));
        }
    }
}
