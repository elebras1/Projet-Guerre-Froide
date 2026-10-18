package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.INPUT_STOCK_DAYS;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingConsumptionSystem {

    public EconomyBuildingConsumptionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingConsumptionSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryProductionPolicyView countryProductionPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView economyBuildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryProductionPolicy = country.getMutView(CountryProductionPolicy.class);
            }

            int level = building.size();
            float inputScale = inputScale(countryProductionPolicy, level, effectiveScale(economyBuilding, economyBuildingTypeData, level));
            double budgetCost = 0.0;
            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float need = inputScale * economyBuilding.activeInputAmounts(g);
                float purchase = Math.max(0f, need + (INPUT_STOCK_DAYS * need - economyBuilding.inputStocks(g)) / INPUT_STOCK_DAYS);
                economyBuilding.goodInputDemandAmounts(g, purchase);
                budgetCost += (double) purchase * countryMarket.goodBudgetPrices(goodIndex);
            }
            double available = Math.max(0.0, economyBuilding.treasury());
            float budgetRatio = budgetCost > available ? (float) (available / budgetCost) : 1f;

            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float demand = economyBuilding.goodInputDemandAmounts(g) * budgetRatio;
                economyBuilding.goodInputDemandAmounts(g, demand);
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
            }
        }
    }
}
