package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class NeedsCostsCalculationSystem {

    public NeedsCostsCalculationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("NeedsCostsCalculationSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .iter(this::calculate);
    }

    private void calculate(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(iter.world().lookup("global_population_type"));
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketView countryMarket = countryMarketField.getMutView(i);

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
                    float price = countryMarket.effectiveGoodPrices(goodIndex);
                    countryMarket.lifeCostsByPopType(ptIndex, countryMarket.lifeCostsByPopType(ptIndex) + amount * price);
                }

                for (int j = 0; j < popTypeData.everydayNeedsGoodIdsLength(); j++) {
                    int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                    if(goodIndex < 0) {
                        break;
                    }
                    float amount = popTypeData.everydayNeedsGoodAmounts(j);
                    float price = countryMarket.effectiveGoodPrices(goodIndex);
                    countryMarket.everydayCostsByPopType(ptIndex, countryMarket.everydayCostsByPopType(ptIndex) + amount * price);
                }

                for (int j = 0; j < popTypeData.luxuryNeedsGoodAmountsLength(); j++) {
                    int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                    if(goodIndex < 0) {
                        break;
                    }
                    float amount = popTypeData.luxuryNeedsGoodAmounts(j);
                    float price = countryMarket.effectiveGoodPrices(goodIndex);
                    countryMarket.luxuryCostsByPopType(ptIndex, countryMarket.luxuryCostsByPopType(ptIndex) + amount * price);
                }
            }
        }
    }
}
