package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.GOOD_COUNT;

public class ArtisanPurchaseSystem {

    public ArtisanPurchaseSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("ArtisanPurchaseSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        World world = iter.world();
        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                countryMarket = world.obtainEntityView(countryId).getMutView(CountryMarket.class);
            }
            double paid = 0.0;
            for (int g = 0; g < GOOD_COUNT; g++) {
                float demanded = regionArtisans.demandedInputs(g);
                float demand = countryMarket.goodDemandAmounts(g);
                if (demanded <= 0f || demand <= 0f) {
                    regionArtisans.inputFills(g, demanded <= 0f ? 1f : 0f);
                    continue;
                }
                float delivered = demanded * countryMarket.goodConsumedAmounts(g) / demand;
                double cost = (double) delivered * countryMarket.goodPrices(g);
                countryMarket.goodRevenues(g, countryMarket.goodRevenues(g) + cost);
                regionArtisans.inputFills(g, delivered / demanded);
                paid += cost;
            }
            regionArtisans.treasury(regionArtisans.treasury() - paid);
            regionArtisans.inputCostToday(paid);
        }
    }
}
