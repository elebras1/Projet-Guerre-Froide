package com.populaire.projetguerrefroide.system.economy;

import com.github.elebras1.flecs.*;
import com.github.elebras1.flecs.util.FlecsConstants;
import com.populaire.projetguerrefroide.component.*;

public class ResourceGatheringOperationProduceSystem {
    private final World ecsWorld;

    public ResourceGatheringOperationProduceSystem(World ecsWorld) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("RGOProduceSystem").kind(FlecsConstants.EcsOnUpdate).with(Province.class).with(ResourceGathering.class).with(GeoHierarchy.class).multiThreaded().iter(this::process);
    }

    public void process(Iter iter) {
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        Field<GeoHierarchy> geoHierarchyField = iter.field(GeoHierarchy.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringView resourceGatheringView = resourceGatheringField.getMutView(i);
            GeoHierarchyView geoHierarchyView = geoHierarchyField.getMutView(i);

            long resourceGoodId = resourceGatheringView.goodId();
            int resourceGoodSize = resourceGatheringView.size();

            EntityView resourceGoodView = this.ecsWorld.obtainEntityView(resourceGoodId);
            GoodView good = resourceGoodView.getMutView(Good.class);

            float baseProduction = resourceGoodSize * good.value();

            int totalWorkers = 0;
            for (int j = 0; j < resourceGatheringView.hiredWorkersLength(); j++) {
                int hiredWorker = resourceGatheringView.hiredWorkers(j);
                totalWorkers += hiredWorker;
            }

            EntityView buildingView = this.ecsWorld.obtainEntityView(resourceGoodId);
            ResourceProductionView resourceProductionView = buildingView.getMutView(ResourceProduction.class);
            EntityView productionTypeView = this.ecsWorld.obtainEntityView(resourceProductionView.productionTypeId());
            ProductionTypeView productionTypeDataView = productionTypeView.getMutView(ProductionType.class);

            int maxWorkers = resourceGoodSize * productionTypeDataView.workforce();
            float throughput = maxWorkers > 0 ? (float) totalWorkers / maxWorkers : 0f;
            float production = baseProduction * throughput;

            resourceGatheringView.production(production);

            EntityView localMarketView = this.ecsWorld.obtainEntityView(geoHierarchyView.localMarketId());
            MarketProductionView marketProductionView = localMarketView.getMutView(MarketProduction.class);
            for(int j = 0; j < marketProductionView.goodIdsLength(); j++) {
                if (marketProductionView.goodIds(j) == resourceGoodId || marketProductionView.goodIds(j) == 0) {
                    marketProductionView.goodIds(j, resourceGoodId);
                    marketProductionView.goodAmounts(j, marketProductionView.goodAmounts(j) + production);
                    break;
                }
            }
        }
    }
}
