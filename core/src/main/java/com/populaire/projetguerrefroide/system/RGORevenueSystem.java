package com.populaire.projetguerrefroide.system;

import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.RGO_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.util.RgoUtils.wageBill;

public class RGORevenueSystem {

    public RGORevenueSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGORevenueSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .iter(this::distribute);
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
                EntityView country = iter.world().obtainEntityView(countryId);
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
            resourceGathering.unitMargin(wageBill > 0f ? (float) ((revenue - wageBill) / wageBill) : (revenue > 0.0 ? 1f : 0f));
        }
    }
}
