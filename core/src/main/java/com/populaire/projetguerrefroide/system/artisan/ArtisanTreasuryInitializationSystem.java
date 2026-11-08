package com.populaire.projetguerrefroide.system.artisan;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.ARTISAN_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.simulation.Constants.MAX_ARTISAN_CRAFTS;

public class ArtisanTreasuryInitializationSystem {
    private final EcsConstants ecsConstants;
    private final EconomyBuildingTypeView[] buildingTypes = new EconomyBuildingTypeView[MAX_ARTISAN_CRAFTS];
    private final ArtisanCraftView[] artisanCrafts = new ArtisanCraftView[MAX_ARTISAN_CRAFTS];

    public ArtisanTreasuryInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanTreasuryInitializationSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        World world = iter.stage();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world, this.ecsConstants);
        int count = crafts.count();
        if (count == 0) {
            return;
        }
        EconomyBuildingTypeView[] buildingTypes = this.buildingTypes;
        ArtisanCraftView[] artisanCrafts = this.artisanCrafts;
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
