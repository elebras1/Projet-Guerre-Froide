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
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

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
                float lifeFullCost = 0f;
                for (int j = 0; j < popTypeData.lifeNeedsGoodAmountsLength(); j++) {
                    int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    lifeCost += popTypeData.lifeNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                    lifeFullCost += popTypeData.lifeNeedsGoodAmounts(j) * countryMarket.goodPrices(goodIndex);
                }

                float everydayCost = 0f;
                float everydayFullCost = 0f;
                for (int j = 0; j < popTypeData.everydayNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    everydayCost += popTypeData.everydayNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                    everydayFullCost += popTypeData.everydayNeedsGoodAmounts(j) * countryMarket.goodPrices(goodIndex);
                }

                float luxuryCost = 0f;
                float luxuryFullCost = 0f;
                for (int j = 0; j < popTypeData.luxuryNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    luxuryCost += popTypeData.luxuryNeedsGoodAmounts(j) * this.paidPrice(countryMarket, goodIndex);
                    luxuryFullCost += popTypeData.luxuryNeedsGoodAmounts(j) * countryMarket.goodPrices(goodIndex);
                }

                countryMarket.paidLifeCostsByPopType(ptIndex, lifeCost);
                countryMarket.paidEverydayCostsByPopType(ptIndex, everydayCost);
                countryMarket.paidLuxuryCostsByPopType(ptIndex, luxuryCost);
                countryMarket.servedLifeRatiosByPopType(ptIndex, lifeFullCost > 0f ? lifeCost / lifeFullCost : 1f);
                countryMarket.servedEverydayRatiosByPopType(ptIndex, everydayFullCost > 0f ? everydayCost / everydayFullCost : 1f);
                countryMarket.servedLuxuryRatiosByPopType(ptIndex, luxuryFullCost > 0f ? luxuryCost / luxuryFullCost : 1f);
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
