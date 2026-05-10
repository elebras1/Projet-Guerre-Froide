package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.*;
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
        CountryEffectPolicyView countryEffectPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryEffectPolicy = country.getMutView(CountryEffectPolicy.class);
            }

            EntityView economyBuildingType = iter.world().obtainEntityView(building.typeId());
            EconomyBuildingTypeView economyBuildingTypeData = economyBuildingType.getMutView(EconomyBuildingType.class);

            float throughput = 1.0f; // TODO : calculer par rapport aux modifier d'agregations des technologies (niveau pays), infrastructures ou batiments specifique (niveau region)

            float inputMultiplier = 1.0f + countryEffectPolicy.factoryInputModifier();

            for (int g = 0; g < economyBuildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = economyBuildingTypeData.goodInputIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float amount = economyBuildingTypeData.goodInputAmounts(g);
                float demand = inputMultiplier * throughput * amount * economyBuilding.scale() * building.size();
                countryMarket.goodDemandAmounts(goodIndex, countryMarket.goodDemandAmounts(goodIndex) + demand);
                economyBuilding.goodInputDemandAmounts(g, demand);
            }
        }
    }
}
