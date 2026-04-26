package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

public class PopulationConsumptionSystem {

    public PopulationConsumptionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationConsumptionSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        Field<Population> popField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationView pop = popField.getMutView(i);

            if(countryId != pop.countryId()) {
                countryId = pop.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            int popTypeIndex = (int) pop.typeId();

            float lifeCost = countryMarket.lifeCostsByPopType(popTypeIndex);
            float everydayCost = countryMarket.everydayCostsByPopType(popTypeIndex);
            float luxuryCost = countryMarket.luxuryCostsByPopType(popTypeIndex);

            float budget = pop.savings();
            float amount = pop.amount();

            float neededLife = lifeCost * amount;
            float lifeFraction = Math.min(1f, budget / Math.max(0.001f, neededLife));
            budget -= neededLife * lifeFraction;
            budget = Math.max(0f, budget);

            float neededEveryday = everydayCost * amount;
            float everydayFraction = Math.min(1f, budget / Math.max(0.001f, neededEveryday));
            budget -= neededEveryday * everydayFraction;
            budget = Math.max(0f, budget);

            float neededLuxury = luxuryCost * amount;
            float luxuryFraction = Math.min(1f, budget / Math.max(0.001f, neededLuxury));

            EntityView popType = iter.world().obtainEntityView(pop.typeId());
            PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);

            for (int j = 0; j < popTypeData.lifeNeedsGoodAmountsLength(); j++) {
                int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.lifeNeedsGoodAmounts(j);
                float demand = base * lifeFraction * amount;
                countryMarket.realDemand(goodIndex, countryMarket.realDemand(goodIndex) + demand);
            }

            for (int j = 0; j < popTypeData.everydayNeedsGoodIndexesLength(); j++) {
                int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.everydayNeedsGoodAmounts(j);
                float demand = base * everydayFraction * amount;
                countryMarket.realDemand(goodIndex, countryMarket.realDemand(goodIndex) + demand);
            }

            for (int j = 0; j < popTypeData.luxuryNeedsGoodIndexesLength(); j++) {
                int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                if (goodIndex < 0) {
                    break;
                }
                float base = popTypeData.luxuryNeedsGoodAmounts(j);
                float demand = base * luxuryFraction * amount;
                countryMarket.realDemand(goodIndex, countryMarket.realDemand(goodIndex) + demand);
            }
        }
    }
}
