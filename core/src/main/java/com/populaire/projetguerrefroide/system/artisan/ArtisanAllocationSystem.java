package com.populaire.projetguerrefroide.system.artisan;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.market.MarketUtils;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.ARTISAN_REALLOCATION_RATE;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ARTISAN_CRAFTS;
import static com.populaire.projetguerrefroide.util.ArrayUtils.zeroed;

public class ArtisanAllocationSystem {

    private final EcsConstants ecsConstants;
    private final EconomyBuildingTypeView[] buildingTypes = new EconomyBuildingTypeView[MAX_ARTISAN_CRAFTS];
    private final ArtisanCraftView[] artisanCrafts = new ArtisanCraftView[MAX_ARTISAN_CRAFTS];
    private final float[] gaps = new float[MAX_ARTISAN_CRAFTS];
    private final float[] scores = new float[MAX_ARTISAN_CRAFTS];
    private final float[] weights = new float[MAX_ARTISAN_CRAFTS];

    public ArtisanAllocationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanAllocationSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::allocate);
    }

    private void allocate(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world, this.ecsConstants);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        boolean reallocationDay = PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants);
        EconomyBuildingTypeView[] buildingTypes = this.buildingTypes;
        ArtisanCraftView[] artisanCrafts = this.artisanCrafts;
        for (int c = 0; c < count; c++) {
            EntityView buildingType = world.obtainEntityView(crafts.buildingTypeIds(c));
            buildingTypes[c] = buildingType.getView(EconomyBuildingType.class);
            artisanCrafts[c] = buildingType.getView(ArtisanCraft.class);
        }

        GlobalMarketView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getView(GlobalMarket.class);
        float[] gaps = zeroed(this.gaps);
        for (int c = 0; c < count; c++) {
            gaps[c] = MarketUtils.clearingGap(globalMarket, buildingTypes[c].goodOutputIndex());
        }

        float[] scores = zeroed(this.scores);
        float[] weights = zeroed(this.weights);
        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
        long countryId = 0;
        CountryMarketView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                countryMarket = country.getView(CountryMarket.class);
                boolean supported = ArtisanUtils.stateSupported(country.getView(CountryProfitDistributionPolicy.class), countryMarket, country.getView(CountryBudget.class), this.ecsConstants);
                for (int c = 0; c < count; c++) {
                    scores[c] = supported ? gaps[c] : ArtisanUtils.marginPerArtisan(buildingTypes[c], artisanCrafts[c], countryMarket);
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
                if (scores[c] > scores[best]) {
                    best = c;
                }
            }
            for (int c = 0; c < count; c++) {
                if (c == best || scores[c] >= scores[best]) {
                    continue;
                }
                float moving = ARTISAN_REALLOCATION_RATE * regionArtisans.shares(c);
                regionArtisans.shares(c, regionArtisans.shares(c) - moving);
                regionArtisans.shares(best, regionArtisans.shares(best) + moving);
            }
        }
    }
}
