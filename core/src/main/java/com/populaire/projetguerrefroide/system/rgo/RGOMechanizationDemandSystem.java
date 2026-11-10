package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_UPKEEP_SHARES;

/** Daily upkeep of the mechanization levels of an RGO (decision T14 d), demanded within what its treasury can pay. */
public class RGOMechanizationDemandSystem {
    private final EcsConstants ecsConstants;

    public RGOMechanizationDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOMechanizationDemandSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Province.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
        int[] upkeepGoods = this.ecsConstants.mechanizationUpkeepGoods();
        float[] goodCosts = this.ecsConstants.goodCosts();
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            if (rgo.mechanization() <= 0) {
                continue;
            }
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                countryMarket = iter.stage().obtainEntityView(countryId).getMutView(CountryMarket.class);
            }
            float parts = rgo.upkeepValue() * RGO_MECHANIZATION_UPKEEP_SHARES[0] / goodCosts[upkeepGoods[0]];
            float fuel = rgo.upkeepValue() * RGO_MECHANIZATION_UPKEEP_SHARES[1] / goodCosts[upkeepGoods[1]];
            double cost = (double) parts * countryMarket.goodBudgetPrices(upkeepGoods[0]) + (double) fuel * countryMarket.goodBudgetPrices(upkeepGoods[1]);
            double available = Math.max(0.0, rgo.treasury());
            float ratio = cost > available ? (float) (available / cost) : 1f;
            rgo.upkeepPartsDemanded(parts * ratio);
            rgo.upkeepFuelDemanded(fuel * ratio);
            countryMarket.goodDemandAmounts(upkeepGoods[0], countryMarket.goodDemandAmounts(upkeepGoods[0]) + parts * ratio);
            countryMarket.goodDemandAmounts(upkeepGoods[1], countryMarket.goodDemandAmounts(upkeepGoods[1]) + fuel * ratio);
        }
    }
}
