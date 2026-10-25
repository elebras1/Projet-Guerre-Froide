package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_TREASURY_BUFFER_DAYS;

public class ArtisanTreasuryInitializationSystem {

    public ArtisanTreasuryInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("ArtisanTreasuryInitializationSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
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
            double cost = 0.0;
            for (int c = 0; c < count; c++) {
                cost += ArtisanUtils.plannedInputCost(buildingTypes[c], ArtisanUtils.levels(regionArtisans, c, artisanCrafts[c]), countryMarket);
            }
            regionArtisans.treasury(ARTISAN_TREASURY_BUFFER_DAYS * cost);
        }
    }
}
