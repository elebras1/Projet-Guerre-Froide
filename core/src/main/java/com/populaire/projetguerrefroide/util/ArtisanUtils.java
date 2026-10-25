package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_OUTPUT_SHARE;

public class ArtisanUtils {

    public static GlobalArtisanCraftsView crafts(World world) {
        return world.obtainEntityView(world.lookup("global_artisan_crafts")).getView(GlobalArtisanCrafts.class);
    }

    public static float levels(RegionArtisansView regionArtisans, int craft, ArtisanCraftView artisanCraft) {
        return artisanCraft.artisansPerLevel() > 0 ? regionArtisans.activeArtisans() * regionArtisans.shares(craft) / artisanCraft.artisansPerLevel() : 0f;
    }

    public static double plannedInputCost(EconomyBuildingTypeView buildingType, float levels, CountryMarketView countryMarket) {
        double cost = 0.0;
        for (int k = 0; k < buildingType.goodInputIndexesLength(); k++) {
            int goodIndex = buildingType.goodInputIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) levels * ARTISAN_OUTPUT_SHARE * buildingType.goodInputAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
        }
        return cost;
    }

    public static float marginPerArtisan(EconomyBuildingTypeView buildingType, ArtisanCraftView artisanCraft, CountryMarketView countryMarket) {
        double value = (double) ARTISAN_OUTPUT_SHARE * buildingType.goodOutputAmount() * countryMarket.goodPrices(buildingType.goodOutputIndex());
        for (int k = 0; k < buildingType.goodInputIndexesLength(); k++) {
            int goodIndex = buildingType.goodInputIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            value -= (double) ARTISAN_OUTPUT_SHARE * buildingType.goodInputAmounts(k) * countryMarket.goodPrices(goodIndex);
        }
        return artisanCraft.artisansPerLevel() > 0 ? (float) (value / artisanCraft.artisansPerLevel()) : 0f;
    }
}
