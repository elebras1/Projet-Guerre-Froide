package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.DIVIDEND_RATE;
import static com.populaire.projetguerrefroide.util.Constants.FACTORY_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.*;

public class EconomyBuildingTreasuryInitializationSystem {

    public EconomyBuildingTreasuryInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingTreasuryInitializationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
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
            float inputScale = inputScale(countryProductionPolicy, level, effectiveScale(economyBuilding, buildingTypeData, level));
            double dailyCost = inputCost(economyBuilding, countryMarket, inputScale)
                + wageBill(countryMarket, regionInstance, buildingTypeData, countryLaborPolicy.minWageFactor(), economyBuilding.primaryWorkerAmount(), economyBuilding.secondaryWorkerAmount());
            economyBuilding.treasury(FACTORY_TREASURY_BUFFER_DAYS * dailyCost + Math.max(0f, economyBuilding.profit()) / DIVIDEND_RATE);
            economyBuilding.profitTrend(economyBuilding.profit());
        }
    }
}
