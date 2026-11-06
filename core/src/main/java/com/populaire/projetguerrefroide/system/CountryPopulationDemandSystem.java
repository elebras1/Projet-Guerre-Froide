package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class CountryPopulationDemandSystem {
    private final EcsConstants ecsConstants;

    public CountryPopulationDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryPopulationDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .multiThreaded()
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        EntityView globalPopType = iter.world().obtainEntityView(this.ecsConstants.globalPopulationType());
        GlobalPopulationTypeView globalPopTypeData = globalPopType.getMutView(GlobalPopulationType.class);
        EntityView globalMarket = iter.world().obtainEntityView(this.ecsConstants.globalMarket());
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);

            for (int ptIndex = 0; ptIndex < globalPopTypeData.popTypeIdsLength(); ptIndex++) {
                EntityView popType = iter.world().obtainEntityView(globalPopTypeData.popTypeIds(ptIndex));
                PopulationTypeView popTypeData = popType.getMutView(PopulationType.class);

                double lifeUnits = countryMarket.lifeDemandUnitsByPopType(ptIndex);
                for (int j = 0; j < popTypeData.lifeNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.lifeNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    this.addDemand(countryMarket, goodIndex, lifeUnits * popTypeData.lifeNeedsGoodAmounts(j));
                }

                double everydayUnits = countryMarket.everydayDemandUnitsByPopType(ptIndex);
                for (int j = 0; j < popTypeData.everydayNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.everydayNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    this.addDemand(countryMarket, goodIndex, everydayUnits * popTypeData.everydayNeedsGoodAmounts(j));
                }

                double luxuryUnits = countryMarket.luxuryDemandUnitsByPopType(ptIndex);
                for (int j = 0; j < popTypeData.luxuryNeedsGoodIndexesLength(); j++) {
                    int goodIndex = popTypeData.luxuryNeedsGoodIndexes(j);
                    if (goodIndex < 0) {
                        break;
                    }
                    if (!globalMarketData.goodAvailabilities(goodIndex)) {
                        continue;
                    }
                    this.addDemand(countryMarket, goodIndex, luxuryUnits * popTypeData.luxuryNeedsGoodAmounts(j));
                }
            }
        }
    }

    private void addDemand(CountryMarketMutView countryMarket, int goodIndex, double demand) {
        countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + (float) demand);
        countryMarket.goodPopulationDemandAmounts(goodIndex, countryMarket.goodPopulationDemandAmounts(goodIndex) + demand);
    }
}
