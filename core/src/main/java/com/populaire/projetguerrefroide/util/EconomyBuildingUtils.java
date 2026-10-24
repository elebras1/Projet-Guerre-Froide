package com.populaire.projetguerrefroide.util;

import com.populaire.projetguerrefroide.component.CountryMarketView;
import com.populaire.projetguerrefroide.component.CountryProductionPolicyView;
import com.populaire.projetguerrefroide.component.EconomyBuilding;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;
import com.populaire.projetguerrefroide.component.EconomyBuildingView;
import com.populaire.projetguerrefroide.component.RegionInstanceView;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class EconomyBuildingUtils {

    public static EconomyBuilding newEconomyBuilding(EconomyBuildingTypeView buildingType, boolean advancedRecipe, long ownerTagId) {
        int[] activeInputGoodIndexes = new int[MAX_GOODS];
        float[] activeInputAmounts = new float[MAX_GOODS];
        for (int g = 0; g < MAX_GOODS; g++) {
            activeInputGoodIndexes[g] = advancedRecipe ? buildingType.advancedGoodInputIndexes(g) : buildingType.goodInputIndexes(g);
            activeInputAmounts[g] = advancedRecipe ? buildingType.advancedGoodInputAmounts(g) : buildingType.goodInputAmounts(g);
        }
        return new EconomyBuilding(ownerTagId, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0d, 0, 0, 0f, 0f, new float[MAX_GOODS], new float[MAX_GOODS], activeInputGoodIndexes, activeInputAmounts, advancedRecipe);
    }

    public static float throughput(CountryProductionPolicyView productionPolicy, int level) {
        return 1f + Math.min(productionPolicy.maximumEconomyScaleFactor(), level * 0.01f);
    }

    public static float maxWorkers(EconomyBuildingTypeView buildingType, int level, float popTypeRatio) {
        return level * buildingType.workforce() * popTypeRatio;
    }

    public static float effectiveScale(EconomyBuildingView economyBuilding, EconomyBuildingTypeView buildingType, int level) {
        float maxPrimary = maxWorkers(buildingType, level, buildingType.primaryWorkerPopTypeRatio());
        float primaryRatio = economyBuilding.primaryWorkerAmount() / Math.max(1f, maxPrimary);
        return Math.min(economyBuilding.utilization() * level, primaryRatio * level);
    }

    public static float inputScale(CountryProductionPolicyView productionPolicy, int level, float effectiveScale) {
        return (1f + productionPolicy.factoryInputModifier()) * throughput(productionPolicy, level) * effectiveScale;
    }

    public static float secondaryFill(EconomyBuildingView economyBuilding, EconomyBuildingTypeView buildingType, int level, float effectiveScale) {
        float needed = maxWorkers(buildingType, level, buildingType.secondaryWorkerPopTypeRatio()) * effectiveScale / Math.max(1, level);
        return needed > 0f ? Math.min(1f, economyBuilding.secondaryWorkerAmount() / needed) : 0f;
    }

    public static float staffingFactor(EconomyBuildingTypeView buildingType, float secondaryFill) {
        if (buildingType.secondaryWorkerPopTypeRatio() <= 0f) {
            return 1f;
        }
        return 1f + (buildingType.secondaryWorkerEffectMultiplier() - 1f) * (2f * secondaryFill - 1f);
    }

    public static float outputScale(CountryProductionPolicyView productionPolicy, EconomyBuildingTypeView buildingType, int level, float secondaryFill, float effectiveScale) {
        return throughput(productionPolicy, level) * (1f + productionPolicy.factoryOutputModifier()) * staffingFactor(buildingType, secondaryFill) * effectiveScale;
    }

    public static float infrastructureBonus(RegionInstanceView regionInstance) {
        return regionInstance.provinceCount() > 0 ? regionInstance.infrastructureBonusSum() / regionInstance.provinceCount() : 0f;
    }

    public static float managementBonus(EconomyBuildingView economyBuilding, RegionInstanceView regionInstance, long stateOwnerTag) {
        return economyBuilding.ownerTagId() == stateOwnerTag ? regionInstance.managementOutputBonus() : 0f;
    }

    public static double inputCost(EconomyBuildingView economyBuilding, CountryMarketView countryMarket, float inputScale) {
        double cost = 0.0;
        for (int g = 0; g < economyBuilding.activeInputGoodIndexesLength(); g++) {
            int goodIndex = economyBuilding.activeInputGoodIndexes(g);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) inputScale * economyBuilding.activeInputAmounts(g) * countryMarket.goodBudgetPrices(goodIndex);
        }
        return cost;
    }

    public static float wagePerUnit(CountryMarketView countryMarket, RegionInstanceView regionInstance, int popTypeIndex, float minWageFactor) {
        float minimumWage = (countryMarket.lifeCostsByPopType(popTypeIndex) + 0.2f * countryMarket.everydayCostsByPopType(popTypeIndex)) * (1f + minWageFactor);
        return minimumWage * (1f + regionInstance.wagePremiumByPopType(popTypeIndex));
    }

    public static float wageBill(CountryMarketView countryMarket, RegionInstanceView regionInstance, EconomyBuildingTypeView buildingType, float minWageFactor, float primaryWorkers, float secondaryWorkers) {
        return (wagePerUnit(countryMarket, regionInstance, buildingType.primaryWorkerPopTypeIndex(), minWageFactor) * primaryWorkers
            + wagePerUnit(countryMarket, regionInstance, buildingType.secondaryWorkerPopTypeIndex(), minWageFactor) * secondaryWorkers) / NEEDS_SCALING_FACTOR;
    }
}
