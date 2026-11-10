package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_MECHANIZATION_EFFICIENCY;

/** Delivers and pays the upkeep; the effective level is level x share of upkeep served (decision T14 b, d). */
public class RGOMechanizationPurchaseSystem {
    private final EcsConstants ecsConstants;

    public RGOMechanizationPurchaseSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOMechanizationPurchaseSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Province.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
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
            float parts = this.delivered(countryMarket, upkeepGoods[0], rgo.upkeepPartsDemanded());
            float fuel = this.delivered(countryMarket, upkeepGoods[1], rgo.upkeepFuelDemanded());
            double cost = (double) parts * countryMarket.goodPrices(upkeepGoods[0]) + (double) fuel * countryMarket.goodPrices(upkeepGoods[1]);
            countryMarket.goodRevenues(upkeepGoods[0], countryMarket.goodRevenues(upkeepGoods[0]) + (double) parts * countryMarket.goodPrices(upkeepGoods[0]));
            countryMarket.goodRevenues(upkeepGoods[1], countryMarket.goodRevenues(upkeepGoods[1]) + (double) fuel * countryMarket.goodPrices(upkeepGoods[1]));
            rgo.treasury(rgo.treasury() - cost);
            float servedValue = parts * goodCosts[upkeepGoods[0]] + fuel * goodCosts[upkeepGoods[1]];
            float served = rgo.upkeepValue() > 0f ? Math.clamp(servedValue / rgo.upkeepValue(), 0f, 1f) : 1f;
            rgo.upkeepServed(served);
            rgo.efficiency(1f + RGO_MECHANIZATION_EFFICIENCY * rgo.mechanization() * served);
        }
    }

    private float delivered(CountryMarketView countryMarket, int goodIndex, float demanded) {
        float demand = countryMarket.goodDemandAmounts(goodIndex);
        if (demanded <= 0f || demand <= 0f) {
            return 0f;
        }
        return demanded * countryMarket.goodConsumedAmounts(goodIndex) / demand;
    }
}
