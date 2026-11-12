package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

/**
 * Daily inputs of the production mode of an RGO (annex 12, decision 188): inputs per unit x the output the mode gives on the
 * last traditional-equivalent output, demanded within what the RGO's treasury can pay (same rule as the mechanization upkeep).
 */
public class RGOModeDemandSystem {
    private final EcsConstants ecsConstants;

    public RGOModeDemandSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("RGOModeDemandSystem")
            .kind(phaseId)
            .with(ResourceGathering.class)
            .with(Province.class)
            .iter(this::demand);
    }

    private void demand(Iter iter) {
        Field<ResourceGathering> rgoField = iter.field(ResourceGathering.class, 0);
        Field<Province> provinceField = iter.field(Province.class, 1);
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
            float output = rgo.modeBaseProduction() * this.ecsConstants.rgoModeOutputs()[mode];
            double cost = 0.0;
            for (int k = 0; k < inputs.length; k++) {
                cost += (double) amounts[k] * output * countryMarket.goodBudgetPrices(inputs[k]);
            }
            double available = Math.max(0.0, rgo.treasury());
            float ratio = cost > available ? (float) (available / cost) : 1f;
            for (int k = 0; k < inputs.length; k++) {
                float demanded = amounts[k] * output * ratio;
                rgo.modeInputsDemanded(k, demanded);
                countryMarket.goodDemandAmounts(inputs[k], countryMarket.goodDemandAmounts(inputs[k]) + demanded);
            }
        }
    }
}
