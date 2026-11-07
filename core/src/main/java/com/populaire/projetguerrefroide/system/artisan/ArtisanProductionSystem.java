package com.populaire.projetguerrefroide.system.artisan;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.ARTISAN_OUTPUT_SHARE;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ARTISAN_CRAFTS;

public class ArtisanProductionSystem {
    private final EcsConstants ecsConstants;
    private final EconomyBuildingTypeView[] buildingTypes = new EconomyBuildingTypeView[MAX_ARTISAN_CRAFTS];

    public ArtisanProductionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanProductionSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::produce);
    }

    private void produce(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world, this.ecsConstants);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        EconomyBuildingTypeView[] buildingTypes = this.buildingTypes;
        for (int c = 0; c < count; c++) {
            buildingTypes[c] = world.obtainEntityView(crafts.buildingTypeIds(c)).getView(EconomyBuildingType.class);
        }

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
            for (int c = 0; c < count; c++) {
                float levels = regionArtisans.levels(c);
                float fill = regionArtisans.inputBudgetRatio();
                for (int k = 0; k < buildingTypes[c].goodInputIndexesLength(); k++) {
                    int goodIndex = buildingTypes[c].goodInputIndexes(k);
                    if (goodIndex < 0) {
                        break;
                    }
                    fill = Math.min(fill, regionArtisans.inputBudgetRatio() * regionArtisans.inputFills(goodIndex));
                }
                float production = levels * ARTISAN_OUTPUT_SHARE * buildingTypes[c].goodOutputAmount() * fill;
                regionArtisans.production(c, production);
                if (production > 0f) {
                    int outputIndex = buildingTypes[c].goodOutputIndex();
                    countryMarket.goodProducedAmounts(outputIndex, countryMarket.goodProducedAmounts(outputIndex) + production);
                }
            }
        }
    }
}
