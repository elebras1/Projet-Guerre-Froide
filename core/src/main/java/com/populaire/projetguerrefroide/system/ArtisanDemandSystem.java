package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.MarketUtils;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_OUTPUT_SHARE;
import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

public class ArtisanDemandSystem {

    private final EcsConstants ecsConstants;

    public ArtisanDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanDemandSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        EconomyBuildingTypeView[] buildingTypes = new EconomyBuildingTypeView[count];
        ArtisanCraftView[] artisanCrafts = new ArtisanCraftView[count];
        for (int c = 0; c < count; c++) {
            EntityView buildingType = world.obtainEntityView(crafts.buildingTypeIds(c));
            buildingTypes[c] = buildingType.getView(EconomyBuildingType.class);
            artisanCrafts[c] = buildingType.getView(ArtisanCraft.class);
        }

        GlobalMarketView globalMarket = world.obtainEntityView(world.lookup("global_market")).getView(GlobalMarket.class);
        boolean[] lacking = new boolean[count];
        for (int c = 0; c < count; c++) {
            lacking[c] = MarketUtils.clearingGap(globalMarket, buildingTypes[c].goodOutputIndex()) > 0f;
        }

        float[] needs = new float[GOOD_COUNT];
        boolean[] active = new boolean[count];
        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                boolean supported = ArtisanUtils.stateSupported(country.getView(CountryProfitDistributionPolicy.class), countryMarket, country.getView(CountryBudget.class), this.ecsConstants);
                for (int c = 0; c < count; c++) {
                    active[c] = ArtisanUtils.marginPerArtisan(buildingTypes[c], artisanCrafts[c], countryMarket) > 0f || supported && lacking[c];
                }
            }
            regionArtisans.revenueToday(0.0);
            regionArtisans.inputCostToday(0.0);

            double cost = 0.0;
            double potentialCost = 0.0;
            for (int g = 0; g < GOOD_COUNT; g++) {
                needs[g] = 0f;
            }
            for (int c = 0; c < count; c++) {
                float potentialLevels = ArtisanUtils.levels(regionArtisans, c, artisanCrafts[c]);
                float levels = active[c] ? potentialLevels : 0f;
                regionArtisans.levels(c, levels);
                potentialCost += ArtisanUtils.plannedInputCost(buildingTypes[c], potentialLevels, countryMarket);
                if (levels <= 0f) {
                    continue;
                }
                for (int k = 0; k < buildingTypes[c].goodInputIndexesLength(); k++) {
                    int goodIndex = buildingTypes[c].goodInputIndexes(k);
                    if (goodIndex < 0) {
                        break;
                    }
                    float amount = levels * ARTISAN_OUTPUT_SHARE * buildingTypes[c].goodInputAmounts(k);
                    needs[goodIndex] += amount;
                    cost += (double) amount * countryMarket.goodBudgetPrices(goodIndex);
                }
            }
            double treasury = Math.max(0.0, regionArtisans.treasury());
            float ratio = cost > treasury ? (float) (treasury / cost) : 1f;
            regionArtisans.plannedInputCost(potentialCost);
            regionArtisans.inputBudgetRatio(ratio);
            for (int g = 0; g < GOOD_COUNT; g++) {
                float demanded = needs[g] * ratio;
                regionArtisans.demandedInputs(g, demanded);
                if (demanded > 0f) {
                    countryMarket.goodDemandAmounts(g, countryMarket.goodDemandAmounts(g) + demanded);
                }
            }
        }
    }
}
