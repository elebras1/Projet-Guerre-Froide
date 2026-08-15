package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class PresimulateFactoryDemandSystem {

    public PresimulateFactoryDemandSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PresimulateFactoryDemandSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .iter(this::accumulate);
    }

    private void accumulate(Iter iter) {
        EntityView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market"));
        GlobalMarketView globalMarketData = globalMarket.getMutView(GlobalMarket.class);

        long buildingTypeId = 0;
        EconomyBuildingTypeView buildingTypeData = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                buildingTypeData = iter.world().obtainEntityView(buildingTypeId).getMutView(EconomyBuildingType.class);
            }

            float scale = economyBuilding.scale() * (float) building.size();

            for(int g = 0; g < buildingTypeData.goodInputIdsLength(); g++) {
                int goodIndex = buildingTypeData.goodInputIndexes(g);
                if(goodIndex < 0) {
                    break;
                }
                float demand = buildingTypeData.goodInputAmounts(g) * scale;
                globalMarketData.goodDemandAmounts(goodIndex, globalMarketData.goodDemandAmounts(goodIndex) + demand);
            }
        }
    }
}
