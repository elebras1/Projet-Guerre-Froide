package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingProductionSystem {

    private final long stateOwnerTag;

    public EconomyBuildingProductionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.stateOwnerTag = ecsConstants.countryTag();
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

        long regionId = 0;
        RegionInstanceView regionInstance = null;

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
            if (building.parentId() != regionId) {
                regionId = building.parentId();
                regionInstance = iter.world().obtainEntityView(regionId).getView(RegionInstance.class);
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
            float outputScale = outputScale(countryProductionPolicy, level, secondaryRatio, effectiveScale) * (1f + managementBonus(economyBuilding, regionInstance, this.stateOwnerTag));
            economyBuilding.production(economyBuildingTypeData.goodOutputAmount() * outputScale * stockRatio);
        }
    }
}
