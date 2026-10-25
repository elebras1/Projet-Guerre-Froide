package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_REALLOCATION_RATE;
import static com.populaire.projetguerrefroide.util.Constants.MAX_ARTISAN_CRAFTS;

public class ArtisanAllocationSystem {

    public ArtisanAllocationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("ArtisanAllocationSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::allocate);
    }

    private void allocate(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        boolean reallocationDay = PopulationMobilityUtils.isMobilityDay(world);
        EconomyBuildingTypeView[] buildingTypes = new EconomyBuildingTypeView[count];
        ArtisanCraftView[] artisanCrafts = new ArtisanCraftView[count];
        for (int c = 0; c < count; c++) {
            EntityView buildingType = world.obtainEntityView(crafts.buildingTypeIds(c));
            buildingTypes[c] = buildingType.getView(EconomyBuildingType.class);
            artisanCrafts[c] = buildingType.getView(ArtisanCraft.class);
        }

        float[] margins = new float[MAX_ARTISAN_CRAFTS];
        float[] weights = new float[MAX_ARTISAN_CRAFTS];
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
                for (int c = 0; c < count; c++) {
                    margins[c] = ArtisanUtils.marginPerArtisan(buildingTypes[c], artisanCrafts[c], countryMarket);
                    int outputIndex = buildingTypes[c].goodOutputIndex();
                    weights[c] = countryMarket.goodDemandAmounts(outputIndex) * countryMarket.goodPrices(outputIndex);
                }
            }

            float total = 0f;
            for (int c = 0; c < count; c++) {
                total += regionArtisans.shares(c);
            }
            if (total <= 0f) {
                float totalWeight = 0f;
                for (int c = 0; c < count; c++) {
                    totalWeight += weights[c];
                }
                for (int c = 0; c < count; c++) {
                    regionArtisans.shares(c, totalWeight > 0f ? weights[c] / totalWeight : 1f / count);
                }
                continue;
            }
            if (!reallocationDay) {
                continue;
            }
            int best = 0;
            for (int c = 1; c < count; c++) {
                if (margins[c] > margins[best]) {
                    best = c;
                }
            }
            for (int c = 0; c < count; c++) {
                if (c == best || margins[c] >= margins[best]) {
                    continue;
                }
                float moving = ARTISAN_REALLOCATION_RATE * regionArtisans.shares(c);
                regionArtisans.shares(c, regionArtisans.shares(c) - moving);
                regionArtisans.shares(best, regionArtisans.shares(best) + moving);
            }
        }
    }
}
