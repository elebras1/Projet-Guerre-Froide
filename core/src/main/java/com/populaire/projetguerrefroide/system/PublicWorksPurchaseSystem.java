package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;

public class PublicWorksPurchaseSystem {

    private final EcsConstants ecsConstants;

    public PublicWorksPurchaseSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PublicWorksPurchaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryPublicWorks.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        World world = iter.world();
        GlobalGoodView globalGood = world.obtainEntityView(world.lookup("global_good")).getView(GlobalGood.class);
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryPublicWorks> countryPublicWorksField = iter.field(CountryPublicWorks.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryPublicWorksMutView countryPublicWorks = countryPublicWorksField.getMutView(i);
            countryPublicWorks.spentToday(0.0);
            if (countryPublicWorks.targetBuildingTypeId() == 0) {
                continue;
            }

            DevelopmentBuildingTypeView buildingType = world.obtainEntityView(countryPublicWorks.targetBuildingTypeId()).getView(DevelopmentBuildingType.class);
            double paid = 0.0;
            double deliveredValue = 0.0;
            double levelValue = 0.0;
            for (int k = 0; k < buildingType.goodCostIndexesLength(); k++) {
                int goodIndex = buildingType.goodCostIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                GoodView good = world.obtainEntityView(globalGood.goodIds(goodIndex)).getView(Good.class);
                levelValue += (double) buildingType.goodCostAmounts(k) * good.cost();
                float demanded = countryPublicWorks.demandedAmounts(goodIndex);
                float demand = countryMarket.goodDemandAmounts(goodIndex);
                if (demanded <= 0f || demand <= 0f) {
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(goodIndex) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(goodIndex);
                countryMarket.goodRevenues(goodIndex, countryMarket.goodRevenues(goodIndex) + cost);
                paid += cost;
                deliveredValue += (double) delivered * good.cost();
            }
            countryMarket.treasury(countryMarket.treasury() - paid);
            countryPublicWorks.spentToday(paid);
            if (levelValue <= 0.0) {
                continue;
            }

            float progress = countryPublicWorks.progress() + (float) (deliveredValue / levelValue);
            while (progress >= 1f && countryPublicWorks.targetBuildingId() != 0) {
                progress -= 1f;
                InfrastructureUtils.levelUp(world, this.ecsConstants, countryPublicWorks.targetBuildingId(), countryPublicWorks.targetBuildingTypeId());
                BuildingView building = world.obtainEntityView(countryPublicWorks.targetBuildingId()).getView(Building.class);
                if (building.size() >= buildingType.maxLevel()) {
                    countryPublicWorks.targetBuildingId(0L);
                    countryPublicWorks.targetBuildingTypeId(0L);
                }
            }
            countryPublicWorks.progress(progress);
        }
    }
}
