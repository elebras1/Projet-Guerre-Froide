package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

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
        Field<EconomyBuildingType> economyBuildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView economyBuildingTypeData = economyBuildingTypeField.getView(0);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryProductionPolicy = country.getMutView(CountryProductionPolicy.class);
            }

            float throughput = 1f + Math.min(countryProductionPolicy.maximumEconomyScaleFactor(), building.size() * 0.01f);

            float inputMultiplier = 1.0f + countryProductionPolicy.factoryInputModifier();

            float level = building.size();
            float workforce = economyBuildingTypeData.workforce();
            float primaryRatioType = economyBuildingTypeData.primaryWorkerPopTypeRatio();
            float maxPrimary = level * workforce * primaryRatioType;
            float primaryRatio = economyBuilding.primaryWorkerAmount() / Math.max(1f, maxPrimary);
            float maxProductionScale = primaryRatio * level;
            float effectiveScale = Math.min(economyBuilding.scale() * level, maxProductionScale);

            float inputScale = inputMultiplier * throughput * effectiveScale;
            double budgetCost = 0.0;
            for (int g = 0; g < economyBuildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                budgetCost += (double) inputScale * economyBuilding.activeInputAmounts(g) * countryMarket.goodBudgetPrices(goodIndex);
            }
            double available = Math.max(0.0, economyBuilding.treasury());
            float inputBudgetRatio = budgetCost > available ? (float) (available / budgetCost) : 1f;
            economyBuilding.inputBudgetRatio(inputBudgetRatio);

            for (int g = 0; g < economyBuildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float demand = inputScale * economyBuilding.activeInputAmounts(g) * inputBudgetRatio;
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                economyBuilding.goodInputDemandAmounts(g, demand);
            }
        }
    }
}
