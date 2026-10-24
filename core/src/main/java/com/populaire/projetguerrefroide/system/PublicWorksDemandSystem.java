package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PublicWorksDemandSystem {

    public PublicWorksDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PublicWorksDemandSystem")
            .kind(phaseId)
            .with(CountryMarket.class)
            .with(CountryBudget.class)
            .with(CountryPublicWorks.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 0);
        Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
        Field<CountryPublicWorks> countryPublicWorksField = iter.field(CountryPublicWorks.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
            CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
            CountryPublicWorksMutView countryPublicWorks = countryPublicWorksField.getMutView(i);

            for (int g = 0; g < countryPublicWorks.demandedAmountsLength(); g++) {
                countryPublicWorks.demandedAmounts(g, 0f);
            }
            if (countryPublicWorks.targetBuildingTypeId() == 0) {
                continue;
            }
            double budget = Math.min(countryBudget.publicWorksBudget(), Math.max(0.0, countryMarket.treasury() - countryBudget.reservedNeeds()) + countryBudget.publicWorksLoan());
            if (budget <= 0.0) {
                continue;
            }
            DevelopmentBuildingTypeView buildingType = iter.world().obtainEntityView(countryPublicWorks.targetBuildingTypeId()).getView(DevelopmentBuildingType.class);
            double bundleCost = 0.0;
            for (int k = 0; k < buildingType.goodCostIndexesLength(); k++) {
                int goodIndex = buildingType.goodCostIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                bundleCost += (double) buildingType.goodCostAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
            }
            if (bundleCost <= 0.0) {
                continue;
            }
            float bundles = (float) (budget / bundleCost);
            for (int k = 0; k < buildingType.goodCostIndexesLength(); k++) {
                int goodIndex = buildingType.goodCostIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                float amount = buildingType.goodCostAmounts(k) * bundles;
                countryPublicWorks.demandedAmounts(goodIndex, countryPublicWorks.demandedAmounts(goodIndex) + amount);
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + amount);
            }
        }
    }
}
