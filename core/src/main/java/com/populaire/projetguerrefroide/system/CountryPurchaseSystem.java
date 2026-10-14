package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryPurchaseSystem {

    public CountryPurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryPurchaseSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int g = 0; g < countryMarket.goodRevenuesLength(); g++) {
                float paidPrice = this.paidPrice(countryMarket, g);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + (double) paidPrice * countryMarket.goodPopulationDemandAmounts(g));
            }

            for (int ptIndex = 0; ptIndex < globalPopTypeData.popTypeIdsLength(); ptIndex++) {
                EntityView popType = iter.world().obtainEntityView(globalPopTypeData.popTypeIds(ptIndex));
                PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);

                float lifeCost = 0f;
                for (int j = 0; j < popTypeData.lifeNeedsGoodAmountsLength(); j++) {
                    int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    lifeCost += popTypeData.lifeNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                }

                float everydayCost = 0f;
                for (int j = 0; j < popTypeData.everydayNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    everydayCost += popTypeData.everydayNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                }

                float luxuryCost = 0f;
                for (int j = 0; j < popTypeData.luxuryNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    luxuryCost += popTypeData.luxuryNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                }

                countryMarket.paidLifeCostsByPopType(ptIndex, lifeCost);
                countryMarket.paidEverydayCostsByPopType(ptIndex, everydayCost);
                countryMarket.paidLuxuryCostsByPopType(ptIndex, luxuryCost);
            }
        }
    }

    private float paidPrice(CountryMarketMutView countryMarket, int goodIndex) {
        float demand = countryMarket.goodDemandAmounts(goodIndex);
        if (demand <= 0f) {
            return 0f;
        }
        return countryMarket.goodPrices(goodIndex) * countryMarket.goodConsumedAmounts(goodIndex) / demand;
    }
}
