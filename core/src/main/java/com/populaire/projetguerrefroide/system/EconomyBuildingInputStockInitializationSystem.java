package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.INPUT_STOCK_DAYS;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingInputStockInitializationSystem {

    public EconomyBuildingInputStockInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingInputStockInitializationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long countryId = 0;
        CountryProductionPolicyView countryProductionPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView buildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                countryProductionPolicy = iter.world().obtainEntityView(countryId).getMutView(CountryProductionPolicy.class);
            }

            int level = building.size();
            float inputScale = inputScale(countryProductionPolicy, level, effectiveScale(economyBuilding, buildingTypeData, level));
            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                if (economyBuilding.activeInputGoodIndexes(g) < 0) {
                    break;
                }
                economyBuilding.inputStocks(g, INPUT_STOCK_DAYS * inputScale * economyBuilding.activeInputAmounts(g));
            }
        }
    }
}
