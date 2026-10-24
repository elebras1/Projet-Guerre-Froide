package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingRevenueSystem {

    private final long stateOwnerTag;

    public EconomyBuildingRevenueSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.stateOwnerTag = ecsConstants.countryTag();
        ecsWorld.system("EconomyBuildingRevenueSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::distribute);
    }

    private void distribute(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;
        CountryProductionPolicyView countryProductionPolicy = null;
        CountryBudgetView countryBudget = null;

        long regionId = 0;
        RegionInstanceView regionInstance = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView buildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);
        int outputIndex = buildingTypeData.goodOutputIndex();

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

            float producedAmount = countryMarket.goodProducedAmounts(outputIndex);
            double revenue = producedAmount > 0f ? countryMarket.goodRevenues(outputIndex) * economyBuilding.production() / producedAmount : 0.0;
            economyBuilding.unitRevenue(producedAmount > 0f ? (float) (countryMarket.goodRevenues(outputIndex) / producedAmount) : countryMarket.goodPrices(outputIndex));
            double treasury = economyBuilding.treasury() + revenue;

            float minWageFactor = countryLaborPolicy.minWageFactor();
            float primaryWages = wagePerUnit(countryMarket, regionInstance, buildingTypeData.primaryWorkerPopTypeIndex(), minWageFactor) * economyBuilding.primaryWorkerAmount() / NEEDS_SCALING_FACTOR;
            float secondaryWages = wagePerUnit(countryMarket, regionInstance, buildingTypeData.secondaryWorkerPopTypeIndex(), minWageFactor) * economyBuilding.secondaryWorkerAmount() / NEEDS_SCALING_FACTOR;
            float wageBill = primaryWages + secondaryWages;
            double available = Math.max(0.0, treasury);
            float wageRatio = wageBill > available ? (float) (available / wageBill) : 1f;
            economyBuilding.primaryWorkerMinWage(primaryWages * wageRatio);
            economyBuilding.secondaryWorkerMinWage(secondaryWages * wageRatio);
            float wages = wageBill * wageRatio;
            treasury -= wages;

            int level = building.size();
            float effectiveScale = effectiveScale(economyBuilding, buildingTypeData, level);
            float inputScale = inputScale(countryProductionPolicy, level, effectiveScale);
            double dailyInputCost = inputCost(economyBuilding, countryMarket, inputScale);
            float fullOutput = buildingTypeData.goodOutputAmount() * outputScale(countryProductionPolicy, buildingTypeData, level, secondaryFill(economyBuilding, buildingTypeData, level, effectiveScale), effectiveScale)
                * (1f + managementBonus(economyBuilding, regionInstance, this.stateOwnerTag)) * (1f + infrastructureBonus(regionInstance));
            float stockRatio = fullOutput > 0f ? economyBuilding.production() / fullOutput : 0f;
            float profit = (float) (revenue - wages - dailyInputCost * stockRatio);
            economyBuilding.profit(profit);
            economyBuilding.profitTrend(economyBuilding.profitTrend() + (profit - economyBuilding.profitTrend()) / DAYS_PER_YEAR);

            double buffer = FACTORY_TREASURY_BUFFER_DAYS * (dailyInputCost + wageBill);
            float lossCoverage = economyBuilding.ownerTagId() == this.stateOwnerTag ? 1f : countryProductionPolicy.industrySubsidyRate();
            if (profit < 0f && lossCoverage > 0f) {
                double surplus = Math.max(0.0, countryMarket.treasury() - countryBudget.targetTreasury());
                double support = Math.min(Math.min(-profit * lossCoverage, Math.max(0.0, buffer - treasury)), surplus);
                if (support > 0.0) {
                    countryMarket.treasury(countryMarket.treasury() - support);
                    countryMarket.enterpriseSupport(countryMarket.enterpriseSupport() + support);
                    treasury += support;
                }
            }
            double dividend = DIVIDEND_RATE * Math.max(0.0, treasury - buffer);
            economyBuilding.dividend((float) dividend);
            economyBuilding.treasury(treasury - dividend);
        }
    }
}
