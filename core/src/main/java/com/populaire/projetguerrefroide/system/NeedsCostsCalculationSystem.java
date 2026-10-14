package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class NeedsCostsCalculationSystem {

    public NeedsCostsCalculationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("NeedsCostsCalculationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::calculate);
    }

    private void calculate(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int ptIndex = 0; ptIndex < globalPopTypeData.popTypeIdsLength(); ptIndex++) {
                long typeId = globalPopTypeData.popTypeIds(ptIndex);

                EntityView popType = iter.world().obtainEntityView(typeId);
                PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);

                for (int j = 0; j < popTypeData.lifeNeedsGoodAmountsLength(); j++) {
                    int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                    if(goodIndex < 0) {
                        break;
                    }
                    float amount = popTypeData.lifeNeedsGoodAmounts(j);
                    countryMarket.lifeCostsByPopType(ptIndex, countryMarket.lifeCostsByPopType(ptIndex) + amount * countryMarket.goodPrices(goodIndex));
                    countryMarket.lifeBudgetCostsByPopType(ptIndex, countryMarket.lifeBudgetCostsByPopType(ptIndex) + amount * countryMarket.goodBudgetPrices(goodIndex));
                }

                for (int j = 0; j < popTypeData.everydayNeedsGoodIdsLength(); j++) {
                    int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                    if(goodIndex < 0) {
                        break;
                    }
                    float amount = popTypeData.everydayNeedsGoodAmounts(j);
                    countryMarket.everydayCostsByPopType(ptIndex, countryMarket.everydayCostsByPopType(ptIndex) + amount * countryMarket.goodPrices(goodIndex));
                    countryMarket.everydayBudgetCostsByPopType(ptIndex, countryMarket.everydayBudgetCostsByPopType(ptIndex) + amount * countryMarket.goodBudgetPrices(goodIndex));
                }

                for (int j = 0; j < popTypeData.luxuryNeedsGoodAmountsLength(); j++) {
                    int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                    if(goodIndex < 0) {
                        break;
                    }
                    float amount = popTypeData.luxuryNeedsGoodAmounts(j);
                    countryMarket.luxuryCostsByPopType(ptIndex, countryMarket.luxuryCostsByPopType(ptIndex) + amount * countryMarket.goodPrices(goodIndex));
                    countryMarket.luxuryBudgetCostsByPopType(ptIndex, countryMarket.luxuryBudgetCostsByPopType(ptIndex) + amount * countryMarket.goodBudgetPrices(goodIndex));
                }
            }
        }
    }
}
