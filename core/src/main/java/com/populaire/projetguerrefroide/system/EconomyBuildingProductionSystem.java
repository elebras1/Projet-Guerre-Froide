package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingProductionSystem {

    public EconomyBuildingProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingProductionSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        long economyBuildingTypeId = 0;
        EconomyBuildingTypeView economyBuildingTypeData = null;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);

        for (int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            if(building.typeId() != economyBuildingTypeId) {
                economyBuildingTypeId = building.typeId();
                EntityView economyBuildingType = iter.world().obtainEntityView(economyBuildingTypeId);
                economyBuildingTypeData = economyBuildingType.getMutView(EconomyBuildingType.class);
            }

            float primaryWorkers = economyBuilding.primaryWorkerAmount();
            float secondaryWorkers = economyBuilding.secondaryWorkerAmount();
            float level = building.size();
            float workforce = economyBuildingTypeData.workforce();
            float primaryRatioType = economyBuildingTypeData.primaryWorkerPopTypeRatio();
            float secondaryRatioType = economyBuildingTypeData.secondaryWorkerPopTypeRatio();

            float maxPrimary = level * workforce * primaryRatioType;
            float maxSecondary = level * workforce * secondaryRatioType;

            float primaryRatio = primaryWorkers / Math.max(1f, maxPrimary);
            float secondaryRatio = secondaryWorkers / Math.max(1f, maxSecondary);

            float maxProductionScale = primaryRatio * level;

            float scale = economyBuilding.scale();
            float effectiveScale = Math.min(scale * level, maxProductionScale);
            float baseOutput = economyBuildingTypeData.goodOutputAmount();
            float throughput = 1f;

            float secondaryEffectMultiplier = 1.5f;
            float outputMultiplier = 1f + secondaryRatio * secondaryEffectMultiplier;

            float minInputSatisfaction = 1f;
            for (int g = 0; g < economyBuildingTypeData.goodInputIndexesLength(); g++) {
                int goodIndex = economyBuildingTypeData.goodInputIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float satisfaction = countryMarket.demandSatisfaction(goodIndex);
                minInputSatisfaction = Math.min(minInputSatisfaction, satisfaction);
            }

            float efficiencySatisfaction = 1f;

            float production = baseOutput * throughput * outputMultiplier * (0.75f + 0.25f * efficiencySatisfaction) * minInputSatisfaction * effectiveScale;

            economyBuilding.production(production);
        }
    }
}
