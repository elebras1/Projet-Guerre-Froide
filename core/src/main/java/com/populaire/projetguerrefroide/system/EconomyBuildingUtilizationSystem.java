package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.FACTORY_UTILIZATION_RATE;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingUtilizationSystem {

    private final long stateOwnerTag;

    public EconomyBuildingUtilizationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.stateOwnerTag = ecsConstants.countryTag();
        ecsWorld.system("EconomyBuildingUtilizationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .multiThreaded()
            .iter(this::adjust);
    }

    private void adjust(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;
        CountryProductionPolicyView countryProductionPolicy = null;

        long regionId = 0;
        RegionInstanceView regionInstance = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView buildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            if (building.parentId() != regionId) {
                regionId = building.parentId();
                regionInstance = iter.world().obtainEntityView(regionId).getView(RegionInstance.class);
            }
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryLaborPolicy = country.getMutView(CountryLaborPolicy.class);
                countryProductionPolicy = country.getMutView(CountryProductionPolicy.class);
            }

            int level = building.size();
            double value = (double) economyBuilding.unitRevenue() * buildingTypeData.goodOutputAmount() * outputScale(countryProductionPolicy, level, 1f, level)
                * (1f + managementBonus(economyBuilding, regionInstance, this.stateOwnerTag));
            double costs = inputCost(economyBuilding, countryMarket, inputScale(countryProductionPolicy, level, level))
                + wageBill(countryMarket, regionInstance, buildingTypeData, countryLaborPolicy.minWageFactor(),
                    maxWorkers(buildingTypeData, level, buildingTypeData.primaryWorkerPopTypeRatio()),
                    maxWorkers(buildingTypeData, level, buildingTypeData.secondaryWorkerPopTypeRatio()));
            float unitMargin = costs > 0.0 ? (float) ((value - costs) / costs) : (value > 0.0 ? 1f : 0f);
            economyBuilding.unitMargin(unitMargin);
            float utilization = economyBuilding.utilization() + FACTORY_UTILIZATION_RATE * Math.clamp(unitMargin, -1f, 1f);
            economyBuilding.utilization(Math.clamp(utilization, 0f, 1f));
        }
    }
}
