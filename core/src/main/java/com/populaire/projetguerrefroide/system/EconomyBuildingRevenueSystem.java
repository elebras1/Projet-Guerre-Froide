package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class EconomyBuildingRevenueSystem {

    public EconomyBuildingRevenueSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingRevenueSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::distribute);
    }

    private void distribute(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;

        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);
        Field<EconomyBuildingType> buildingTypeField = iter.field(EconomyBuildingType.class, 2);
        EconomyBuildingTypeView buildingTypeData = buildingTypeField.getView(0);

        for(int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);

            if(building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryLaborPolicy = country.getMutView(CountryLaborPolicy.class);
            }

            int outputIndex = buildingTypeData.goodOutputIndex();
            float producedAmount = countryMarket.goodProducedAmounts(outputIndex);
            double revenue = producedAmount > 0f ? countryMarket.goodRevenues(outputIndex) * economyBuilding.production() / producedAmount : 0.0;
            float availableForWages = (float) economyBuilding.treasury();
            economyBuilding.treasury(revenue);

            float primaryMinWageFactor = (countryMarket.lifeCostsByPopType(buildingTypeData.primaryWorkerPopTypeIndex()) + 0.2f * countryMarket.everydayCostsByPopType(buildingTypeData.primaryWorkerPopTypeIndex())) * (1f + countryLaborPolicy.minWageFactor());
            float secondaryMinWageFactor = (countryMarket.lifeCostsByPopType(buildingTypeData.secondaryWorkerPopTypeIndex()) + 0.2f * countryMarket.everydayCostsByPopType(buildingTypeData.secondaryWorkerPopTypeIndex())) * (1f + countryLaborPolicy.minWageFactor());

            float normalizedPrimaryWages = primaryMinWageFactor * economyBuilding.primaryWorkerAmount() / NEEDS_SCALING_FACTOR;
            float normalizedSecondaryWages = secondaryMinWageFactor * economyBuilding.secondaryWorkerAmount() / NEEDS_SCALING_FACTOR;
            float normalizedTotalWages = normalizedPrimaryWages + normalizedSecondaryWages;

            float primaryWorkerMinWage;
            float secondaryWorkerMinWage;
            if(availableForWages < normalizedTotalWages) {
                float wageRatio = normalizedTotalWages > 0f ? availableForWages / normalizedTotalWages : 0f;
                primaryWorkerMinWage = normalizedPrimaryWages * wageRatio;
                secondaryWorkerMinWage = normalizedSecondaryWages * wageRatio;
                economyBuilding.profit(0f);
            } else {
                primaryWorkerMinWage = normalizedPrimaryWages;
                secondaryWorkerMinWage = normalizedSecondaryWages;
                economyBuilding.profit(availableForWages - normalizedTotalWages);
            }

            economyBuilding.primaryWorkerMinWage(primaryWorkerMinWage);
            economyBuilding.secondaryWorkerMinWage(secondaryWorkerMinWage);
        }
    }
}
