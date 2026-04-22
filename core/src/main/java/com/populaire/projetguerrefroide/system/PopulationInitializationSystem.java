package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PopulationInitializationSystem {

    public PopulationInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationInitializationSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::initialize);
    }

    public void initialize(Iter iter) {
        long populationTypeId = 0;
        PopulationTypeView populationTypeData;
        float baseSavings = 0;
        float strataMultiplier = 1f;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);

            if(population.typeId() != populationTypeId) {
                populationTypeId = population.typeId();
                populationTypeData = iter.world().obtainEntityView(populationTypeId).getMutView(PopulationType.class);
                baseSavings = this.calculateBaseSavings(iter.world(), populationTypeData);
                strataMultiplier = (populationTypeData.strata() * 2) + 1;
            }

            population.savings(baseSavings * strataMultiplier * population.amount());
            population.lifeNeedsSatisfaction(1f);
            population.everydayNeedsSatisfaction(0.5f);
            population.luxuryNeedsSatisfaction(0f);
        }

    }

    private float calculateBaseSavings(World world, PopulationTypeView populationType) {
        float base = 0f;
        for(int g = 0; g < populationType.lifeNeedsGoodAmountsLength(); g++) {
            long goodId = populationType.lifeNeedsGoodIds(g);
            float needAmount = populationType.lifeNeedsGoodAmounts(g);
            if(goodId > 0 && needAmount > 0f) {
                base += this.getGoodCost(world, goodId) * needAmount;
            }
        }
        for(int g = 0; g < populationType.everydayNeedsGoodIdsLength(); g++) {
            long goodId = populationType.everydayNeedsGoodIds(g);
            float needAmount = populationType.everydayNeedsGoodAmounts(g);
            if(goodId > 0 && needAmount > 0f) {
                base += this.getGoodCost(world, goodId) * needAmount * 0.5f;
            }
        }
        return base;
    }

    private float getGoodCost(World world, long goodId) {
        EntityView good = world.obtainEntityView(goodId);
        GoodView goodData = good.getMutView(Good.class);
        return goodData.cost();
    }
}
