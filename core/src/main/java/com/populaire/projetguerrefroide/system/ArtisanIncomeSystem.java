package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_TREASURY_BUFFER_DAYS;

public class ArtisanIncomeSystem {

    public ArtisanIncomeSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("ArtisanIncomeSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstanceIncome.class)
            .iter(this::pay);
    }

    private void pay(Iter iter) {
        int artisanIndex = ArtisanUtils.crafts(iter.world()).artisanPopTypeIndex();
        if (artisanIndex < 0) {
            return;
        }
        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceIncomeMutView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            double payout = regionArtisans.treasury() - ARTISAN_TREASURY_BUFFER_DAYS * regionArtisans.plannedInputCost();
            if (payout <= 0.0) {
                continue;
            }
            regionArtisans.treasury(regionArtisans.treasury() - payout);
            regionInstanceIncome.profitShareByPopType(artisanIndex, regionInstanceIncome.profitShareByPopType(artisanIndex) + (float) payout);
        }
    }
}
