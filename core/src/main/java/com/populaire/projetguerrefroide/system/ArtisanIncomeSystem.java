package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ArtisanUtils;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.EconomyBuildingUtils;

import static com.populaire.projetguerrefroide.util.Constants.ARTISAN_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.util.Constants.NEEDS_SCALING_FACTOR;

public class ArtisanIncomeSystem {

    private final EcsConstants ecsConstants;

    public ArtisanIncomeSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ArtisanIncomeSystem")
            .kind(phaseId)
            .with(RegionArtisans.class)
            .with(RegionInstanceIncome.class)
            .with(RegionInstance.class)
            .iter(this::pay);
    }

    private void pay(Iter iter) {
        World world = iter.world();
        GlobalArtisanCraftsView crafts = ArtisanUtils.crafts(world);
        int artisanIndex = crafts.artisanPopTypeIndex();
        if (artisanIndex < 0) {
            return;
        }
        int count = crafts.count();
        int[] artisansPerLevel = new int[count];
        for (int c = 0; c < count; c++) {
            ArtisanCraftView artisanCraft = world.obtainEntityView(crafts.buildingTypeIds(c)).getView(ArtisanCraft.class);
            artisansPerLevel[c] = artisanCraft.artisansPerLevel();
        }
        Field<RegionArtisans> regionArtisansField = iter.field(RegionArtisans.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 2);
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryBudgetView countryBudget = null;
        float minWageFactor = 0f;
        boolean stateOwned = false;
        float stateShareRatio = 1f;
        for (int i = 0; i < iter.count(); i++) {
            RegionArtisansMutView regionArtisans = regionArtisansField.getMutView(i);
            RegionInstanceIncomeMutView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryBudget = country.getView(CountryBudget.class);
                CountryLaborPolicyView countryLaborPolicy = country.getView(CountryLaborPolicy.class);
                minWageFactor = countryLaborPolicy.minWageFactor();
                CountryProfitDistributionPolicyView policy = country.getView(CountryProfitDistributionPolicy.class);
                stateOwned = ArtisanUtils.stateOwned(policy, this.ecsConstants);
                float totalShareStateRatio = policy.stateProfitShareRate() + policy.workerProfitShareRate();
                stateShareRatio = totalShareStateRatio > 0f ? policy.stateProfitShareRate() / totalShareStateRatio : 1f;
            }
            double reserve = ARTISAN_TREASURY_BUFFER_DAYS * regionArtisans.plannedInputCost();
            regionArtisans.wagesToday(0.0);
            if (stateOwned) {
                double employedArtisans = 0.0;
                for (int c = 0; c < count; c++) {
                    employedArtisans += (double) regionArtisans.levels(c) * artisansPerLevel[c];
                }
                double wageBill = EconomyBuildingUtils.wagePerUnit(countryMarket, regionInstance, artisanIndex, minWageFactor) * employedArtisans / NEEDS_SCALING_FACTOR;
                double surplus = countryMarket.treasury() - countryBudget.targetTreasury();
                double support = Math.min(reserve + wageBill - regionArtisans.treasury(), surplus);
                if (support > 0.0) {
                    countryMarket.treasury(countryMarket.treasury() - support);
                    countryMarket.enterpriseSupport(countryMarket.enterpriseSupport() + support);
                    regionArtisans.treasury(regionArtisans.treasury() + support);
                }
                double wages = Math.min(wageBill, Math.max(0.0, regionArtisans.treasury() - reserve));
                if (wages > 0.0) {
                    regionArtisans.treasury(regionArtisans.treasury() - wages);
                    regionArtisans.wagesToday(wages);
                    regionInstanceIncome.minWagesByPopType(artisanIndex, regionInstanceIncome.minWagesByPopType(artisanIndex) + (float) wages);
                }
            }
            double payout = regionArtisans.treasury() - reserve;
            if (payout <= 0.0) {
                continue;
            }
            regionArtisans.treasury(regionArtisans.treasury() - payout);
            double artisanShare = payout;
            if (stateOwned) {
                double stateShare = stateShareRatio * payout;
                regionInstanceIncome.countryProfitShare(regionInstanceIncome.countryProfitShare() + (float) stateShare);
                artisanShare -= stateShare;
            }
            regionInstanceIncome.profitShareByPopType(artisanIndex, regionInstanceIncome.profitShareByPopType(artisanIndex) + (float) artisanShare);
        }
    }
}
