package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingConsumptionSystem {

    public EconomyBuildingConsumptionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingConsumptionSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::consume);
    }

    private void consume(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryProductionPolicyView countryProductionPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

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

            // Align input demand with the effective production scale used by
            // EconomyBuildingProductionSystem (capped by available primary workers).
            float level = building.size();
            float workforce = economyBuildingTypeData.workforce();
            float primaryRatioType = economyBuildingTypeData.primaryWorkerPopTypeRatio();
            float maxPrimary = level * workforce * primaryRatioType;
            float primaryRatio = economyBuilding.primaryWorkerAmount() / Math.max(1f, maxPrimary);
            float maxProductionScale = primaryRatio * level;
            float effectiveScale = Math.min(economyBuilding.scale() * level, maxProductionScale);

            for (int g = 0; g < economyBuildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = economyBuildingTypeData.goodInputIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float amount = economyBuildingTypeData.goodInputAmounts(g);
                float demand = inputMultiplier * throughput * amount * effectiveScale;
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                economyBuilding.goodInputDemandAmounts(g, demand);
            }
        }
    }
}
