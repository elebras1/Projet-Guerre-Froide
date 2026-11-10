package com.populaire.projetguerrefroide.system.rgo;

import com.populaire.projetguerrefroide.simulation.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.simulation.Constants.RGO_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.system.rgo.RgoUtils.wageBill;

public class RGORevenueSystem {

    public RGORevenueSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGORevenueSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .iter(this::distribute);
    }

    /**
     * An RGO without workers or output takes the margin of a first hire: output of one worker at the country's price
     * against the wage of one worker (decision T15). Otherwise its margin is computed on its revenue and wage bill.
     */
    private static float unitMargin(ResourceGatheringView resourceGathering, CountryMarketView countryMarket, int workerPopTypeIndex, float minWageFactor,
                                    double revenue, float wageBill) {
        if (wageBill > 0f) {
            return (float) ((revenue - wageBill) / wageBill);
        }
        if (revenue > 0.0) {
            return 1f;
        }
        float workerWage = wageBill(countryMarket, workerPopTypeIndex, minWageFactor, 1);
        if (workerWage <= 0f || resourceGathering.workerOutput() <= 0f) {
            return 0f;
        }
        return (countryMarket.goodPrices(resourceGathering.goodIndex()) * resourceGathering.workerOutput() - workerWage) / workerWage;
    }

    private void distribute(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        int workerPopTypeIndex = iter.field(ResourceGatheringType.class, 2).<ResourceGatheringTypeView>getView(0).workerPopTypeIndex();

        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);

            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryLaborPolicy = country.getMutView(CountryLaborPolicy.class);
            }

            int goodIndex = resourceGathering.goodIndex();
            float producedAmount = countryMarket.goodProducedAmounts(goodIndex);
            double revenue = producedAmount > 0f ? countryMarket.goodRevenues(goodIndex) * resourceGathering.production() / producedAmount : 0.0;
            double treasury = resourceGathering.treasury() + revenue;

            float wageBill = wageBill(countryMarket, workerPopTypeIndex, countryLaborPolicy.minWageFactor(), resourceGathering.workerAmount());
            float wage = (float) Math.min(wageBill, treasury);
            treasury -= wage;
            double profit = Math.max(0.0, treasury - RGO_TREASURY_BUFFER_DAYS * wageBill);
            treasury -= profit;

            resourceGathering.treasury(treasury);
            resourceGathering.workerMinWage(wage);
            resourceGathering.profit((float) profit);
            resourceGathering.unitMargin(unitMargin(resourceGathering, countryMarket, workerPopTypeIndex, countryLaborPolicy.minWageFactor(), revenue, wageBill));
        }
    }
}
