package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingSpreadProductionSystem {

    public EconomyBuildingSpreadProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingSpreadProductionSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        Field<EconomyBuildingType> economyBuildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView economyBuildingTypeData = economyBuildingTypeField.getView(0);
        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            countryMarket.goodProducedAmounts(economyBuildingTypeData.goodOutputIndex(), countryMarket.goodProducedAmounts(economyBuildingTypeData.goodOutputIndex()) + economyBuilding.production());
        }
    }
}
