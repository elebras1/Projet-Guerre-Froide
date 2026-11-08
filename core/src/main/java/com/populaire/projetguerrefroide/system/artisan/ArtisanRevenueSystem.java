package com.populaire.projetguerrefroide.system.artisan;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.util.ArrayUtils.zeroed;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ARTISAN_CRAFTS;

public class ArtisanRevenueSystem {
    private final EcsConstants ecsConstants;
    private final int[] outputIndexes = new int[MAX_ARTISAN_CRAFTS];

    public ArtisanRevenueSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanRevenueSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::collect);
    }

    private void collect(Iter iter) {
        World world = iter.stage();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world, this.ecsConstants);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        int[] outputIndexes = zeroed(this.outputIndexes);
        for (int c = 0; c < count; c++) {
            EconomyBuildingTypeView buildingType = world.obtainEntityView(crafts.buildingTypeIds(c)).getView(EconomyBuildingType.class);
            outputIndexes[c] = buildingType.goodOutputIndex();
        }

        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
        long countryId = 0;
        CountryMarketView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                countryMarket = world.obtainEntityView(countryId).getView(CountryMarket.class);
            }
            double revenue = 0.0;
            for (int c = 0; c < count; c++) {
                float production = regionArtisans.production(c);
                float producedAmount = countryMarket.goodProducedAmounts(outputIndexes[c]);
                if (production > 0f && producedAmount > 0f) {
                    revenue += countryMarket.goodRevenues(outputIndexes[c]) * production / producedAmount;
                }
            }
            regionArtisans.treasury(regionArtisans.treasury() + revenue);
            regionArtisans.revenueToday(revenue);
        }
    }
}
