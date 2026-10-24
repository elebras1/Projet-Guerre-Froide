package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;

public class EconomyBuildingWorkingCapitalSystem {

    private final EcsConstants ecsConstants;

    public EconomyBuildingWorkingCapitalSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("EconomyBuildingWorkingCapitalSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::finance);
    }

    private void finance(Iter iter) {
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;
        CountryProductionPolicyView countryProductionPolicy = null;
        CountryInvestmentMutView countryInvestment = null;
        CountryBudgetView countryBudget = null;

        long regionId = 0;
        RegionInstanceView regionInstance = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        EconomyBuildingTypeView buildingTypeData = iter.field(EconomyBuildingType.class, 2).getView(0);

        for (int i = 0; i < iter.count(); i++) {
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);
            int owner = ownerIndex(economyBuilding.ownerTagId(), this.ecsConstants);
            if ((owner != STATE_OWNER && economyBuilding.unitMargin() <= 0f) || economyBuilding.utilization() <= 0f) {
                continue;
            }
            BuildingView building = buildingField.getMutView(i);
            if (building.parentId() != regionId) {
                regionId = building.parentId();
                regionInstance = iter.world().obtainEntityView(regionId).getView(RegionInstance.class);
            }
            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryLaborPolicy = country.getMutView(CountryLaborPolicy.class);
                countryProductionPolicy = country.getMutView(CountryProductionPolicy.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
                countryBudget = country.getMutView(CountryBudget.class);
            }

            int level = building.size();
            float utilization = economyBuilding.utilization();
            double dailyCost = inputCost(economyBuilding, countryMarket, inputScale(countryProductionPolicy, level, utilization * level))
                + wageBill(countryMarket, regionInstance, buildingTypeData, countryLaborPolicy.minWageFactor(),
                    utilization * maxWorkers(buildingTypeData, level, buildingTypeData.primaryWorkerPopTypeRatio()),
                    utilization * maxWorkers(buildingTypeData, level, buildingTypeData.secondaryWorkerPopTypeRatio()));
            if (economyBuilding.treasury() >= dailyCost) {
                continue;
            }

            double need = FACTORY_TREASURY_BUFFER_DAYS * dailyCost - economyBuilding.treasury();
            double available = owner == STATE_OWNER
                ? countryMarket.treasury() - countryBudget.reservedNeeds()
                : countryInvestment.funds(owner);
            double transfer = Math.min(need, Math.max(0.0, available));
            if (transfer <= 0.0) {
                continue;
            }
            if (owner == STATE_OWNER) {
                countryMarket.treasury(countryMarket.treasury() - transfer);
            } else {
                countryInvestment.funds(owner, countryInvestment.funds(owner) - transfer);
            }
            economyBuilding.treasury(economyBuilding.treasury() + transfer);
        }
    }
}
