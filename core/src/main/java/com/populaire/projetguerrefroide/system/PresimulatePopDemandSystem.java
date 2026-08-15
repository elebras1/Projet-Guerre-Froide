package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class PresimulatePopDemandSystem {

    private static final float LIFE_NEEDS_WEIGHT = 0.9f;
    private static final float EVERYDAY_NEEDS_WEIGHT = 0.3f;
    private static final float LUXURY_NEEDS_WEIGHT = 0.005f;

    public PresimulatePopDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PresimulatePopDemandSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        long populationTypeId = 0;
        PopulationTypeView populationTypeData = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);

            if(population.typeId() != populationTypeId) {
                populationTypeId = population.typeId();
                populationTypeData = iter.world().obtainEntityView(populationTypeId).getMutView(PopulationType.class);
            }

            float scaledAmount = population.amount() / NEEDS_SCALING_FACTOR;

            for(int g = 0; g < populationTypeData.lifeNeedsGoodAmountsLength(); g++) {
                int goodIndex = populationTypeData.lifeNeedsGoodIndexes(g);
                if(goodIndex < 0) {
                    break;
                }
                float demand = populationTypeData.lifeNeedsGoodAmounts(g) * scaledAmount * LIFE_NEEDS_WEIGHT;
                globalMarketData.goodDemandAmounts(goodIndex, globalMarketData.goodDemandAmounts(goodIndex) + demand);
            }

            for(int g = 0; g < populationTypeData.everydayNeedsGoodAmountsLength(); g++) {
                int goodIndex = populationTypeData.everydayNeedsGoodIndexes(g);
                if(goodIndex < 0) {
                    break;
                }
                float demand = populationTypeData.everydayNeedsGoodAmounts(g) * scaledAmount * EVERYDAY_NEEDS_WEIGHT;
                globalMarketData.goodDemandAmounts(goodIndex, globalMarketData.goodDemandAmounts(goodIndex) + demand);
            }

            for(int g = 0; g < populationTypeData.luxuryNeedsGoodAmountsLength(); g++) {
                int goodIndex = populationTypeData.luxuryNeedsGoodIndexes(g);
                if(goodIndex < 0) {
                    break;
                }
                float demand = populationTypeData.luxuryNeedsGoodAmounts(g) * scaledAmount * LUXURY_NEEDS_WEIGHT;
                globalMarketData.goodDemandAmounts(goodIndex, globalMarketData.goodDemandAmounts(goodIndex) + demand);
            }
        }
    }
}
