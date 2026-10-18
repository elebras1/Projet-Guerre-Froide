package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;

public class ConstructionUtils {

    private static final float DELIVERED_EPSILON = 1e-3f;

    public static double levelCost(World world, long buildingTypeId, CountryMarketView countryMarket) {
        EntityView buildingType = world.obtainEntityView(buildingTypeId);
        double cost = 0.0;
        if (buildingType.has(EconomyBuildingType.class)) {
            EconomyBuildingTypeView typeData = buildingType.getView(EconomyBuildingType.class);
            for (int k = 0; k < typeData.goodCostIndexesLength(); k++) {
                int goodIndex = typeData.goodCostIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                cost += (double) typeData.goodCostAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
            }
        } else if (buildingType.has(DevelopmentBuildingType.class)) {
            DevelopmentBuildingTypeView typeData = buildingType.getView(DevelopmentBuildingType.class);
            for (int k = 0; k < typeData.goodCostIndexesLength(); k++) {
                int goodIndex = typeData.goodCostIndexes(k);
                if (goodIndex < 0) {
                    break;
                }
                cost += (double) typeData.goodCostAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
            }
        }
        return cost;
    }

    public static void startSite(World world, EcsConstants ecsConstants, long buildingId, long buildingTypeId, long regionInstanceId, long countryId, long ownerTagId) {
        EntityView buildingType = world.obtainEntityView(buildingTypeId);
        int time;
        int[] goodIndexes = new int[MAX_GOODS];
        float[] goodAmounts = new float[MAX_GOODS];
        if (buildingType.has(EconomyBuildingType.class)) {
            EconomyBuildingTypeView typeData = buildingType.getView(EconomyBuildingType.class);
            time = typeData.time();
            for (int k = 0; k < MAX_GOODS; k++) {
                goodIndexes[k] = typeData.goodCostIndexes(k);
                goodAmounts[k] = typeData.goodCostAmounts(k);
            }
        } else {
            DevelopmentBuildingTypeView typeData = buildingType.getView(DevelopmentBuildingType.class);
            time = typeData.time();
            for (int k = 0; k < MAX_GOODS; k++) {
                goodIndexes[k] = typeData.goodCostIndexes(k);
                goodAmounts[k] = typeData.goodCostAmounts(k);
            }
        }

        EntityView country = world.obtainEntityView(countryId);
        CountryProductionPolicyView productionPolicy = country.getView(CountryProductionPolicy.class);
        int duration = Math.max(1, (int) (time * (1f - productionPolicy.constructionSpeed())));
        CountryMarketView countryMarket = country.getView(CountryMarket.class);
        double committedCost = levelCost(world, buildingTypeId, countryMarket);
        CountryInvestmentMutView countryInvestment = country.getMutView(CountryInvestment.class);
        int owner = ownerIndex(ownerTagId, ecsConstants);
        countryInvestment.committed(owner, countryInvestment.committed(owner) + committedCost);

        float[] dailyGoods = new float[MAX_GOODS];
        for (int k = 0; k < MAX_GOODS; k++) {
            dailyGoods[k] = goodIndexes[k] >= 0 ? goodAmounts[k] / duration : 0f;
        }
        countryInvestment.sitesInProgress(countryInvestment.sitesInProgress() + 1);
        countryInvestment.sitesStarted(countryInvestment.sitesStarted() + 1);
        world.obtainEntityView(world.entity()).set(new ConstructionSite(buildingId, buildingTypeId, regionInstanceId, countryId, ownerTagId,
            duration, committedCost, goodIndexes, goodAmounts, dailyGoods, new float[MAX_GOODS]));
    }

    public static boolean finished(ConstructionSiteView site) {
        if (site.daysLeft() > 0) {
            return false;
        }
        for (int k = 0; k < site.goodIndexesLength(); k++) {
            if (site.goodIndexes(k) < 0) {
                break;
            }
            if (site.goodsLeft(k) > DELIVERED_EPSILON) {
                return false;
            }
        }
        return true;
    }
}
