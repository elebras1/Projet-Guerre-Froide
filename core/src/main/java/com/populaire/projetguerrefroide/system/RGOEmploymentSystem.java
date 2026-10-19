package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class RGOEmploymentSystem {

    public RGOEmploymentSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOEmploymentSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .multiThreaded()
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        if (iter.count() == 0) {
            return;
        }
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        ResourceGatheringTypeView rgoTypeData = iter.field(ResourceGatheringType.class, 2).getView(0);
        int workforce = rgoTypeData.workforce();
        float workerPopTypeRatio = rgoTypeData.workerPopTypeRatio();
        float slavePopTypeRatio = rgoTypeData.slavePopTypeRatio();
        int workerIndex = rgoTypeData.workerPopTypeIndex();
        int slaveIndex = rgoTypeData.slavePopTypeIndex();

        ResourceGatheringView firstResourceGathering = resourceGatheringField.getMutView(0);
        int goodIndex = firstResourceGathering.goodIndex();
        GoodView good = iter.world().obtainEntityView(firstResourceGathering.goodId()).getView(Good.class);
        float goodCost = good.cost();
        GlobalMarketView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market")).getView(GlobalMarket.class);
        float worldPrice = globalMarket.goodPrices(goodIndex);
        float worldProduction = globalMarket.goodProductionAmounts(goodIndex);
        float worldDemand = globalMarket.goodDemandAmounts(goodIndex);
        float inventory = globalMarket.goodStockAmounts(goodIndex) - worldProduction;
        float clearingProduction = Math.max(0f, worldDemand - (inventory - STOCK_TARGET_DAYS * worldDemand) / RGO_STOCK_ADJUSTMENT_DAYS);
        float clearingGap = worldProduction > 0f ? clearingProduction / worldProduction - 1f : (clearingProduction > 0f ? 1f : 0f);
        float adjustment = RGO_CLEARING_GAIN * clearingGap + RGO_PRICE_GAIN * (float) Math.log(worldPrice / goodCost);
        adjustment = Math.clamp(adjustment, -RGO_EMPLOYMENT_ADJUSTMENT_RATE, RGO_EMPLOYMENT_ADJUSTMENT_RATE);

        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            int maxCapacity = resourceGathering.size() * workforce;
            int targetWorkers = (int) (maxCapacity * workerPopTypeRatio);
            int targetSlaves = (int) (maxCapacity * slavePopTypeRatio);
            int maximumWorkers = Math.min(targetWorkers, demographics.totalByPopType(workerIndex));

            int workerAmount = resourceGathering.workerAmount() + Math.round(adjustment * maximumWorkers);
            resourceGathering.vacancies(adjustment > 0f ? Math.max(0, targetWorkers - demographics.totalByPopType(workerIndex)) : 0);
            resourceGathering.applicants(0f);
            resourceGathering.workerAmount(Math.max(0, Math.min(maximumWorkers, workerAmount)));
            resourceGathering.slaveAmount(Math.min(targetSlaves, demographics.totalByPopType(slaveIndex)));
        }
    }
}
