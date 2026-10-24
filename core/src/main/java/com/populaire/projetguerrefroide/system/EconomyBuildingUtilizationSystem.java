package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.MarketUtils;

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
        CountryBudgetView countryBudget = null;

        long regionId = 0;
        RegionInstanceView regionInstance = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView buildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);
        GlobalMarketView globalMarket = iter.world().obtainEntityView(iter.world().lookup("global_market")).getView(GlobalMarket.class);
        float demandGap = Math.clamp(MarketUtils.clearingGap(globalMarket, buildingTypeData.goodOutputIndex()), -1f, 1f);

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
                countryBudget = country.getView(CountryBudget.class);
            }

            int level = building.size();
            double value = (double) economyBuilding.unitRevenue() * buildingTypeData.goodOutputAmount() * outputScale(countryProductionPolicy, buildingTypeData, level, 1f, level)
                * (1f + managementBonus(economyBuilding, regionInstance, this.stateOwnerTag)) * (1f + infrastructureBonus(regionInstance));
            double costs = inputCost(economyBuilding, countryMarket, inputScale(countryProductionPolicy, level, level))
                + wageBill(countryMarket, regionInstance, buildingTypeData, countryLaborPolicy.minWageFactor(),
                    maxWorkers(buildingTypeData, level, buildingTypeData.primaryWorkerPopTypeRatio()),
                    maxWorkers(buildingTypeData, level, buildingTypeData.secondaryWorkerPopTypeRatio()));
            float unitMargin = costs > 0.0 ? (float) ((value - costs) / costs) : (value > 0.0 ? 1f : 0f);
            economyBuilding.unitMargin(unitMargin);
            float signal;
            if (economyBuilding.ownerTagId() == this.stateOwnerTag && countryMarket.treasury() > countryBudget.targetTreasury()) {
                signal = demandGap;
            } else {
                float coveredMargin = unitMargin < 0f ? unitMargin * (1f - countryProductionPolicy.industrySubsidyRate()) : unitMargin;
                signal = Math.clamp(coveredMargin, -1f, 1f);
            }
            float utilization = economyBuilding.utilization() + FACTORY_UTILIZATION_RATE * signal;
            economyBuilding.utilization(Math.clamp(utilization, 0f, 1f));
        }
    }
}
