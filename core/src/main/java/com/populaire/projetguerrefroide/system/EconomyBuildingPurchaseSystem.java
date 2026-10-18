package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingPurchaseSystem {

    public EconomyBuildingPurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingPurchaseSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            double paid = 0.0;
            for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
                int goodIndex = economyBuilding.activeInputGoodIndexes(g);
                if (goodIndex < 0) {
                    break;
                }
                float demand = countryMarket.goodDemandAmounts(goodIndex);
                if (demand <= 0f) {
                    continue;
                }
                float delivered = economyBuilding.goodInputDemandAmounts(g) * countryMarket.goodConsumedAmounts(goodIndex) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(goodIndex);
                economyBuilding.inputStocks(g, economyBuilding.inputStocks(g) + delivered);
                countryMarket.goodRevenues(goodIndex, countryMarket.goodRevenues(goodIndex) + cost);
                paid += cost;
            }
            economyBuilding.treasury(economyBuilding.treasury() - paid);
        }
    }
}
