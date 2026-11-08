package com.populaire.projetguerrefroide.system.building;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.market.MarketUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.building.EconomyBuildingUtils.*;
import static com.populaire.projetguerrefroide.system.building.OwnerUtils.ownerIndex;

public class EconomyBuildingUtilizationSystem {

    private final EcsConstants ecsConstants;
    private final long stateOwnerTag;

    public EconomyBuildingUtilizationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.stateOwnerTag = ecsConstants.countryTag();
        ecsWorld.system("EconomyBuildingUtilizationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .with(ecsConstants.suspended()).optional()
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
        GlobalMarketView globalMarket = iter.stage().obtainEntityView(this.ecsConstants.globalMarket()).getView(GlobalMarket.class);
        float demandGap = Math.clamp(MarketUtils.clearingGap(globalMarket, buildingTypeData.goodOutputIndex()), -1f, 1f);
        boolean ordered = iter.isSet(3);

        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            if (building.parentId() != regionId) {
                regionId = building.parentId();
                regionInstance = iter.stage().obtainEntityView(regionId).getView(RegionInstance.class);
            }
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.stage().obtainEntityView(countryId);
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
            float coveredMargin = unitMargin < 0f ? unitMargin * (1f - countryProductionPolicy.industrySubsidyRate()) : unitMargin;
            economyBuilding.lossDays(coveredMargin < 0f ? economyBuilding.lossDays() + 1 : 0);
            int owner = ownerIndex(economyBuilding.ownerTagId(), this.ecsConstants);
            if (owner != STATE_OWNER) {
                if (economyBuilding.suspended()) {
                    if (unitMargin > 0f) {
                        economyBuilding.suspended(false);
                        economyBuilding.suspendedDays(0);
                    } else {
                        economyBuilding.suspendedDays(economyBuilding.suspendedDays() + 1);
                    }
                } else if (economyBuilding.lossDays() >= FACTORY_LOSS_SUSPENSION_DAYS && (owner == CAPITALIST_OWNER || economyBuilding.treasury() <= 0.0)) {
                    economyBuilding.suspended(true);
                    economyBuilding.suspendedDays(0);
                }
            }
            if (ordered || economyBuilding.suspended()) {
                economyBuilding.utilization(0f);
                continue;
            }
            float signal;
            if (economyBuilding.ownerTagId() == this.stateOwnerTag && countryMarket.treasury() > countryBudget.targetTreasury()) {
                signal = demandGap;
            } else {
                signal = Math.clamp(coveredMargin, -1f, 1f);
            }
            float utilization = economyBuilding.utilization() + FACTORY_UTILIZATION_RATE * signal;
            economyBuilding.utilization(Math.clamp(utilization, 0f, 1f));
        }
    }
}
