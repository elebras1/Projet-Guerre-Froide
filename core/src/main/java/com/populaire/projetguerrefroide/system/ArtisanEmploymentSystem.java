package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;
import com.populaire.projetguerrefroide.util.EmploymentUtils;

public class ArtisanEmploymentSystem {

    public ArtisanEmploymentSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("ArtisanEmploymentSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstance.class)
            .with(RegionDemographics.class)
            .iter(this::employ);
    }

    private void employ(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world);
        int artisanIndex = crafts.artisanPopTypeIndex();
        if (crafts.count() == 0 || artisanIndex < 0) {
            return;
        }
        float baseShare = EmploymentUtils.baseWorkforceShares(world)[artisanIndex];

        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        long countryId = 0;
        float activeShare = 0f;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                CountryPopulationPolicyView populationPolicy = world.obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                activeShare = EmploymentUtils.activeShare(baseShare, populationPolicy.workforceShareModifier());
            }
            int artisans = regionDemographics.totalByPopType(artisanIndex);
            regionArtisans.activeArtisans(artisans * activeShare);
            regionInstance.workerPopTypeEmploymentRatios(artisanIndex, artisans > 0 ? activeShare : 0f);
        }
    }
}
