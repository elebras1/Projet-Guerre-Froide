package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingTreasuryInitializationSystem {

    public EconomyBuildingTreasuryInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingTreasuryInitializationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryProductionPolicyView countryProductionPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryProductionPolicy = country.getMutView(CountryProductionPolicy.class);
            }

            EntityView economyBuildingType = iter.world().obtainEntityView(building.typeId());
            EconomyBuildingTypeView economyBuildingTypeData = economyBuildingType.getMutView(EconomyBuildingType.class);

            float throughput = 1f + Math.min(countryProductionPolicy.maximumEconomyScaleFactor(), building.size() * 0.01f);
            float inputMultiplier = 1.0f + countryProductionPolicy.factoryInputModifier();

            float level = building.size();
            float maxPrimary = level * economyBuildingTypeData.workforce() * economyBuildingTypeData.primaryWorkerPopTypeRatio();
            float primaryRatio = economyBuilding.primaryWorkerAmount() / Math.max(1f, maxPrimary);
            float effectiveScale = Math.min(economyBuilding.scale() * level, primaryRatio * level);

            float inputScale = inputMultiplier * throughput * effectiveScale;
            double budgetCost = 0.0;
            for (int g = 0; g < economyBuildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                budgetCost += (double) inputScale * economyBuilding.activeInputAmounts(g) * countryMarket.goodBudgetPrices(goodIndex);
            }
            economyBuilding.treasury(budgetCost);
        }
    }
}
