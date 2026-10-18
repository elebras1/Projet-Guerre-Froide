package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingProductionSystem {

    public EconomyBuildingProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingProductionSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .with(EconomyBuildingType.class)
            .multiThreaded()
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        long countryId = 0;
        CountryProductionPolicyView countryProductionPolicy = null;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);
        EconomyBuildingTypeView economyBuildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);

        for (int i = 0; i < iter.count(); i++) {
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                countryProductionPolicy = iter.world().obtainEntityView(countryId).getMutView(CountryProductionPolicy.class);
            }

            int level = building.size();
            float effectiveScale = effectiveScale(economyBuilding, economyBuildingTypeData, level);
            float inputScale = inputScale(countryProductionPolicy, level, effectiveScale);

            float stockRatio = 1f;
            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                if (economyBuilding.activeInputGoodIndexes(g) < 0) {
                    break;
                }
                float need = inputScale * economyBuilding.activeInputAmounts(g);
                if (need > 0f) {
                    stockRatio = Math.min(stockRatio, economyBuilding.inputStocks(g) / need);
                }
            }

            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                if (economyBuilding.activeInputGoodIndexes(g) < 0) {
                    break;
                }
                float used = inputScale * economyBuilding.activeInputAmounts(g) * stockRatio;
                economyBuilding.inputStocks(g, Math.max(0f, economyBuilding.inputStocks(g) - used));
            }

            float maxSecondary = maxWorkers(economyBuildingTypeData, level, economyBuildingTypeData.secondaryWorkerPopTypeRatio());
            float secondaryRatio = economyBuilding.secondaryWorkerAmount() / Math.max(1f, maxSecondary);
            float outputScale = outputScale(countryProductionPolicy, level, secondaryRatio, effectiveScale);
            economyBuilding.production(economyBuildingTypeData.goodOutputAmount() * outputScale * stockRatio);
        }
    }
}
