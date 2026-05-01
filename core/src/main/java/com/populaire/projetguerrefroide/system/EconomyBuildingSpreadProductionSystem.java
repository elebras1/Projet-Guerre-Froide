package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingSpreadProductionSystem {

    public EconomyBuildingSpreadProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingSpreadProductionSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        long economyBuildingTypeId = 0;
        EconomyBuildingTypeView economyBuildingTypeData = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            if(building.typeId() != economyBuildingTypeId) {
                economyBuildingTypeId = building.typeId();
                EntityView economyBuildingType = iter.world().obtainEntityView(economyBuildingTypeId);
                economyBuildingTypeData = economyBuildingType.getMutView(EconomyBuildingType.class);
            }

            countryMarket.goodAmountsPool(economyBuildingTypeData.goodOutputIndex(), countryMarket.goodAmountsPool(economyBuildingTypeData.goodOutputIndex()) + economyBuilding.production());
        }
    }
}
