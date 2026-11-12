package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

/**
 * Delivers and pays the inputs of the production mode (annex 12, decision 188); the mode works in proportion of the value of
 * its inputs served, at base costs: output factor = 1 + (output - 1) x share served. Nothing demanded (first day, empty
 * treasury) counts as served when the RGO had nothing to produce, so that a stopped RGO does not lose its mode.
 */
public class RGOModePurchaseSystem {
    private final EcsConstants ecsConstants;

    public RGOModePurchaseSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOModePurchaseSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Province.class)
            .iter(this::purchase);
    }

    private void purchase(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
        float[] costs = this.ecsConstants.goodCosts();
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            ResourceGatheringMutView rgo = rgoField.getMutView(i);
            int mode = rgo.mode();
            if (mode <= 0) {
                continue;
            }
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                countryMarket = iter.stage().obtainEntityView(countryId).getMutView(CountryMarket.class);
            }
            int[] inputs = this.ecsConstants.rgoModeInputGoods()[mode];
            float[] amounts = this.ecsConstants.rgoModeInputAmounts()[mode];
            float needed = rgo.modeBaseProduction() * this.ecsConstants.rgoModeOutputs()[mode];
            double cost = 0.0;
            float servedValue = 0f;
            float neededValue = 0f;
            for (int k = 0; k < inputs.length; k++) {
                float delivered = delivered(countryMarket, inputs[k], rgo.modeInputsDemanded(k));
                cost += (double) delivered * countryMarket.goodPrices(inputs[k]);
                countryMarket.goodRevenues(inputs[k], countryMarket.goodRevenues(inputs[k]) + (double) delivered * countryMarket.goodPrices(inputs[k]));
                servedValue += delivered * costs[inputs[k]];
                neededValue += amounts[k] * needed * costs[inputs[k]];
                rgo.modeInputsDemanded(k, 0f);
            }
            rgo.treasury(rgo.treasury() - cost);
            rgo.modeServed(neededValue > 0f ? Math.clamp(servedValue / neededValue, 0f, 1f) : 1f);
        }
    }

    private static float delivered(CountryMarketView countryMarket, int goodIndex, float demanded) {
        float demand = countryMarket.goodDemandAmounts(goodIndex);
        if (demanded <= 0f || demand <= 0f) {
            return 0f;
        }
        return demanded * countryMarket.goodConsumedAmounts(goodIndex) / demand;
    }
}
